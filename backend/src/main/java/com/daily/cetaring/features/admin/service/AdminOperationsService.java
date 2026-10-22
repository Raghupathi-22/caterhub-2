package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminDashboardSummaryDTO;
import com.daily.cetaring.features.admin.dto.EventCreateRequest;
import com.daily.cetaring.features.admin.dto.OfferCreateRequest;
import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import com.daily.cetaring.features.admin.repository.PromotionCampaignRepository;
import com.daily.cetaring.features.admin.dto.AdminBookingDetailsDTO;
import com.daily.cetaring.features.admin.dto.AdminOrderSummaryDTO;
import com.daily.cetaring.features.admin.dto.AdminServiceRequestDetailsDTO;
import com.daily.cetaring.features.admin.dto.AdminStaffingJobDetailsDTO;
import com.daily.cetaring.features.catalog.ServiceCatalog;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.features.worker.entity.JobAssignment;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.repository.JobAssignmentRepository;
import com.daily.cetaring.features.worker.repository.StaffingJobAcceptanceRepository;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOperationsService {

    private final BookingRepository bookingRepository;
    private final CouponRepository couponRepository;
    private final PromotionCampaignRepository promotionCampaignRepository;
    private final UserRepository userRepository;
    private final JobAssignmentRepository jobAssignmentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final StaffingJobAcceptanceRepository staffingJobAcceptanceRepository;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryDTO getDashboardSummary(Long businessId) {
        List<Booking> bookings = bookingRepository.findByBusinessId(businessId);

        long totalOrders = bookings.size();
        long pendingOrders = bookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();
        long deliveredOrders = bookings.stream().filter(b -> "DELIVERED".equalsIgnoreCase(b.getStatus())).count();
        long cancelledOrders = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> "DELIVERED".equalsIgnoreCase(b.getStatus()))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0
                ? bookings.stream().map(Booking::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AdminDashboardSummaryDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Booking> getOrders(Long businessId) {
        return bookingRepository.findByBusinessId(businessId).stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .toList();
    }

    private static final List<JobAssignment.AssignmentStatus> ACCEPTED_ASSIGNMENT_STATUSES =
            List.of(JobAssignment.AssignmentStatus.ACCEPTED, JobAssignment.AssignmentStatus.COMPLETED);
    private static final List<StaffingJobAcceptance.AcceptanceStatus> ACCEPTED_ACCEPTANCE_STATUSES =
            List.of(StaffingJobAcceptance.AcceptanceStatus.ACCEPTED, StaffingJobAcceptance.AcceptanceStatus.COMPLETED);

    /**
     * Unified admin feed: catering orders, customer service requests, and any worker job that cannot be attributed
     * to a service request. Past and future events are both included for auditing. Uses a fixed number of batched
     * queries (no per-row lookups).
     */
    @Transactional(readOnly = true)
    public List<AdminOrderSummaryDTO> getAllOrders() {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getDeletedAt() == null)
                .toList();
        Map<Long, User> bookingCustomers = usersById(bookings.stream().map(Booking::getUserId).toList());
        Map<Long, Long> bookingAccepted = bookings.isEmpty() ? Map.of() : toCountMap(
                jobAssignmentRepository.countWorkersByBookingIds(bookings.stream().map(Booking::getId).toList(), ACCEPTED_ASSIGNMENT_STATUSES));

        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllWithCustomerOrderByCreatedAtDesc();
        List<StaffingRequest> jobs = staffingRequestRepository.findAll();
        StaffingJobLinker.LinkResult links = StaffingJobLinker.link(serviceRequests, jobs);
        Map<Long, Long> jobAccepted = jobs.isEmpty() ? Map.of() : toCountMap(
                staffingJobAcceptanceRepository.countWorkersByStaffingRequestIds(jobs.stream().map(StaffingRequest::getId).toList(), ACCEPTED_ACCEPTANCE_STATUSES));
        Map<Long, User> jobCreators = usersById(links.unmatchedJobs().stream()
                .map(job -> job.getCreatedBy() == null ? null : job.getCreatedBy().getId())
                .toList());

        java.util.stream.Stream<AdminOrderSummaryDTO> catering = bookings.stream()
                .map(b -> mapCateringOrderSummary(b, bookingCustomers.get(b.getUserId()), bookingAccepted.getOrDefault(b.getId(), 0L)));
        java.util.stream.Stream<AdminOrderSummaryDTO> services = serviceRequests.stream()
                .map(r -> mapServiceOrderSummary(r, links.jobsFor(r.getId()), jobAccepted));
        java.util.stream.Stream<AdminOrderSummaryDTO> orphanJobs = links.unmatchedJobs().stream()
                .map(job -> mapStaffingJobSummary(job, jobCreators.get(job.getCreatedBy() == null ? null : job.getCreatedBy().getId()), jobAccepted));

        return java.util.stream.Stream.of(catering, services, orphanJobs).flatMap(s -> s)
                .sorted(Comparator.comparing(AdminOrderSummaryDTO::getBookedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private AdminOrderSummaryDTO mapCateringOrderSummary(Booking booking, User customer, long accepted) {
        return AdminOrderSummaryDTO.builder()
                .id(booking.getId())
                .orderType(AdminOrderSummaryDTO.OrderType.CATERING_ORDER)
                .reference(booking.getBookingReference())
                .serviceType("CATERING")
                .serviceCategory("Catering & Food")
                .eventType(booking.getEventType())
                .eventDate(booking.getEventDate())
                .startTime(booking.getEventDateTime() == null ? null : booking.getEventDateTime().toLocalTime())
                .bookedAt(booking.getCreatedAt())
                .area(booking.getDeliveryAddress())
                .location(booking.getDeliveryAddress())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .acceptedWorkerCount((int) accepted)
                .requestedWorkerCount(null)
                .requestedRoles(List.of())
                .customer(customer == null ? null : mapCustomer(customer))
                .build();
    }

    private AdminOrderSummaryDTO mapServiceOrderSummary(ServiceRequest request, List<StaffingRequest> jobs, Map<Long, Long> jobAccepted) {
        User customer = request.getCreatedBy();
        int accepted = jobs.stream().mapToInt(job -> jobAccepted.getOrDefault(job.getId(), 0L).intValue()).sum();
        Integer requested = jobs.isEmpty() ? null : jobs.stream().mapToInt(job -> job.getRequiredWorkers() == null ? 0 : job.getRequiredWorkers()).sum();
        ServiceCatalog.CategoryDefinition category = ServiceCatalog.categoryByServiceType(request.getServiceType());
        return AdminOrderSummaryDTO.builder()
                .id(request.getId())
                .orderType(AdminOrderSummaryDTO.OrderType.SERVICE_REQUEST)
                .reference("SR-" + request.getId())
                .serviceType(request.getServiceType())
                .serviceCategory(category == null ? null : category.getName())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookedAt(request.getCreatedAt())
                .area(request.getArea())
                .location(request.getLocation())
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus() == null ? null : request.getStatus().name())
                .acceptedWorkerCount(accepted)
                .requestedWorkerCount(requested)
                .requestedRoles(requestedRoles(jobs))
                .customer(customer == null ? null : mapCustomer(customer))
                .build();
    }

    private AdminOrderSummaryDTO mapStaffingJobSummary(StaffingRequest job, User creator, Map<Long, Long> jobAccepted) {
        int requiredWorkers = job.getRequiredWorkers() == null ? 0 : job.getRequiredWorkers();
        return AdminOrderSummaryDTO.builder()
                .id(job.getId())
                .orderType(AdminOrderSummaryDTO.OrderType.STAFFING_JOB)
                .reference("JOB-" + job.getId())
                .serviceType(job.getWorkerType() == null ? null : job.getWorkerType().name())
                .serviceCategory(categoryNameForWorkerType(job.getWorkerType()))
                .eventType(job.getEventType())
                .eventDate(job.getEventDate())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .bookedAt(job.getCreatedAt())
                .area(job.getArea())
                .location(job.getLocation())
                .totalAmount(job.getPayment() == null ? null : job.getPayment().multiply(BigDecimal.valueOf(requiredWorkers)))
                .status(job.getStatus() == null ? null : job.getStatus().name())
                .acceptedWorkerCount(jobAccepted.getOrDefault(job.getId(), 0L).intValue())
                .requestedWorkerCount(job.getRequiredWorkers())
                .requestedRoles(requestedRoles(List.of(job)))
                .customer(creator == null ? null : mapCustomer(creator))
                .build();
    }

    public Booking updateOrderStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(status.toUpperCase());
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public AdminBookingDetailsDTO getBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        User customer = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<JobAssignment> assignments = jobAssignmentRepository.findActiveWithWorkersByBookingId(bookingId);
        // One entry per worker: the most recent ACCEPTED/COMPLETED assignment (list is newest first).
        Map<Long, JobAssignment> acceptedByWorker = new LinkedHashMap<>();
        assignments.stream()
                .filter(a -> ACCEPTED_ASSIGNMENT_STATUSES.contains(a.getStatus()))
                .forEach(a -> acceptedByWorker.putIfAbsent(a.getWorkerProfile().getId(), a));
        List<AdminBookingDetailsDTO.AcceptedWorkerDTO> acceptedWorkers = acceptedByWorker.values().stream()
                .map(this::mapAcceptedWorker)
                .toList();

        return AdminBookingDetailsDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .eventType(booking.getEventType())
                .guestCount(booking.getGuestCount())
                .mealType(booking.getMealType())
                .eventDate(booking.getEventDate())
                .eventDateTime(booking.getEventDateTime())
                .deliveryAddress(booking.getDeliveryAddress())
                .specialInstructions(booking.getSpecialInstructions())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .customer(mapCustomer(customer))
                .acceptedWorkers(acceptedWorkers)
                .acceptedWorkerCount(acceptedWorkers.size())
                .workerAssignments(assignments.stream().map(this::mapAssignment).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminServiceRequestDetailsDTO getServiceRequestDetails(Long serviceRequestId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));
        User customer = request.getCreatedBy();
        Long customerId = customer.getId();
        // Same attribution rule as the list view: explicit links first, then the customer's unlinked jobs
        // matched against all of that customer's requests so a job is never attributed to two requests.
        List<StaffingRequest> candidateJobs = new java.util.ArrayList<>(staffingRequestRepository.findByServiceRequestIdIn(List.of(serviceRequestId)));
        candidateJobs.addAll(staffingRequestRepository.findByServiceRequestIdIsNullAndCreatedByIdIn(List.of(customerId)));
        List<StaffingRequest> jobs = StaffingJobLinker.link(serviceRequestRepository.findByCreatedByIdOrderByCreatedAtDesc(customerId), candidateJobs)
                .jobsFor(serviceRequestId);

        List<StaffingJobAcceptance> acceptances = jobs.isEmpty() ? List.of()
                : staffingJobAcceptanceRepository.findWithWorkersByStaffingRequestIds(jobs.stream().map(StaffingRequest::getId).toList());
        List<StaffingJobAcceptance> accepted = acceptances.stream()
                .filter(a -> ACCEPTED_ACCEPTANCE_STATUSES.contains(a.getStatus()))
                .toList();
        Map<Long, Long> acceptedPerJob = accepted.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getStaffingRequest().getId(), java.util.stream.Collectors.counting()));

        return AdminServiceRequestDetailsDTO.builder()
                .id(request.getId())
                .serviceType(request.getServiceType())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .area(request.getArea())
                .selectedServices(fromStorage(request.getSelectedServices()))
                .instructions(request.getInstructions())
                .details(request.getDetails())
                .quoteBased(Boolean.TRUE.equals(request.getQuoteBased()))
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .customer(mapServiceCustomer(customer))
                .staffingJobs(jobs.stream().map(job -> mapStaffingJobDetails(job, acceptedPerJob.getOrDefault(job.getId(), 0L).intValue())).toList())
                .acceptedWorkers(accepted.stream().map(a -> mapServiceAcceptedWorker(a, a.getStaffingRequest())).toList())
                .requestedWorkerCount(jobs.isEmpty() ? null : jobs.stream().mapToInt(job -> job.getRequiredWorkers() == null ? 0 : job.getRequiredWorkers()).sum())
                .acceptedWorkerCount(accepted.size())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminStaffingJobDetailsDTO getStaffingJobDetails(Long jobId) {
        StaffingRequest job = staffingRequestRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Staffing request not found"));
        List<StaffingJobAcceptance> accepted = staffingJobAcceptanceRepository.findWithWorkersByStaffingRequestIds(List.of(jobId)).stream()
                .filter(a -> ACCEPTED_ACCEPTANCE_STATUSES.contains(a.getStatus()))
                .toList();
        User creator = job.getCreatedBy();
        return AdminStaffingJobDetailsDTO.builder()
                .id(job.getId())
                .serviceRequestId(job.getServiceRequestId())
                .serviceCategory(categoryNameForWorkerType(job.getWorkerType()))
                .workerType(job.getWorkerType())
                .eventType(job.getEventType())
                .eventDate(job.getEventDate())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .location(job.getLocation())
                .area(job.getArea())
                .requiredWorkers(job.getRequiredWorkers())
                .acceptedWorkerCount(accepted.size())
                .paymentPerWorker(job.getPayment())
                .additionalRequirements(job.getAdditionalRequirements())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .customer(creator == null ? null : mapServiceCustomer(creator))
                .acceptedWorkers(accepted.stream().map(a -> mapServiceAcceptedWorker(a, job)).toList())
                .build();
    }

    private Map<Long, User> usersById(java.util.Collection<Long> ids) {
        List<Long> distinct = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinct.isEmpty()) return Map.of();
        Map<Long, User> users = new java.util.HashMap<>();
        userRepository.findAllById(distinct).forEach(user -> users.put(user.getId(), user));
        return users;
    }

    private static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> counts = new java.util.HashMap<>();
        for (Object[] row : rows) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private static List<String> requestedRoles(List<StaffingRequest> jobs) {
        return jobs.stream()
                .map(StaffingRequest::getWorkerType)
                .filter(java.util.Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .toList();
    }

    static String categoryNameForWorkerType(WorkerProfile.WorkerType workerType) {
        if (workerType == null) return null;
        return ServiceCatalog.categories().stream()
                .filter(category -> category.getRoles().stream().anyMatch(role -> workerType.name().equals(role.getWorkerType())))
                .map(ServiceCatalog.CategoryDefinition::getName)
                .findFirst()
                .orElse(null);
    }

    private AdminBookingDetailsDTO.CustomerDTO mapCustomer(User user) {
        return AdminBookingDetailsDTO.CustomerDTO.builder()
                .id(user.getId()).name(fullName(user)).username(user.getUsername())
                .mobileNumber(user.getPhoneNumber()).email(user.getEmail()).verified(user.getIsVerified()).build();
    }

    private AdminServiceRequestDetailsDTO.CustomerDTO mapServiceCustomer(User user) {
        return AdminServiceRequestDetailsDTO.CustomerDTO.builder()
                .id(user.getId()).name(fullName(user)).username(user.getUsername())
                .mobileNumber(user.getPhoneNumber()).email(user.getEmail()).verified(user.getIsVerified()).build();
    }

    private AdminBookingDetailsDTO.AcceptedWorkerDTO mapAcceptedWorker(JobAssignment assignment) {
        WorkerProfile profile = assignment.getWorkerProfile();
        User user = profile.getUser();
        return AdminBookingDetailsDTO.AcceptedWorkerDTO.builder()
                .assignmentId(assignment.getId()).workerProfileId(profile.getId()).userId(user.getId())
                .name(fullName(user)).username(user.getUsername()).mobileNumber(user.getPhoneNumber()).email(user.getEmail())
                .workerType(profile.getWorkerType()).serviceCategory(categoryNameForWorkerType(profile.getWorkerType()))
                .profileStatus(profile.getStatus()).assignmentStatus(assignment.getStatus())
                .acceptedAt(assignment.getRespondedAt()).experienceYears(profile.getExperienceYears())
                .skills(profile.getSkills()).preferredAreas(profile.getPreferredAreas()).languages(profile.getLanguages())
                .rating(profile.getRating()).bio(profile.getBio()).build();
    }

    private AdminBookingDetailsDTO.WorkerAssignmentDTO mapAssignment(JobAssignment assignment) {
        WorkerProfile profile = assignment.getWorkerProfile();
        User user = profile.getUser();
        return AdminBookingDetailsDTO.WorkerAssignmentDTO.builder()
                .assignmentId(assignment.getId()).workerProfileId(profile.getId()).name(fullName(user))
                .mobileNumber(user.getPhoneNumber()).workerType(profile.getWorkerType()).status(assignment.getStatus())
                .offeredAt(assignment.getOfferedAt()).respondedAt(assignment.getRespondedAt())
                .declineReason(assignment.getDeclineReason()).build();
    }

    private AdminServiceRequestDetailsDTO.StaffingJobDTO mapStaffingJobDetails(StaffingRequest job, int acceptedCount) {
        int required = job.getRequiredWorkers() == null ? 0 : job.getRequiredWorkers();
        return AdminServiceRequestDetailsDTO.StaffingJobDTO.builder()
                .id(job.getId()).workerType(job.getWorkerType()).requiredWorkers(job.getRequiredWorkers())
                .acceptedWorkers(acceptedCount).remainingPositions(Math.max(0, required - acceptedCount))
                .paymentPerWorker(job.getPayment()).status(job.getStatus()).build();
    }

    private AdminServiceRequestDetailsDTO.AcceptedWorkerDTO mapServiceAcceptedWorker(StaffingJobAcceptance acceptance, StaffingRequest job) {
        WorkerProfile profile = acceptance.getWorkerProfile();
        User user = profile.getUser();
        return AdminServiceRequestDetailsDTO.AcceptedWorkerDTO.builder()
                .acceptanceId(acceptance.getId()).staffingRequestId(job.getId()).workerProfileId(profile.getId()).userId(user.getId())
                .name(fullName(user)).username(user.getUsername()).mobileNumber(user.getPhoneNumber()).email(user.getEmail())
                .workerType(profile.getWorkerType()).requestedWorkerType(job.getWorkerType())
                .serviceCategory(categoryNameForWorkerType(profile.getWorkerType()))
                .profileStatus(profile.getStatus()).acceptanceStatus(acceptance.getStatus())
                .acceptedAt(acceptance.getAcceptedAt()).experienceYears(profile.getExperienceYears()).skills(profile.getSkills())
                .preferredAreas(profile.getPreferredAreas()).languages(profile.getLanguages()).rating(profile.getRating()).bio(profile.getBio()).build();
    }

    private String fullName(User user) {
        String name = String.join(" ", user.getFirstName() == null ? "" : user.getFirstName(), user.getLastName() == null ? "" : user.getLastName()).trim();
        return name.isBlank() ? user.getUsername() : name;
    }

    private List<String> fromStorage(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split("\\R")).map(String::trim).filter(v -> !v.isBlank()).toList();
    }

    @Transactional(readOnly = true)
    public List<Coupon> getOffers(Long businessId) {
        return couponRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public Coupon createOffer(OfferCreateRequest request) {
        if (request.getValidFrom().isAfter(request.getValidUntil())) {
            throw new IllegalArgumentException("Offer valid_from must be before valid_until");
        }
        if (couponRepository.existsByCouponCode(request.getCouponCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .businessId(request.getBusinessId())
                .couponCode(request.getCouponCode().trim().toUpperCase())
                .description(request.getDescription().trim())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(true)
                .build();

        return couponRepository.save(coupon);
    }

    public Coupon setOfferActive(Long offerId, boolean active) {
        Coupon coupon = couponRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        coupon.setIsActive(active);
        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<PromotionCampaign> getEvents(Long businessId) {
        return promotionCampaignRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public PromotionCampaign createEvent(EventCreateRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Event start_date must be before end_date");
        }

        PromotionCampaign campaign = PromotionCampaign.builder()
                .businessId(request.getBusinessId())
                .campaignName(request.getCampaignName().trim())
                .campaignDescription(request.getCampaignDescription().trim())
                .campaignType(request.getCampaignType().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .targetAudience(request.getTargetAudience())
                .budget(request.getBudget())
                .status(request.getStatus() == null ? PromotionCampaign.CampaignStatus.DRAFT : request.getStatus())
                .build();

        return promotionCampaignRepository.save(campaign);
    }

    public PromotionCampaign updateEventStatus(Long eventId, String status) {
        PromotionCampaign campaign = promotionCampaignRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        PromotionCampaign.CampaignStatus parsedStatus = PromotionCampaign.CampaignStatus.valueOf(status.toUpperCase());
        campaign.setStatus(parsedStatus);
        return promotionCampaignRepository.save(campaign);
    }
}
