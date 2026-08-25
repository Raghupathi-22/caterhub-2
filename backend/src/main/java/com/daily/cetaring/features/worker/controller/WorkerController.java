package com.daily.cetaring.features.worker.controller;

import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/profiles")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.WorkerProfileResponse> createProfile(
        @Valid @RequestBody WorkerDtos.CreateWorkerProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.createProfile(request));
    }

    @PostMapping("/profiles/me")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.WorkerProfileResponse> createMyProfile(
        Authentication authentication,
        @Valid @RequestBody WorkerDtos.CreateMyWorkerProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            workerService.createProfileForUsername(authentication.getName(), request)
        );
    }

    @GetMapping("/profiles/me")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.WorkerProfileResponse getMyProfile(Authentication authentication) {
        return workerService.getProfileByUsername(authentication.getName());
    }

    @GetMapping("/profiles/{profileId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.WorkerProfileResponse getProfile(@PathVariable Long profileId) {
        return workerService.getProfile(profileId);
    }

    @GetMapping("/profiles/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.WorkerProfileResponse getProfileByUserId(@PathVariable Long userId) {
        return workerService.getProfileByUserId(userId);
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.WorkerProfileResponse> findWorkerProfiles(
        @RequestParam(required = false) WorkerProfile.WorkerType workerType,
        @RequestParam(required = false) WorkerProfile.WorkerStatus status
    ) {
        return workerService.findWorkerProfiles(workerType, status);
    }

    @PatchMapping("/profiles/{profileId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.WorkerProfileResponse updateProfileStatus(
        @PathVariable Long profileId,
        @Valid @RequestBody WorkerDtos.UpdateWorkerStatusRequest request
    ) {
        return workerService.updateProfileStatus(profileId, request);
    }

    @PostMapping("/profiles/{profileId}/availability")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.AvailabilityResponse> addAvailability(
        @PathVariable Long profileId,
        @Valid @RequestBody WorkerDtos.UpsertAvailabilityRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.addAvailability(profileId, request));
    }

    @GetMapping("/profiles/{profileId}/availability")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.AvailabilityResponse> getAvailability(
        @PathVariable Long profileId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return workerService.getAvailability(profileId, startDate, endDate);
    }

    @PostMapping("/profiles/{profileId}/documents")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.DocumentResponse> addDocument(
        @PathVariable Long profileId,
        @Valid @RequestBody WorkerDtos.AddDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.addDocument(profileId, request));
    }

    @GetMapping("/profiles/{profileId}/documents")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.DocumentResponse> getDocuments(@PathVariable Long profileId) {
        return workerService.getDocuments(profileId);
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.AssignmentResponse> assignWorker(
        @Valid @RequestBody WorkerDtos.AssignWorkerRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.assignWorker(request));
    }

    @PatchMapping("/assignments/{assignmentId}/response")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.AssignmentResponse respondToAssignment(
        @PathVariable Long assignmentId,
        @Valid @RequestBody WorkerDtos.RespondAssignmentRequest request
    ) {
        return workerService.respondToAssignment(assignmentId, request);
    }

    @GetMapping("/profiles/{profileId}/assignments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.AssignmentResponse> getAssignmentsForWorker(@PathVariable Long profileId) {
        return workerService.getAssignmentsForWorker(profileId);
    }

    @GetMapping("/assignments/by-booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.AssignmentResponse> getAssignmentsForBooking(@PathVariable Long bookingId) {
        return workerService.getAssignmentsForBooking(bookingId);
    }

    @GetMapping("/dashboard/me")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.WorkerDashboardResponse getMyWorkerDashboard(Authentication authentication) {
        return workerService.getWorkerDashboard(authentication.getName());
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.StaffingJobResponse> getAvailableJobs(
        Authentication authentication,
        @RequestParam(required = false) WorkerProfile.WorkerType role,
        @RequestParam(required = false) String area,
        @RequestParam(required = false) String search
    ) {
        return workerService.getAvailableStaffingJobs(authentication.getName(), role, area, search);
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.StaffingJobResponse getJob(@PathVariable Long jobId, Authentication authentication) {
        return workerService.getStaffingJob(jobId, authentication.getName());
    }

    @PostMapping("/jobs/{jobId}/accept")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.AcceptStaffingJobResponse acceptJob(@PathVariable Long jobId, Authentication authentication) {
        return workerService.acceptStaffingJob(jobId, authentication.getName());
    }

    @GetMapping("/jobs/me")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public List<WorkerDtos.WorkerJobResponse> getMyJobs(Authentication authentication) {
        return workerService.getMyStaffingJobs(authentication.getName());
    }

    @PutMapping("/availability/me")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public WorkerDtos.AvailabilityResponse updateMyAvailability(
        Authentication authentication,
        @Valid @RequestBody WorkerDtos.UpdateAvailabilityToggleRequest request
    ) {
        return workerService.updateMyAvailability(authentication.getName(), request);
    }

    @PostMapping("/staffing-requests")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<WorkerDtos.StaffingJobResponse> createStaffingRequest(
        Authentication authentication,
        @Valid @RequestBody WorkerDtos.CreateStaffingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            workerService.createStaffingRequestForUsername(authentication.getName(), request)
        );
    }
}
