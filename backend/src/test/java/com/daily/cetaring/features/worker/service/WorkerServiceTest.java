package com.daily.cetaring.features.worker.service;

import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.entity.JobAssignment;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerAvailability;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkerServiceTest {

    private WorkerService workerService;

    @Mock
    private WorkerProfileRepository workerProfileRepository;
    @Mock
    private WorkerAvailabilityRepository workerAvailabilityRepository;
    @Mock
    private WorkerDocumentRepository workerDocumentRepository;
    @Mock
    private JobAssignmentRepository jobAssignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private StaffingRequestRepository staffingRequestRepository;
    @Mock
    private StaffingJobAcceptanceRepository staffingJobAcceptanceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workerService = new WorkerService(
            workerProfileRepository,
            workerAvailabilityRepository,
            workerDocumentRepository,
            jobAssignmentRepository,
            userRepository,
            roleRepository,
            bookingRepository,
            staffingRequestRepository,
            staffingJobAcceptanceRepository
        );
    }

    @Test
    void createProfileRejectsDuplicateUserProfile() {
        User user = user(10L);
        WorkerDtos.CreateWorkerProfileRequest request = WorkerDtos.CreateWorkerProfileRequest.builder()
            .userId(10L)
            .workerType(WorkerProfile.WorkerType.CHEF)
            .experienceYears(4)
            .build();
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(workerProfileRepository.existsByUserIdAndDeletedAtIsNull(10L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workerService.createProfile(request)
        );

        assertEquals("Worker profile already exists for this user", exception.getMessage());
        verify(workerProfileRepository, never()).save(any());
    }

    @Test
    void createProfilePersistsPendingVerificationProfile() {
        User user = user(10L);
        WorkerDtos.CreateWorkerProfileRequest request = WorkerDtos.CreateWorkerProfileRequest.builder()
            .userId(10L)
            .workerType(WorkerProfile.WorkerType.SUPERVISOR)
            .experienceYears(6)
            .skills("Team handling")
            .build();

        when(workerProfileRepository.existsByUserIdAndDeletedAtIsNull(10L)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_WORKER")).thenReturn(Optional.of(workerRole()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> {
            WorkerProfile profile = invocation.getArgument(0);
            profile.setId(99L);
            profile.setUser(user);
            profile.setStatus(WorkerProfile.WorkerStatus.PENDING_VERIFICATION);
            profile.setExperienceYears(6);
            profile.setWorkerType(WorkerProfile.WorkerType.SUPERVISOR);
            return profile;
        });

        WorkerDtos.WorkerProfileResponse response = workerService.createProfile(request);

        assertEquals(99L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals(WorkerProfile.WorkerStatus.PENDING_VERIFICATION, response.getStatus());
        assertEquals(WorkerProfile.WorkerType.SUPERVISOR, response.getWorkerType());
        assertTrue(user.getRoles().stream().anyMatch(role -> "ROLE_WORKER".equals(role.getName())));
    }

    @Test
    void createMyProfileUsesAuthenticatedUserAndAssignsWorkerRole() {
        User user = user(11L);
        WorkerDtos.CreateMyWorkerProfileRequest request = WorkerDtos.CreateMyWorkerProfileRequest.builder()
            .workerType(WorkerProfile.WorkerType.CHEF)
            .experienceYears(8)
            .skills("Hyderabadi biryani, bulk cooking")
            .preferredAreas("Madhapur, Gachibowli")
            .languages("Telugu, Hindi")
            .bio("Experienced catering chef")
            .build();

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.existsByUserIdAndDeletedAtIsNull(11L)).thenReturn(false);
        when(roleRepository.findByName("ROLE_WORKER")).thenReturn(Optional.of(workerRole()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> {
            WorkerProfile profile = invocation.getArgument(0);
            profile.setId(101L);
            return profile;
        });

        WorkerDtos.WorkerProfileResponse response = workerService.createProfileForUsername("user11", request);

        assertEquals(101L, response.getId());
        assertEquals(11L, response.getUserId());
        assertEquals(WorkerProfile.WorkerStatus.PENDING_VERIFICATION, response.getStatus());
        assertTrue(user.getRoles().stream().anyMatch(role -> "ROLE_WORKER".equals(role.getName())));
    }

    @Test
    void addAvailabilityRejectsInvalidTimeWindow() {
        WorkerDtos.UpsertAvailabilityRequest request = WorkerDtos.UpsertAvailabilityRequest.builder()
            .availableDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(18, 0))
            .endTime(LocalTime.of(9, 0))
            .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workerService.addAvailability(1L, request)
        );

        assertEquals("Start time must be before end time", exception.getMessage());
    }

    @Test
    void assignWorkerRequiresActiveWorker() {
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.PENDING_VERIFICATION);
        WorkerDtos.AssignWorkerRequest request = WorkerDtos.AssignWorkerRequest.builder()
            .bookingId(30L)
            .workerProfileId(20L)
            .assignedByUserId(1L)
            .build();

        when(jobAssignmentRepository.existsByBookingIdAndWorkerProfileId(30L, 20L)).thenReturn(false);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(Booking.builder().id(30L).build()));
        when(workerProfileRepository.findById(20L)).thenReturn(Optional.of(profile));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workerService.assignWorker(request)
        );

        assertEquals("Only active workers can be assigned", exception.getMessage());
        verify(jobAssignmentRepository, never()).save(any());
    }

    @Test
    void respondToAssignmentAcceptsOnlyOfferedAssignments() {
        JobAssignment assignment = JobAssignment.builder()
            .id(40L)
            .status(JobAssignment.AssignmentStatus.OFFERED)
            .booking(Booking.builder().id(30L).build())
            .workerProfile(workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE))
            .assignedBy(user(1L))
            .workerType(WorkerProfile.WorkerType.CHEF)
            .build();

        when(jobAssignmentRepository.findById(40L)).thenReturn(Optional.of(assignment));
        when(jobAssignmentRepository.save(any(JobAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkerDtos.AssignmentResponse response = workerService.respondToAssignment(
            40L,
            WorkerDtos.RespondAssignmentRequest.builder()
                .status(JobAssignment.AssignmentStatus.ACCEPTED)
                .build()
        );

        assertEquals(JobAssignment.AssignmentStatus.ACCEPTED, response.getStatus());
        assertTrue(response.getRespondedAt() != null);
    }

    @Test
    void findWorkerProfilesFiltersByStatus() {
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.PENDING_VERIFICATION);
        when(workerProfileRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            WorkerProfile.WorkerStatus.PENDING_VERIFICATION
        )).thenReturn(java.util.List.of(profile));

        var profiles = workerService.findWorkerProfiles(null, WorkerProfile.WorkerStatus.PENDING_VERIFICATION);

        assertEquals(1, profiles.size());
        assertEquals(WorkerProfile.WorkerStatus.PENDING_VERIFICATION, profiles.get(0).getStatus());
    }

    @Test
    void acceptStaffingJobRejectsDuplicateAcceptance() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        StaffingRequest job = staffingRequest(50L, 10, 3);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));
        when(staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(50L, 20L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workerService.acceptStaffingJob(50L, "user11")
        );

        assertEquals("You have already accepted this job", exception.getMessage());
        verify(staffingJobAcceptanceRepository, never()).save(any());
    }

    @Test
    void acceptStaffingJobRejectsFilledJobBeforeSaving() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        StaffingRequest job = staffingRequest(50L, 10, 10);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workerService.acceptStaffingJob(50L, "user11")
        );

        assertEquals("All positions have been filled", exception.getMessage());
        verify(staffingJobAcceptanceRepository, never()).save(any());
    }

    @Test
    void acceptStaffingJobIncrementsAcceptedWorkersAndFillsAtCapacity() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        StaffingRequest job = staffingRequest(50L, 10, 9);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));
        when(staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(50L, 20L)).thenReturn(false);
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(staffingJobAcceptanceRepository.save(any(StaffingJobAcceptance.class))).thenAnswer(invocation -> {
            StaffingJobAcceptance acceptance = invocation.getArgument(0);
            acceptance.setId(70L);
            acceptance.setAcceptedAt(LocalDateTime.now());
            return acceptance;
        });

        WorkerDtos.AcceptStaffingJobResponse response = workerService.acceptStaffingJob(50L, "user11");

        assertEquals("Job accepted successfully.", response.getMessage());
        assertEquals(10, job.getAcceptedWorkers());
        assertEquals(StaffingRequest.StaffingStatus.FILLED, job.getStatus());
    }

    @Test
    void acceptStaffingJobRejectsJobOutsideRoleFamily() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        StaffingRequest job = staffingRequest(50L, 10, 3, WorkerProfile.WorkerType.SERVING_BOY);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));

        assertThrows(AccessDeniedException.class, () -> workerService.acceptStaffingJob(50L, "user11"));
        verify(staffingJobAcceptanceRepository, never()).save(any());
    }

    @Test
    void cateringServiceStaffCanAcceptCateringBoyJob() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        profile.setWorkerType(WorkerProfile.WorkerType.SERVING_STAFF);
        StaffingRequest job = staffingRequest(50L, 10, 2, WorkerProfile.WorkerType.CATERING_BOY);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));
        when(staffingJobAcceptanceRepository.existsByStaffingRequestIdAndWorkerProfileId(50L, 20L)).thenReturn(false);
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(staffingJobAcceptanceRepository.save(any(StaffingJobAcceptance.class))).thenAnswer(invocation -> {
            StaffingJobAcceptance acceptance = invocation.getArgument(0);
            acceptance.setId(71L);
            acceptance.setAcceptedAt(LocalDateTime.now());
            return acceptance;
        });

        WorkerDtos.AcceptStaffingJobResponse response = workerService.acceptStaffingJob(50L, "user11");

        assertEquals("Job accepted successfully.", response.getMessage());
        assertEquals(3, job.getAcceptedWorkers());
    }

    @Test
    void legacyFineGrainedRoleKeepsExactMatching() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        profile.setWorkerType(WorkerProfile.WorkerType.CATERING_BOY);
        StaffingRequest job = staffingRequest(50L, 10, 2, WorkerProfile.WorkerType.CATERING_GIRL);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(job));

        assertThrows(AccessDeniedException.class, () -> workerService.acceptStaffingJob(50L, "user11"));
    }

    @Test
    void availableJobsForOwnRoleQueryWholeRoleFamily() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        profile.setWorkerType(WorkerProfile.WorkerType.SERVING_STAFF);
        StaffingRequest job = staffingRequest(50L, 5, 1, WorkerProfile.WorkerType.CATERING_GIRL);

        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(profile));
        when(staffingRequestRepository.findByStatusAndWorkerTypeInOrderByEventDateAscStartTimeAsc(
            eq(StaffingRequest.StaffingStatus.OPEN), anyCollection())).thenReturn(List.of(job));

        var jobs = workerService.getAvailableStaffingJobs("user11", WorkerProfile.WorkerType.SERVING_STAFF, null, null);

        assertEquals(1, jobs.size());
        verify(staffingRequestRepository).findByStatusAndWorkerTypeInOrderByEventDateAscStartTimeAsc(
            StaffingRequest.StaffingStatus.OPEN,
            WorkerRoleFamilies.matchingJobTypes(WorkerProfile.WorkerType.SERVING_STAFF)
        );
    }

    @Test
    void updateMyProfileChangesOnlyEditableFieldsOfAuthenticatedWorker() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        profile.setRating(new BigDecimal("4.70"));
        profile.setTotalRatings(12);
        stubOwnProfile(user, profile);

        WorkerDtos.WorkerProfileResponse response = workerService.updateMyProfile("user11", validUpdate()
            .fullName("  Ravi   Kumar  Reddy ")
            .build());

        assertEquals("Ravi", user.getFirstName());
        assertEquals("Kumar Reddy", user.getLastName());
        assertEquals("90000000" + 11L, user.getPhoneNumber());
        assertEquals(Integer.valueOf(32), response.getAge());
        assertEquals("Biryani, bulk cooking", response.getSkills());
        assertEquals("Madhapur", response.getPreferredAreas());
        assertEquals("Telugu, Hindi", response.getLanguages());
        assertEquals("Ten years of catering", response.getBio());
        assertEquals(WorkerProfile.WorkerStatus.ACTIVE, response.getStatus());
        assertEquals(WorkerProfile.WorkerType.CHEF, response.getWorkerType());
        assertEquals(new BigDecimal("4.70"), profile.getRating());
        assertEquals(12, profile.getTotalRatings());
        assertEquals(5, profile.getExperienceYears());
        verify(workerProfileRepository).save(profile);
        verify(workerProfileRepository, never()).findById(any());
    }

    @Test
    void updateMyProfileDoesNotApproveOrResetPendingVerification() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.PENDING_VERIFICATION);
        profile.setUser(user);
        stubOwnProfile(user, profile);

        WorkerDtos.WorkerProfileResponse response = workerService.updateMyProfile("user11", validUpdate().build());

        assertEquals(WorkerProfile.WorkerStatus.PENDING_VERIFICATION, response.getStatus());
    }

    @Test
    void updateMyProfileWithoutWorkerProfileReturnsNotFound() {
        User user = user(11L);
        when(userRepository.findByUsername("user11")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(11L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> workerService.updateMyProfile("user11", validUpdate().build())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(workerProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMyProfileRejectsInvalidInputWithoutSaving() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        stubOwnProfile(user, profile);

        List<WorkerDtos.UpdateMyWorkerProfileRequest> invalid = List.of(
            validUpdate().age(17).build(),
            validUpdate().age(76).build(),
            validUpdate().fullName(" ").build(),
            validUpdate().fullName("A").build(),
            validUpdate().skills("  ").build(),
            validUpdate().preferredAreas(null).build(),
            validUpdate().languages("").build(),
            validUpdate().bio("x".repeat(WorkerService.MAX_TEXT_LENGTH + 1)).build()
        );
        for (WorkerDtos.UpdateMyWorkerProfileRequest request : invalid) {
            assertThrows(IllegalArgumentException.class, () -> workerService.updateMyProfile("user11", request));
        }

        verify(workerProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        assertEquals("User", user.getFirstName());
    }

    @Test
    void updateMyProfileAllowsClearingOptionalAgeAndBio() {
        User user = user(11L);
        WorkerProfile profile = workerProfile(20L, WorkerProfile.WorkerStatus.ACTIVE);
        profile.setUser(user);
        profile.setAge(40);
        profile.setBio("Old bio");
        stubOwnProfile(user, profile);

        WorkerDtos.WorkerProfileResponse response = workerService.updateMyProfile(
            "user11", validUpdate().fullName("Ravi").age(null).bio("  ").build());

        assertEquals("Ravi", user.getFirstName());
        assertEquals(null, user.getLastName());
        assertEquals(null, response.getAge());
        assertEquals(null, response.getBio());
    }

    private void stubOwnProfile(User user, WorkerProfile profile) {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserIdAndDeletedAtIsNull(user.getId())).thenReturn(Optional.of(profile));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private WorkerDtos.UpdateMyWorkerProfileRequest.UpdateMyWorkerProfileRequestBuilder validUpdate() {
        return WorkerDtos.UpdateMyWorkerProfileRequest.builder()
            .fullName("Ravi Kumar")
            .age(32)
            .skills("Biryani, bulk cooking")
            .preferredAreas("Madhapur")
            .languages("Telugu, Hindi")
            .bio("Ten years of catering");
    }

    private User user(Long id) {
        return User.builder()
            .id(id)
            .username("user" + id)
            .email("user" + id + "@example.com")
            .phoneNumber("90000000" + id)
            .passwordHash("hash")
            .firstName("User")
            .lastName(String.valueOf(id))
            .roles(new HashSet<>())
            .build();
    }

    private WorkerProfile workerProfile(Long id, WorkerProfile.WorkerStatus status) {
        return WorkerProfile.builder()
            .id(id)
            .user(user(100L + id))
            .workerType(WorkerProfile.WorkerType.CHEF)
            .status(status)
            .experienceYears(5)
            .build();
    }

    private Role workerRole() {
        return Role.builder()
            .id(3L)
            .name("ROLE_WORKER")
            .description("Worker role")
            .build();
    }

    private StaffingRequest staffingRequest(Long id, int required, int accepted) {
        // Matches the CHEF worker from workerProfile(...) so capacity/duplicate rules are exercised.
        return staffingRequest(id, required, accepted, WorkerProfile.WorkerType.CHEF);
    }

    private StaffingRequest staffingRequest(Long id, int required, int accepted, WorkerProfile.WorkerType type) {
        return StaffingRequest.builder()
            .id(id)
            .createdBy(user(1L))
            .eventType("Wedding Catering")
            .workerType(type)
            .eventDate(LocalDate.now().plusDays(3))
            .startTime(LocalTime.of(18, 0))
            .endTime(LocalTime.of(23, 0))
            .location("Kondapur Convention Hall")
            .area("Kondapur")
            .requiredWorkers(required)
            .acceptedWorkers(accepted)
            .payment(new BigDecimal("800"))
            .status(StaffingRequest.StaffingStatus.OPEN)
            .build();
    }
}
