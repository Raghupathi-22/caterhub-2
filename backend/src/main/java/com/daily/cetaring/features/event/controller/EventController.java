package com.daily.cetaring.features.event.controller;

import com.daily.cetaring.features.event.EventType;
import com.daily.cetaring.features.event.dto.EventDtos;
import com.daily.cetaring.features.event.entity.BusinessServiceOffering;
import com.daily.cetaring.features.event.repository.BusinessServiceOfferingRepository;
import com.daily.cetaring.features.event.service.ChecklistItemSpec;
import com.daily.cetaring.features.event.service.EventService;
import com.daily.cetaring.shared.entity.Business;
import com.daily.cetaring.shared.repository.BusinessRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final BusinessServiceOfferingRepository offeringRepository;
    private final BusinessRepository businessRepository;

    @GetMapping("/types")
    public ResponseEntity<List<EventDtos.EventTypeGroupResponse>> types() {
        return ResponseEntity.ok(eventService.eventTypes());
    }

    @PostMapping("/checklist/preview")
    public ResponseEntity<List<ChecklistItemSpec>> preview(@Valid @RequestBody EventDtos.ChecklistPreviewRequest request) {
        return ResponseEntity.ok(eventService.preview(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EventDtos.EventSummary>> mine(Authentication authentication) {
        return ResponseEntity.ok(eventService.mine(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<EventDtos.EventDashboard> create(@Valid @RequestBody EventDtos.CreateEventRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(eventService.create(request, authentication.getName()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDtos.EventDashboard> dashboard(@PathVariable Long eventId, Authentication authentication) {
        return ResponseEntity.ok(eventService.dashboard(eventId, authentication.getName()));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDtos.EventDashboard> update(@PathVariable Long eventId,
                                                            @Valid @RequestBody EventDtos.UpdateEventRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(eventService.update(eventId, request, authentication.getName()));
    }

    @PutMapping("/{eventId}/requirements/{requirementId}")
    public ResponseEntity<EventDtos.EventDashboard> updateRequirement(@PathVariable Long eventId,
                                                                        @PathVariable Long requirementId,
                                                                        @Valid @RequestBody EventDtos.UpdateRequirementRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.ok(eventService.updateRequirement(eventId, requirementId, request, authentication.getName()));
    }

    @PostMapping("/{eventId}/requirements/{requirementId}/book")
    public ResponseEntity<EventDtos.EventDashboard> bookRequirement(
            @PathVariable Long eventId,
            @PathVariable Long requirementId,
            @Valid @RequestBody EventDtos.BookRequirementRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(eventService.bookRequirement(eventId, requirementId, request, authentication.getName()));
    }

    @DeleteMapping("/{eventId}/requirements/{requirementId}")
    public ResponseEntity<EventDtos.EventDashboard> removeRequirement(@PathVariable Long eventId,
                                                                        @PathVariable Long requirementId,
                                                                        Authentication authentication) {
        return ResponseEntity.ok(eventService.removeOptionalRequirement(eventId, requirementId, authentication.getName()));
    }

    @GetMapping("/providers/{serviceKey}")
    public ResponseEntity<List<EventDtos.ProviderResponse>> providers(
            @PathVariable String serviceKey,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer guestCount,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Boolean verifiedOnly,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "rating") String sort) {

        List<BusinessServiceOffering> offerings = offeringRepository.findByServiceKeyAndEnabledTrue(serviceKey);
        List<EventDtos.ProviderResponse> results = new ArrayList<>();

        for (BusinessServiceOffering offering : offerings) {
            Business b = offering.getBusiness();
            if (b == null || !Boolean.TRUE.equals(b.getIsActive()) || b.getDeletedAt() != null) continue;
            if (city != null && !city.isBlank() && (b.getCity() == null || !b.getCity().equalsIgnoreCase(city.trim()))) continue;
            if (Boolean.TRUE.equals(verifiedOnly) && !Boolean.TRUE.equals(b.getIsVerified())) continue;
            if (vegetarian != null && vegetarian && !offering.isVegSupported()) continue;
            if (minPrice != null && offering.getPricePerUnit() != null && offering.getPricePerUnit().compareTo(minPrice) < 0) continue;
            if (maxPrice != null && offering.getPricePerUnit() != null && offering.getPricePerUnit().compareTo(maxPrice) > 0) continue;

            boolean eligible = true;
            String reason = null;
            if (guestCount != null) {
                if (offering.getMinCapacity() != null && guestCount < offering.getMinCapacity()) {
                    eligible = false; reason = "Minimum capacity is " + offering.getMinCapacity();
                } else if (offering.getMaxCapacity() != null && guestCount > offering.getMaxCapacity()) {
                    eligible = false; reason = "Maximum capacity is " + offering.getMaxCapacity();
                }
            }
            if (offering.getPricePerUnit() != null && minPrice != null && offering.getPricePerUnit().compareTo(minPrice) < 0) continue;
            results.add(EventDtos.ProviderResponse.builder()
                    .id(b.getId()).name(b.getName()).description(b.getDescription()).logoUrl(b.getLogoUrl())
                    .city(b.getCity()).area(b.getArea()).rating(b.getRating()).totalReviews(b.getTotalReviews())
                    .verified(b.getIsVerified()).minCapacity(offering.getMinCapacity()).maxCapacity(offering.getMaxCapacity())
                    .serviceRadiusKm(offering.getBusiness().getServiceRadiusKm()).pricePerUnit(offering.getPricePerUnit())
                    .serviceCategory(b.getServiceCategory()).completedEvents(b.getCompletedEvents())
                    .eligible(eligible).ineligibilityReason(reason).build());
        }

        if (guestCount != null) results.removeIf(r -> !r.isEligible());
        if ("price".equalsIgnoreCase(sort)) results.sort((a,b) -> compareNullable(a.getPricePerUnit(), b.getPricePerUnit()));
        else if ("distance".equalsIgnoreCase(sort)) results.sort((a,b) -> Integer.compare(
                a.getServiceRadiusKm() == null ? Integer.MAX_VALUE : a.getServiceRadiusKm(),
                b.getServiceRadiusKm() == null ? Integer.MAX_VALUE : b.getServiceRadiusKm()));
        else results.sort((a,b) -> compareNullable(b.getRating(), a.getRating()));

        return ResponseEntity.ok(results);
    }

    private static <T extends Comparable<T>> int compareNullable(T a, T b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }
}
