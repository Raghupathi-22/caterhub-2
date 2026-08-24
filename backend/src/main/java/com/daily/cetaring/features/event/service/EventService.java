package com.daily.cetaring.features.event.service;

import com.daily.cetaring.features.event.Event;
import com.daily.cetaring.features.event.EventRequirement;
import com.daily.cetaring.features.event.EventType;
import com.daily.cetaring.features.event.dto.EventDtos;
import com.daily.cetaring.features.event.repository.EventRepository;
import com.daily.cetaring.features.event.repository.EventRequirementRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventRequirementRepository eventRequirementRepository;
    private final UserRepository userRepository;

    public EventDtos.Response createEvent(EventDtos.CreateEventRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        if (username == null) {
            throw new IllegalArgumentException("Authentication required to create event");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Event event = Event.builder()
                .name(request.getName())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .guestCount(request.getGuestCount())
                .budget(request.getBudget())
                .createdBy(user)
                .build();

        // generate event code if not provided
        if (request.getEventCode() != null && !request.getEventCode().isBlank()) {
            event.setEventCode(request.getEventCode());
        } else {
            event.setEventCode(generateEventCode());
        }

        Event saved = eventRepository.save(event);

        // generate default checklist based on type
        List<EventRequirement> checklist = generateChecklistForType(saved, saved.getEventType());
        eventRequirementRepository.saveAll(checklist);

        return mapToResponse(saved);
    }

    public EventDtos.Response getEvent(Long id) {
        User user = currentUser();
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (e.getCreatedBy() == null || !e.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have access to this event");
        }
        return mapToResponse(e);
    }

    public List<EventDtos.Response> listMyEvents() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (username == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Event> events = eventRepository.findByCreatedByIdOrderByEventDateDesc(user.getId());
        return events.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public EventDtos.WorkspaceResponse getWorkspace(Long eventId) {
        User user = currentUser();
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (e.getCreatedBy() == null || !e.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have access to this event");
        }
        List<EventRequirement> requirements = eventRequirementRepository.findByEventId(eventId);

        BigDecimal totalPlanned = requirements.stream()
                .map(r -> r.getPlannedAmount() == null ? BigDecimal.ZERO : r.getPlannedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBooked = requirements.stream()
                .map(r -> r.getBookedAmount() == null ? BigDecimal.ZERO : r.getBookedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EventDtos.Requirement> checklist = requirements.stream().map(r -> EventDtos.Requirement.builder()
                .id(r.getId())
                .category(r.getCategory())
                .plannedAmount(r.getPlannedAmount())
                .bookedAmount(r.getBookedAmount())
                .requiredFlag(r.isRequiredFlag())
                .build()).collect(Collectors.toList());

        return EventDtos.WorkspaceResponse.builder()
                .id(e.getId())
                .eventCode(e.getEventCode())
                .name(e.getName())
                .eventType(e.getEventType())
                .eventDate(e.getEventDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .location(e.getLocation())
                .guestCount(e.getGuestCount())
                .budget(e.getBudget())
                .totalPlanned(totalPlanned)
                .totalBooked(totalBooked)
                .checklist(checklist)
                .build();
    }

    private EventDtos.Response mapToResponse(Event e) {
        return EventDtos.Response.builder()
                .id(e.getId())
                .eventCode(e.getEventCode())
                .name(e.getName())
                .eventType(e.getEventType())
                .eventDate(e.getEventDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .location(e.getLocation())
                .guestCount(e.getGuestCount())
                .budget(e.getBudget())
                .build();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (username == null || username.equals("anonymousUser")) {
            throw new IllegalArgumentException("Authentication required");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private String generateEventCode() {
        // Simple sequential-ish code using timestamp; replace with DB-sequence for production
        long ts = System.currentTimeMillis() % 1000000;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return String.format("CH-%s-%06d", year, ts);
    }

    private List<EventRequirement> generateChecklistForType(Event event, EventType type) {
        List<EventRequirement> list = new ArrayList<>();

        // Core services for every event.
        add(list, event, "Venue");
        add(list, event, "Catering");
        add(list, event, "Decoration");

        switch (type) {
            case MARRIAGE -> {
                add(list, event, "Catering Workforce");
                add(list, event, "Priest / Purohit");
                add(list, event, "Photography");
                add(list, event, "Videography");
                add(list, event, "Makeup");
                add(list, event, "Mehendi");
                add(list, event, "DJ / Music");
                add(list, event, "Transport");
                add(list, event, "Accommodation");
                add(list, event, "Invitations");
                add(list, event, "Flowers / Garlands");
                add(list, event, "Cleaning");
                add(list, event, "Other Custom Requirements");
            }
            case ENGAGEMENT -> {
                add(list, event, "Photography");
                add(list, event, "Makeup");
                add(list, event, "Mehendi");
                add(list, event, "DJ / Music");
                add(list, event, "Flowers / Garlands");
                add(list, event, "Transport");
                add(list, event, "Invitations");
                add(list, event, "Cleaning");
            }
            case POOJA -> {
                add(list, event, "Priest / Purohit");
                add(list, event, "Puja Materials");
                add(list, event, "Flowers / Garlands");
                add(list, event, "Photography");
                add(list, event, "Cleaning");
            }
            case BIRTHDAY, BABY_FUNCTION, HOUSEWARMING, ANNIVERSARY -> {
                add(list, event, "Photography");
                add(list, event, "Entertainment");
                add(list, event, "Transport");
                add(list, event, "Invitations");
                add(list, event, "Cleaning");
            }
            case CORPORATE, SCHOOL_COLLEGE -> {
                add(list, event, "Event Staff");
                add(list, event, "Audio / AV");
                add(list, event, "Photography");
                add(list, event, "Transport");
                add(list, event, "Invitations");
                add(list, event, "Cleaning");
            }
            case OTHER -> {
                add(list, event, "Photography");
                add(list, event, "Event Staff");
                add(list, event, "Transport");
                add(list, event, "Other Custom Requirements");
            }
        }

        return list;
    }

    private void add(List<EventRequirement> list, Event event, String category) {
        list.add(buildRequirement(event, category, null));
    }

    private EventRequirement buildRequirement(Event event, String category, BigDecimal planned) {
        return EventRequirement.builder()
                .event(event)
                .category(category)
                .plannedAmount(planned)
                .bookedAmount(BigDecimal.ZERO)
                .requiredFlag(true)
                .build();
    }
}
