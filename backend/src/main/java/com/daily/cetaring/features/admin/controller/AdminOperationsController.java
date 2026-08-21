package com.daily.cetaring.features.admin.controller;

import com.daily.cetaring.features.admin.dto.AdminDashboardSummaryDTO;
import com.daily.cetaring.features.admin.dto.EventCreateRequest;
import com.daily.cetaring.features.admin.dto.OfferCreateRequest;
import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import com.daily.cetaring.features.admin.service.AdminOperationsService;
import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.service.ServiceRequestService;
import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;
    private final ServiceRequestService serviceRequestService;
    private final StaffingRequestRepository staffingRequestRepository;

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

    @GetMapping("/service-requests")
    public List<ServiceRequestDtos.Response> getServiceRequests() {
        return serviceRequestService.all();
    }

    @PatchMapping("/staffing-requests/{requestId}/status")
    public WorkerDtos.StaffingJobResponse updateStaffingRequestStatus(@PathVariable Long requestId, @RequestParam StaffingRequest.StaffingStatus status) {
        StaffingRequest request = staffingRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Staffing request not found"));
        request.setStatus(status);
        StaffingRequest saved = staffingRequestRepository.save(request);
        return WorkerDtos.StaffingJobResponse.builder()
            .id(saved.getId()).eventType(saved.getEventType()).workerType(saved.getWorkerType())
            .eventDate(saved.getEventDate()).startTime(saved.getStartTime()).endTime(saved.getEndTime())
            .location(saved.getLocation()).area(saved.getArea()).requiredWorkers(saved.getRequiredWorkers())
            .acceptedWorkers(saved.getAcceptedWorkers()).remainingPositions(Math.max(0, saved.getRequiredWorkers() - saved.getAcceptedWorkers()))
            .payment(saved.getPayment()).additionalRequirements(saved.getAdditionalRequirements())
            .status(saved.getStatus()).alreadyAccepted(false).createdAt(saved.getCreatedAt()).build();
    }

    @GetMapping("/staffing-requests")
    public List<WorkerDtos.StaffingJobResponse> getStaffingRequests() {
        return staffingRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(job ->
            WorkerDtos.StaffingJobResponse.builder()
                .id(job.getId()).eventType(job.getEventType()).workerType(job.getWorkerType())
                .eventDate(job.getEventDate()).startTime(job.getStartTime()).endTime(job.getEndTime())
                .location(job.getLocation()).area(job.getArea()).requiredWorkers(job.getRequiredWorkers())
                .acceptedWorkers(job.getAcceptedWorkers()).remainingPositions(Math.max(0, job.getRequiredWorkers() - job.getAcceptedWorkers()))
                .payment(job.getPayment()).additionalRequirements(job.getAdditionalRequirements())
                .status(job.getStatus()).alreadyAccepted(false).createdAt(job.getCreatedAt()).build()
        ).toList();
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
