package com.daily.cetaring.features.event.service;

import com.daily.cetaring.config.exception.ResourceNotFoundException;
import com.daily.cetaring.features.booking.dto.CreateMyBookingRequest;
import com.daily.cetaring.features.booking.service.BookingService;
import com.daily.cetaring.features.event.*;
import com.daily.cetaring.features.event.dto.EventDtos;
import com.daily.cetaring.features.event.entity.Event;
import com.daily.cetaring.features.event.entity.EventRequirement;
import com.daily.cetaring.features.event.entity.EventTimelineEntry;
import com.daily.cetaring.features.event.repository.EventRepository;
import com.daily.cetaring.features.event.repository.EventRequirementRepository;
import com.daily.cetaring.features.event.repository.EventTimelineRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventRequirementRepository requirementRepository;
    private final EventTimelineRepository timelineRepository;
    private final EventChecklistCatalog checklistCatalog;
    private final EventQuantityEstimator quantityEstimator;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public List<EventDtos.EventTypeGroupResponse> eventTypes() {
        return Arrays.stream(EventGroup.values()).map(group -> {
            EventDtos.EventTypeGroupResponse response = new EventDtos.EventTypeGroupResponse();
            response.setGroup(group);
            response.setTypes(EventType.byGroup(group).stream().map(type -> {
                EventDtos.EventTypeResponse item = new EventDtos.EventTypeResponse();
                item.setCode(type);
                item.setDisplayName(type.getDisplayName());
                item.setGroup(type.getGroup());
                return item;
            }).toList());
            return response;
        }).toList();
    }

    public List<ChecklistItemSpec> preview(EventDtos.ChecklistPreviewRequest request) {
        EventType type = parseType(request.getEventType());
        return checklistCatalog.preview(type, request.getPoojaKind(), request.getAgeGroup());
    }

    @Transactional
    public EventDtos.EventDashboard create(EventDtos.CreateEventRequest request, String username) {
        User customer = currentUser(username);
        EventType type = parseType(request.getEventType());
        validateDate(request.getEventDate());
        validateTimes(request.getStartTime(), request.getEndTime());

        Event event = Event.builder()
                .eventCode(generateCode())
                .customer(customer)
                .eventType(type)
                .eventName(blankDefault(request.getEventName(), type.getDisplayName()))
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation().trim())
                .city(request.getCity())
                .area(request.getArea())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .guestCount(request.getGuestCount())
                .venueSetting(request.getVenueSetting())
                .foodPreference(request.getFoodPreference())
                .foodStyle(request.getFoodStyle())
                .specialRequirements(request.getSpecialRequirements())
                .notes(request.getNotes())
                .estimatedBudget(nonNegative(request.getEstimatedBudget()))
                .status(EventStatus.PLANNING)
                .build();
        event = eventRepository.save(event);

        Map<String, EventDtos.SelectedRequirement> selected = Optional.ofNullable(request.getSelectedServices())
                .orElseGet(List::of).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(EventDtos.SelectedRequirement::getServiceKey, x -> x, (a, b) -> b));

        for (ChecklistItemSpec spec : checklistCatalog.preview(type, request.getPoojaKind(), request.getAgeGroup())) {
            EventDtos.SelectedRequirement choice = selected.get(spec.serviceKey());
            boolean enabled = spec.required() || (choice != null && Boolean.TRUE.equals(choice.getSelected()));
            int quantity = choice != null && choice.getQuantity() != null
                    ? Math.max(1, choice.getQuantity())
                    : quantityEstimator.quantity(spec.quantityRule(), request.getGuestCount());
            BigDecimal estimate = quantityEstimator.estimate(spec.budgetRule(), request.getGuestCount(), request.getEstimatedBudget());
            BigDecimal customerBudget = choice != null && choice.getCustomerBudget() != null
                    ? nonNegative(choice.getCustomerBudget()) : estimate;

            requirementRepository.save(EventRequirement.builder()
                    .event(event)
                    .category(spec.category())
                    .serviceKey(spec.serviceKey())
                    .serviceName(spec.serviceName())
                    .quantity(quantity)
                    .unit(spec.unit())
                    .requiredFlag(spec.required())
                    .estimatedBudget(estimate)
                    .customerBudget(customerBudget)
                    .actualBookedAmount(BigDecimal.ZERO)
                    .status(enabled ? RequirementStatus.SELECTED : RequirementStatus.NOT_SELECTED)
                    .notes(choice == null ? null : choice.getNotes())
                    .build());
        }

        for (EventDtos.CustomRequirement custom : Optional.ofNullable(request.getCustomRequirements()).orElseGet(List::of)) {
            if (custom == null || isBlank(custom.getCategory()) || isBlank(custom.getServiceName())) continue;
            BigDecimal budget = nonNegative(custom.getCustomerBudget());
            requirementRepository.save(EventRequirement.builder()
                    .event(event)
                    .category(custom.getCategory().trim().toUpperCase(Locale.ROOT))
                    .serviceKey("custom_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                    .serviceName(custom.getServiceName().trim())
                    .description(custom.getDescription())
                    .quantity(custom.getQuantity() == null ? 1 : Math.max(1, custom.getQuantity()))
                    .unit(custom.getUnit() == null ? RequirementUnit.ITEM : custom.getUnit())
                    .requiredFlag(false)
                    .estimatedBudget(budget)
                    .customerBudget(budget)
                    .actualBookedAmount(BigDecimal.ZERO)
                    .status(RequirementStatus.SELECTED)
                    .notes(custom.getNotes())
                    .build());
        }

        addTimeline(event, "Event created", "Planning started for " + event.getEventType().getDisplayName());
        recalculate(event);
        return dashboard(event.getId(), username);
    }

    public List<EventDtos.EventSummary> mine(String username) {
        User user = currentUser(username);
        return eventRepository.findByCustomer_IdOrderByEventDateDesc(user.getId()).stream().map(this::summary).toList();
    }

    public EventDtos.EventDashboard dashboard(Long eventId, String username) {
        Event event = ownedEvent(eventId, username);
        List<EventRequirement> requirements = requirementRepository.findByEvent_IdOrderByCategoryAscIdAsc(eventId);
        List<EventTimelineEntry> timeline = timelineRepository.findByEvent_IdOrderByOccurredAtAsc(eventId);
        return EventDtos.EventDashboard.builder()
                .event(summary(event))
                .requirements(requirements.stream().map(this::requirementResponse).toList())
                .timeline(timeline.stream().map(t -> EventDtos.TimelineResponse.builder()
                        .id(t.getId()).title(t.getTitle()).detail(t.getDetail()).occurredAt(t.getOccurredAt()).build()).toList())
                .budgetWarning(budgetWarning(event))
                .build();
    }

    @Transactional
    public EventDtos.EventDashboard update(Long id, EventDtos.UpdateEventRequest request, String username) {
        Event event = ownedEvent(id, username);
        if (request.getEventDate() != null) { validateDate(request.getEventDate()); event.setEventDate(request.getEventDate()); }
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        validateTimes(event.getStartTime(), event.getEndTime());
        if (!isBlank(request.getEventName())) event.setEventName(request.getEventName().trim());
        if (!isBlank(request.getLocation())) event.setLocation(request.getLocation().trim());
        if (request.getCity() != null) event.setCity(request.getCity());
        if (request.getArea() != null) event.setArea(request.getArea());
        if (request.getGuestCount() != null) event.setGuestCount(Math.max(1, request.getGuestCount()));
        if (request.getEstimatedBudget() != null) event.setEstimatedBudget(nonNegative(request.getEstimatedBudget()));
        if (request.getNotes() != null) event.setNotes(request.getNotes());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        eventRepository.save(event);
        recalculate(event);
        return dashboard(id, username);
    }

    @Transactional
    public EventDtos.EventDashboard updateRequirement(Long eventId, Long requirementId,
                                                       EventDtos.UpdateRequirementRequest request, String username) {
        Event event = ownedEvent(eventId, username);
        EventRequirement r = requirementRepository.findByIdAndEvent_Id(requirementId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event requirement not found"));
        if (request.getQuantity() != null) r.setQuantity(Math.max(1, request.getQuantity()));
        if (request.getCustomerBudget() != null) r.setCustomerBudget(nonNegative(request.getCustomerBudget()));
        if (request.getNotes() != null) r.setNotes(request.getNotes());
        if (request.getSelected() != null) {
            if (request.getSelected()) r.setStatus(RequirementStatus.SELECTED);
            else if (!r.isRequiredFlag()) {
                if (r.getStatus() == RequirementStatus.BOOKED || r.getStatus() == RequirementStatus.IN_PROGRESS)
                    throw new IllegalArgumentException("Booked service must be cancelled before removing");
                r.setStatus(RequirementStatus.NOT_SELECTED);
            } else {
                throw new IllegalArgumentException("Required service cannot be removed");
            }
        }
        if (request.getStatus() != null) {
            if (request.getStatus() == RequirementStatus.NOT_SELECTED && r.isRequiredFlag())
                throw new IllegalArgumentException("Required service cannot be removed");
            r.setStatus(request.getStatus());
        }
        requirementRepository.save(r);
        addTimeline(event, "Requirement updated", r.getServiceName());
        recalculate(event);
        return dashboard(eventId, username);
    }


    @Transactional
    public EventDtos.EventDashboard bookRequirement(Long eventId, Long requirementId,
                                                      EventDtos.BookRequirementRequest request, String username) {
        Event event = ownedEvent(eventId, username);
        EventRequirement requirement = requirementRepository.findByIdAndEvent_Id(requirementId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event requirement not found"));
        if (request.getProviderId() == null) throw new IllegalArgumentException("Provider is required");
        if (request.getAmount() == null || request.getAmount().signum() <= 0)
            throw new IllegalArgumentException("Booking amount must be greater than zero");
        if (event.getEventDate() == null) throw new IllegalArgumentException("Event date is required");
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(
                event.getEventDate(), event.getStartTime() == null ? java.time.LocalTime.NOON : event.getStartTime());

        var booking = bookingService.createBookingForUsername(username, CreateMyBookingRequest.builder()
                .businessId(request.getProviderId())
                .eventType(event.getEventType().name())
                .guestCount(event.getGuestCount())
                .mealType(requirement.getServiceName())
                .eventDateTime(dateTime)
                .deliveryAddress(event.getLocation())
                .specialInstructions(request.getNotes())
                .estimatedAmount(request.getAmount())
                .build());

        requirement.setVendorId(request.getProviderId());
        requirement.setBookingId(booking.getId());
        requirement.setActualBookedAmount(request.getAmount());
        requirement.setStatus(RequirementStatus.BOOKED);
        requirementRepository.save(requirement);
        addTimeline(event, "Service booked", requirement.getServiceName());
        recalculate(event);
        return dashboard(eventId, username);
    }

    @Transactional
    public EventDtos.EventDashboard removeOptionalRequirement(Long eventId, Long requirementId, String username) {
        Event event = ownedEvent(eventId, username);
        EventRequirement r = requirementRepository.findByIdAndEvent_Id(requirementId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event requirement not found"));
        if (r.isRequiredFlag()) throw new IllegalArgumentException("Required service cannot be removed");
        if (r.getStatus() == RequirementStatus.BOOKED || r.getStatus() == RequirementStatus.IN_PROGRESS)
            throw new IllegalArgumentException("Booked service must be cancelled before removing");
        r.setStatus(RequirementStatus.NOT_SELECTED);
        r.setVendorId(null);
        r.setBookingId(null);
        requirementRepository.save(r);
        recalculate(event);
        return dashboard(eventId, username);
    }

    private EventDtos.EventSummary summary(Event e) {
        List<EventRequirement> rs = requirementRepository.findByEvent_IdOrderByCategoryAscIdAsc(e.getId());
        int required = (int) rs.stream().filter(EventRequirement::isRequiredFlag).count();
        int bookedRequired = (int) rs.stream().filter(r -> r.isRequiredFlag() && r.getStatus() == RequirementStatus.BOOKED).count();
        int selected = (int) rs.stream().filter(r -> r.getStatus() != RequirementStatus.NOT_SELECTED).count();
        return EventDtos.EventSummary.builder().id(e.getId()).eventCode(e.getEventCode())
                .eventType(e.getEventType()).eventName(e.getEventName()).eventDate(e.getEventDate())
                .startTime(e.getStartTime()).endTime(e.getEndTime()).location(e.getLocation()).city(e.getCity())
                .guestCount(e.getGuestCount()).status(e.getStatus()).estimatedBudget(e.getEstimatedBudget())
                .totalEstimatedCost(e.getTotalEstimatedCost()).totalBookedAmount(e.getTotalBookedAmount())
                .remainingBudget(e.getRemainingBudget()).bookedRequired(bookedRequired).requiredCount(required)
                .selectedCount(selected).overBudget(e.getTotalEstimatedCost().compareTo(e.getEstimatedBudget()) > 0).build();
    }

    private EventDtos.RequirementResponse requirementResponse(EventRequirement r) {
        return EventDtos.RequirementResponse.builder().id(r.getId()).category(r.getCategory())
                .serviceKey(r.getServiceKey()).serviceName(r.getServiceName()).description(r.getDescription())
                .quantity(r.getQuantity()).unit(r.getUnit()).required(r.isRequiredFlag())
                .estimatedBudget(r.getEstimatedBudget()).customerBudget(r.getCustomerBudget())
                .actualBookedAmount(r.getActualBookedAmount()).status(r.getStatus()).vendorId(r.getVendorId())
                .bookingId(r.getBookingId()).staffingRequestId(r.getStaffingRequestId())
                .confirmedWorkers(0).remainingWorkers(r.getUnit() == RequirementUnit.STAFF ? r.getQuantity() : 0)
                .notes(r.getNotes()).build();
    }

    private void recalculate(Event e) {
        List<EventRequirement> rs = requirementRepository.findByEvent_IdOrderByCategoryAscIdAsc(e.getId());
        BigDecimal estimated = rs.stream().filter(r -> r.getStatus() != RequirementStatus.NOT_SELECTED)
                .map(r -> r.getCustomerBudget() == null ? BigDecimal.ZERO : r.getCustomerBudget())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal booked = rs.stream().map(r -> r.getActualBookedAmount() == null ? BigDecimal.ZERO : r.getActualBookedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        e.setTotalEstimatedCost(estimated);
        e.setTotalBookedAmount(booked);
        e.setRemainingBudget(e.getEstimatedBudget().subtract(booked));
        eventRepository.save(e);
    }

    private String budgetWarning(Event e) {
        if (e.getTotalEstimatedCost().compareTo(e.getEstimatedBudget()) > 0) return "Planned services exceed your event budget.";
        if (e.getEstimatedBudget().signum() > 0 &&
                e.getTotalEstimatedCost().compareTo(e.getEstimatedBudget().multiply(new BigDecimal("0.85"))) > 0)
            return "You have used more than 85% of your planned budget.";
        return null;
    }

    private void addTimeline(Event e, String title, String detail) {
        timelineRepository.save(EventTimelineEntry.builder().event(e).title(title).detail(detail).occurredAt(LocalDateTime.now()).build());
    }

    private Event ownedEvent(Long id, String username) {
        User user = currentUser(username);
        return eventRepository.findByIdAndCustomer_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private EventType parseType(String raw) {
        if (isBlank(raw)) throw new IllegalArgumentException("Event type is required");
        return EventType.fromLegacy(raw);
    }

    private String generateCode() {
        return "CH-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private static String blankDefault(String value, String fallback) { return isBlank(value) ? fallback : value.trim(); }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static BigDecimal nonNegative(BigDecimal value) { return value == null ? BigDecimal.ZERO : value.max(BigDecimal.ZERO); }

    private static void validateDate(java.time.LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Event date is required");
        if (date.isBefore(java.time.LocalDate.now())) throw new IllegalArgumentException("Event date cannot be in the past");
    }
    private static void validateTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && !start.isBefore(end)) throw new IllegalArgumentException("Start time must be before end time");
    }
}
