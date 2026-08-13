package com.daily.cetaring.features.admin.controller;

import com.daily.cetaring.features.admin.dto.AdminDashboardSummaryDTO;
import com.daily.cetaring.features.admin.dto.EventCreateRequest;
import com.daily.cetaring.features.admin.dto.OfferCreateRequest;
import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import com.daily.cetaring.features.admin.service.AdminOperationsService;
import com.daily.cetaring.features.booking.entity.Booking;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;

    @GetMapping("/dashboard")
    public AdminDashboardSummaryDTO getDashboardSummary(@RequestParam Long businessId) {
        return adminOperationsService.getDashboardSummary(businessId);
    }

    @GetMapping("/orders")
    public List<Booking> getOrders(@RequestParam Long businessId) {
        return adminOperationsService.getOrders(businessId);
    }

    @PutMapping("/orders/{bookingId}/status")
    public Booking updateOrderStatus(@PathVariable Long bookingId, @RequestParam String status) {
        return adminOperationsService.updateOrderStatus(bookingId, status);
    }

    @GetMapping("/offers")
    public List<Coupon> getOffers(@RequestParam Long businessId) {
        return adminOperationsService.getOffers(businessId);
    }

    @PostMapping("/offers")
    public Coupon createOffer(@Valid @RequestBody OfferCreateRequest request) {
        return adminOperationsService.createOffer(request);
    }

    @PatchMapping("/offers/{offerId}/active")
    public Coupon setOfferActive(@PathVariable Long offerId, @RequestParam boolean active) {
        return adminOperationsService.setOfferActive(offerId, active);
    }

    @GetMapping("/events")
    public List<PromotionCampaign> getEvents(@RequestParam Long businessId) {
        return adminOperationsService.getEvents(businessId);
    }

    @PostMapping("/events")
    public PromotionCampaign createEvent(@Valid @RequestBody EventCreateRequest request) {
        return adminOperationsService.createEvent(request);
    }

    @PatchMapping("/events/{eventId}/status")
    public PromotionCampaign updateEventStatus(@PathVariable Long eventId, @RequestParam String status) {
        return adminOperationsService.updateEventStatus(eventId, status);
    }
}
