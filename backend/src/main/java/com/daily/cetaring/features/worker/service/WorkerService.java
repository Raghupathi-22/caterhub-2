package com.daily.cetaring.features.worker.service;

import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.entity.JobAssignment;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerAvailability;
import com.daily.cetaring.features.worker.entity.WorkerDocument;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.repository.JobAssignmentRepository;
import com.daily.cetaring.features.worker.repository.StaffingJobAcceptanceRepository;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.features.worker.repository.WorkerAvailabilityRepository;
import com.daily.cetaring.features.worker.repository.WorkerDocumentRepository;
import com.daily.cetaring.features.worker.repository.WorkerProfileRepository;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RoleRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerService {

    private final WorkerProfileRepository workerProfileRepository;
    private final WorkerAvailabilityRepository workerAvailabilityRepository;
    private final WorkerDocumentRepository workerDocumentRepository;
    private final JobAssignmentRepository jobAssignmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final StaffingJobAcceptanceRepository staffingJobAcceptanceRepository;

    public WorkerDtos.WorkerProfileResponse createProfile(WorkerDtos.CreateWorkerProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return createProfileForUser(user, request);
    }

    public WorkerDtos.WorkerProfileResponse createProfileForUsername(
        String username,
        WorkerDtos.CreateMyWorkerProfileRequest request
    ) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        WorkerDtos.CreateWorkerProfileRequest internalRequest = WorkerDtos.CreateWorkerProfileRequest.builder()
            .userId(user.getId())
            .workerType(request.getWorkerType())
            .experienceYears(request.getExperienceYears())
            .skills(request.getSkills())
            .preferredAreas(request.getPreferredAreas())
            .languages(request.getLanguages())
            .bio(request.getBio())
            .build();
        return createProfileForUser(user, internalRequest);
    }

    private WorkerDtos.WorkerProfileResponse createProfileForUser(User user, WorkerDtos.CreateWorkerProfileRequest request) {
        if (workerProfileRepository.existsByUserIdAndDeletedAtIsNull(user.getId())) {
            throw new IllegalArgumentException("Worker profile already exists for this user");
        }

        WorkerProfile profile = WorkerProfile.builder()
            .user(user)
            .workerType(request.getWorkerType())
            .experienceYears(request.getExperienceYears())
            .skills(trimToNull(request.getSkills()))
            .preferredAreas(trimToNull(request.getPreferredAreas()))
            .languages(trimToNull(request.getLanguages()))
            .bio(trimToNull(request.getBio()))
            .status(WorkerProfile.WorkerStatus.PENDING_VERIFICATION)
            .build();

        ensureWorkerRole(user);
        return mapProfile(workerProfileRepository.save(profile));
    }

    private void ensureWorkerRole(User user) {
        Role workerRole = roleRepository.findByName("ROLE_WORKER")
            .orElseThrow(() -> new IllegalArgumentException("Worker role not configured"));
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        boolean hasWorkerRole = user.getRoles().stream()
            .anyMatch(role -> "ROLE_WORKER".equals(role.getName()));
        if (!hasWorkerRole) {
            user.getRoles().add(workerRole);
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public WorkerDtos.WorkerProfileResponse getProfile(Long profileId) {
        return mapProfile(getProfileEntity(profileId));
    }

    @Transactional(readOnly = true)
    public WorkerDtos.WorkerProfileResponse getProfileByUserId(Long userId) {
        return workerProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
            .map(this::mapProfile)
            .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
    }

    @Transactional(readOnly = true)
    public WorkerDtos.WorkerProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        return getProfileByUserId(user.getId());
    }

    private WorkerProfile getProfileEntity(Long profileId) {
        return workerProfileRepository.findById(profileId)
            .filter(profile -> profile.getDeletedAt() == null)
            .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.WorkerProfileResponse> findActiveWorkersByType(WorkerProfile.WorkerType workerType) {
        return workerProfileRepository
            .findByWorkerTypeAndStatusAndDeletedAtIsNull(workerType, WorkerProfile.WorkerStatus.ACTIVE)
            .stream()
            .map(this::mapProfile)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.WorkerProfileResponse> findWorkerProfiles(
        WorkerProfile.WorkerType workerType,
        WorkerProfile.WorkerStatus status
    ) {
        List<WorkerProfile> profiles;
        if (workerType != null && status != null) {
            profiles = workerProfileRepository.findByWorkerTypeAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(workerType, status);
        } else if (workerType != null) {
            profiles = workerProfileRepository.findByWorkerTypeAndDeletedAtIsNullOrderByCreatedAtDesc(workerType);
        } else if (status != null) {
            profiles = workerProfileRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status);
        } else {
            profiles = workerProfileRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
        }
        return profiles.stream().map(this::mapProfile).toList();
    }

    public WorkerDtos.WorkerProfileResponse updateProfileStatus(Long profileId, WorkerDtos.UpdateWorkerStatusRequest request) {
        WorkerProfile profile = getProfileEntity(profileId);
        profile.setStatus(request.getStatus());
        profile.setRejectionReason(null);

        if (request.getStatus() == WorkerProfile.WorkerStatus.ACTIVE) {
            User admin = null;
            if (request.getAdminUserId() != null) {
                admin = userRepository.findById(request.getAdminUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
            }
            profile.setApprovedBy(admin);
            profile.setApprovedAt(LocalDateTime.now());
        } else if (request.getStatus() == WorkerProfile.WorkerStatus.REJECTED) {
            profile.setApprovedBy(null);
            profile.setApprovedAt(null);
            profile.setRejectionReason(requireText(request.getRejectionReason(), "Rejection reason is required"));
        }

        return mapProfile(workerProfileRepository.save(profile));
    }

    public WorkerDtos.AvailabilityResponse addAvailability(Long profileId, WorkerDtos.UpsertAvailabilityRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        WorkerProfile profile = getProfileEntity(profileId);
        WorkerAvailability availability = WorkerAvailability.builder()
            .workerProfile(profile)
            .availableDate(request.getAvailableDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .status(request.getStatus() == null ? WorkerAvailability.AvailabilityStatus.AVAILABLE : request.getStatus())
            .notes(trimToNull(request.getNotes()))
            .build();

        return mapAvailability(workerAvailabilityRepository.save(availability));
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.AvailabilityResponse> getAvailability(Long profileId, LocalDate startDate, LocalDate endDate) {
        getProfileEntity(profileId);
        List<WorkerAvailability> records;
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            records = workerAvailabilityRepository
                .findByWorkerProfileIdAndAvailableDateBetweenOrderByAvailableDateAscStartTimeAsc(profileId, startDate, endDate);
        } else {
            records = workerAvailabilityRepository.findByWorkerProfileIdOrderByAvailableDateAscStartTimeAsc(profileId);
        }
        return records.stream().map(this::mapAvailability).toList();
    }

    public WorkerDtos.DocumentResponse addDocument(Long profileId, WorkerDtos.AddDocumentRequest request) {
        WorkerProfile profile = getProfileEntity(profileId);
        WorkerDocument document = WorkerDocument.builder()
            .workerProfile(profile)
            .documentType(request.getDocumentType())
            .fileName(requireText(request.getFileName(), "File name is required"))
            .fileUrl(requireText(request.getFileUrl(), "File URL is required"))
            .contentType(trimToNull(request.getContentType()))
            .fileSizeBytes(request.getFileSizeBytes())
            .status(WorkerDocument.DocumentStatus.PENDING)
            .build();

        return mapDocument(workerDocumentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.DocumentResponse> getDocuments(Long profileId) {
        getProfileEntity(profileId);
        return workerDocumentRepository.findByWorkerProfileIdOrderByCreatedAtDesc(profileId)
            .stream()
            .map(this::mapDocument)
            .toList();
    }

    public WorkerDtos.AssignmentResponse assignWorker(WorkerDtos.AssignWorkerRequest request) {
        if (jobAssignmentRepository.existsByBookingIdAndWorkerProfileId(request.getBookingId(), request.getWorkerProfileId())) {
            throw new IllegalArgumentException("Worker is already assigned to this booking");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        WorkerProfile workerProfile = getProfileEntity(request.getWorkerProfileId());
        if (workerProfile.getStatus() != WorkerProfile.WorkerStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active workers can be assigned");
        }
        User assignedBy = userRepository.findById(request.getAssignedByUserId())
            .orElseThrow(() -> new IllegalArgumentException("Assigning user not found"));

        JobAssignment assignment = JobAssignment.builder()
            .booking(booking)
            .workerProfile(workerProfile)
            .assignedBy(assignedBy)
            .workerType(workerProfile.getWorkerType())
            .status(JobAssignment.AssignmentStatus.OFFERED)
            .notes(trimToNull(request.getNotes()))
            .build();

        return mapAssignment(jobAssignmentRepository.save(assignment));
    }

    public WorkerDtos.AssignmentResponse respondToAssignment(Long assignmentId, WorkerDtos.RespondAssignmentRequest request) {
        JobAssignment assignment = jobAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (request.getStatus() != JobAssignment.AssignmentStatus.ACCEPTED
            && request.getStatus() != JobAssignment.AssignmentStatus.DECLINED) {
            throw new IllegalArgumentException("Workers can only accept or decline assignments");
        }
        if (assignment.getStatus() != JobAssignment.AssignmentStatus.OFFERED) {
            throw new IllegalArgumentException("Only offered assignments can be updated by workers");
        }

        assignment.setStatus(request.getStatus());
        assignment.setRespondedAt(LocalDateTime.now());
        if (request.getStatus() == JobAssignment.AssignmentStatus.DECLINED) {
            assignment.setDeclineReason(requireText(request.getDeclineReason(), "Decline reason is required"));
        } else {
            assignment.setDeclineReason(null);
        }

        return mapAssignment(jobAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.AssignmentResponse> getAssignmentsForWorker(Long profileId) {
        getProfileEntity(profileId);
        return jobAssignmentRepository.findByWorkerProfileIdOrderByCreatedAtDesc(profileId)
            .stream()
            .map(this::mapAssignment)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.AssignmentResponse> getAssignmentsForBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new IllegalArgumentException("Booking not found");
        }
        return jobAssignmentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
            .stream()
            .map(this::mapAssignment)
            .toList();
    }

    public WorkerDtos.WorkerDashboardResponse getWorkerDashboard(String username) {
        WorkerDtos.WorkerProfileResponse profile;
        try {
            profile = getProfileByUsername(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
            String fullName = String.join(" ",
                user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()
            ).trim();
            profile = WorkerDtos.WorkerProfileResponse.builder()
                .id(0L)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(fullName.isBlank() ? user.getUsername() : fullName)
                .status(WorkerProfile.WorkerStatus.PENDING_VERIFICATION)
                .build();
        }
        List<WorkerDtos.StaffingJobResponse> opportunities = getAvailableStaffingJobs(username, null, null, null).stream()
            .limit(5)
            .toList();
        List<WorkerDtos.WorkerJobResponse> myJobs = getMyStaffingJobs(username);
        return WorkerDtos.WorkerDashboardResponse.builder()
            .profile(profile)
            .profileCompletionPercent(profileCompletion(profile))
            .availableForWork(profile.getStatus() == WorkerProfile.WorkerStatus.ACTIVE)
            .nearbyOpportunities(opportunities)
            .myJobs(myJobs)
            .build();
    }

    public WorkerDtos.StaffingJobResponse createStaffingRequestForUsername(String username, WorkerDtos.CreateStaffingRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        User creator = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        StaffingRequest staffingRequest = StaffingRequest.builder()
            .createdBy(creator)
            .eventType(requireText(request.getEventType(), "Event type is required"))
            .workerType(request.getWorkerType())
            .eventDate(request.getEventDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .location(requireText(request.getLocation(), "Location is required"))
            .area(requireText(request.getArea(), "Area is required"))
            .requiredWorkers(request.getRequiredWorkers())
            .acceptedWorkers(0)
            .payment(request.getPayment())
            .additionalRequirements(trimToNull(request.getAdditionalRequirements()))
            .status(StaffingRequest.StaffingStatus.OPEN)
            .build();
        return mapStaffingJob(staffingRequestRepository.save(staffingRequest), false);
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.StaffingJobResponse> getAvailableStaffingJobs(String username, WorkerProfile.WorkerType role, String area, String search) {
        WorkerProfile profile = null;
        try {
            profile = getProfileEntityByUsername(username);
        } catch (IllegalArgumentException ignored) {
            // Customers/admins may browse staff requirements from non-worker contexts.
        }
        WorkerProfile.WorkerType effectiveRole = role != null ? role : (profile == null ? null : profile.getWorkerType());
        Long workerProfileId = profile == null ? null : profile.getId();
        List<StaffingRequest> jobs = effectiveRole == null
            ? staffingRequestRepository.findByStatusOrderByEventDateAscStartTimeAsc(StaffingRequest.StaffingStatus.OPEN)
            : staffingRequestRepository.findByStatusAndWorkerTypeOrderByEventDateAscStartTimeAsc(StaffingRequest.StaffingStatus.OPEN, effectiveRole);
        String areaFilter = trimToNull(area);
        String searchFilter = trimToNull(search);
        return jobs.stream()
            .filter(job -> areaFilter == null || job.getArea().equalsIgnoreCase(areaFilter))
            .filter(job -> searchFilter == null
                || job.getArea().toLowerCase().contains(searchFilter.toLowerCase())
                || job.getLocation().toLowerCase().contains(searchFilter.toLowerCase())
                || job.getEventType().toLowerCase().contains(searchFilter.toLowerCase()))
            .filter(job -> job.getAcceptedWorkers() < job.getRequiredWorkers())
            .map(job -> mapStaffingJob(job, workerProfileId != null && staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(job.getId(), workerProfileId)))
            .toList();
    }

    @Transactional(readOnly = true)
    public WorkerDtos.StaffingJobResponse getStaffingJob(Long jobId, String username) {
        WorkerProfile profile = null;
        try {
            profile = getProfileEntityByUsername(username);
        } catch (IllegalArgumentException ignored) {
        }
        Long profileId = profile == null ? null : profile.getId();
        StaffingRequest job = staffingRequestRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return mapStaffingJob(job, profileId != null && staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(jobId, profileId));
    }

    public WorkerDtos.AcceptStaffingJobResponse acceptStaffingJob(Long jobId, String username) {
        WorkerProfile profile = getProfileEntityByUsername(username);
        if (profile.getStatus() != WorkerProfile.WorkerStatus.ACTIVE) {
            throw new IllegalArgumentException("Worker profile must be approved before accepting jobs");
        }
        StaffingRequest job = staffingRequestRepository.findByIdForUpdate(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (job.getStatus() != StaffingRequest.StaffingStatus.OPEN || job.getAcceptedWorkers() >= job.getRequiredWorkers()) {
            throw new IllegalArgumentException("All positions have been filled");
        }
        if (staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(jobId, profile.getId())) {
            throw new IllegalArgumentException("You have already accepted this job");
        }
        job.setAcceptedWorkers(job.getAcceptedWorkers() + 1);
        if (job.getAcceptedWorkers().equals(job.getRequiredWorkers())) {
            job.setStatus(StaffingRequest.StaffingStatus.FILLED);
        }
        StaffingRequest savedJob = staffingRequestRepository.save(job);
        StaffingJobAcceptance acceptance = staffingJobAcceptanceRepository.save(StaffingJobAcceptance.builder()
            .staffingRequest(savedJob)
            .workerProfile(profile)
            .status(StaffingJobAcceptance.AcceptanceStatus.ACCEPTED)
            .build());
        return WorkerDtos.AcceptStaffingJobResponse.builder()
            .job(mapStaffingJob(savedJob, true))
            .myJob(mapWorkerJob(acceptance))
            .message("Job accepted successfully.")
            .build();
    }

    @Transactional(readOnly = true)
    public List<WorkerDtos.WorkerJobResponse> getMyStaffingJobs(String username) {
        WorkerProfile profile;
        try {
            profile = getProfileEntityByUsername(username);
        } catch (IllegalArgumentException ignored) {
            return List.of();
        }
        return staffingJobAcceptanceRepository.findByWorkerProfileIdOrderByAcceptedAtDesc(profile.getId())
            .stream()
            .map(this::mapWorkerJob)
            .toList();
    }

    public WorkerDtos.AvailabilityResponse updateMyAvailability(String username, WorkerDtos.UpdateAvailabilityToggleRequest request) {
        WorkerProfile profile = getProfileEntityByUsername(username);
        WorkerAvailability availability = WorkerAvailability.builder()
            .workerProfile(profile)
            .availableDate(LocalDate.now())
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(22, 0))
            .status(Boolean.FALSE.equals(request.getAvailable()) ? WorkerAvailability.AvailabilityStatus.UNAVAILABLE : WorkerAvailability.AvailabilityStatus.AVAILABLE)
            .notes(trimToNull(request.getNotes()))
            .build();
        return mapAvailability(workerAvailabilityRepository.save(availability));
    }

    private WorkerProfile getProfileEntityByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        return workerProfileRepository.findByUserIdAndDeletedAtIsNull(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
    }

    private int profileCompletion(WorkerDtos.WorkerProfileResponse profile) {
        int total = 6;
        int complete = 0;
        if (trimToNull(profile.getFullName()) != null) complete++;
        if (profile.getWorkerType() != null) complete++;
        if (profile.getExperienceYears() != null) complete++;
        if (trimToNull(profile.getSkills()) != null) complete++;
        if (trimToNull(profile.getPreferredAreas()) != null) complete++;
        if (trimToNull(profile.getLanguages()) != null) complete++;
        return (int) Math.round((complete * 100.0) / total);
    }

    private WorkerDtos.StaffingJobResponse mapStaffingJob(StaffingRequest job, boolean alreadyAccepted) {
        return WorkerDtos.StaffingJobResponse.builder()
            .id(job.getId())
            .eventType(job.getEventType())
            .workerType(job.getWorkerType())
            .eventDate(job.getEventDate())
            .startTime(job.getStartTime())
            .endTime(job.getEndTime())
            .location(job.getLocation())
            .area(job.getArea())
            .requiredWorkers(job.getRequiredWorkers())
            .acceptedWorkers(job.getAcceptedWorkers())
            .remainingPositions(Math.max(0, job.getRequiredWorkers() - job.getAcceptedWorkers()))
            .payment(job.getPayment())
            .additionalRequirements(job.getAdditionalRequirements())
            .status(job.getStatus())
            .alreadyAccepted(alreadyAccepted)
            .createdAt(job.getCreatedAt())
            .build();
    }

    private WorkerDtos.WorkerJobResponse mapWorkerJob(StaffingJobAcceptance acceptance) {
        StaffingRequest job = acceptance.getStaffingRequest();
        return WorkerDtos.WorkerJobResponse.builder()
            .acceptanceId(acceptance.getId())
            .jobId(job.getId())
            .eventType(job.getEventType())
            .workerType(job.getWorkerType())
            .eventDate(job.getEventDate())
            .startTime(job.getStartTime())
            .endTime(job.getEndTime())
            .location(job.getLocation())
            .area(job.getArea())
            .payment(job.getPayment())
            .status(acceptance.getStatus())
            .acceptedAt(acceptance.getAcceptedAt())
            .build();
    }

    private WorkerDtos.WorkerProfileResponse mapProfile(WorkerProfile profile) {
        User user = profile.getUser();
        String fullName = String.join(" ",
            user.getFirstName() == null ? "" : user.getFirstName(),
            user.getLastName() == null ? "" : user.getLastName()
        ).trim();

        return WorkerDtos.WorkerProfileResponse.builder()
            .id(profile.getId())
            .userId(user.getId())
            .username(user.getUsername())
            .fullName(fullName.isBlank() ? user.getUsername() : fullName)
            .workerType(profile.getWorkerType())
            .status(profile.getStatus())
            .experienceYears(profile.getExperienceYears())
            .skills(profile.getSkills())
            .preferredAreas(profile.getPreferredAreas())
            .languages(profile.getLanguages())
            .bio(profile.getBio())
            .rating(profile.getRating())
            .totalRatings(profile.getTotalRatings())
            .approvedAt(profile.getApprovedAt())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }

    private WorkerDtos.AvailabilityResponse mapAvailability(WorkerAvailability availability) {
        return WorkerDtos.AvailabilityResponse.builder()
            .id(availability.getId())
            .workerProfileId(availability.getWorkerProfile().getId())
            .availableDate(availability.getAvailableDate())
            .startTime(availability.getStartTime())
            .endTime(availability.getEndTime())
            .status(availability.getStatus())
            .notes(availability.getNotes())
            .build();
    }

    private WorkerDtos.DocumentResponse mapDocument(WorkerDocument document) {
        return WorkerDtos.DocumentResponse.builder()
            .id(document.getId())
            .workerProfileId(document.getWorkerProfile().getId())
            .documentType(document.getDocumentType())
            .fileName(document.getFileName())
            .fileUrl(document.getFileUrl())
            .contentType(document.getContentType())
            .fileSizeBytes(document.getFileSizeBytes())
            .status(document.getStatus())
            .reviewedAt(document.getReviewedAt())
            .rejectionReason(document.getRejectionReason())
            .createdAt(document.getCreatedAt())
            .build();
    }

    private WorkerDtos.AssignmentResponse mapAssignment(JobAssignment assignment) {
        return WorkerDtos.AssignmentResponse.builder()
            .id(assignment.getId())
            .bookingId(assignment.getBooking().getId())
            .workerProfileId(assignment.getWorkerProfile().getId())
            .assignedByUserId(assignment.getAssignedBy().getId())
            .workerType(assignment.getWorkerType())
            .status(assignment.getStatus())
            .offeredAt(assignment.getOfferedAt())
            .respondedAt(assignment.getRespondedAt())
            .completedAt(assignment.getCompletedAt())
            .notes(assignment.getNotes())
            .declineReason(assignment.getDeclineReason())
            .build();
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
