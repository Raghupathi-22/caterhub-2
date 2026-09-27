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
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<AdminOrderSummaryDTO> getAllOrders() {
        List<AdminOrderSummaryDTO> cateringOrders = bookingRepository.findAll().stream()
                .filter(b -> b.getDeletedAt() == null)
                .map(this::mapCateringOrderSummary)
                .toList();

        List<AdminOrderSummaryDTO> serviceOrders = serviceRequestRepository.findAll().stream()
                .map(this::mapServiceOrderSummary)
                .toList();

        return java.util.stream.Stream.concat(cateringOrders.stream(), serviceOrders.stream())
                .sorted(Comparator.comparing(AdminOrderSummaryDTO::getBookedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private AdminOrderSummaryDTO mapCateringOrderSummary(Booking booking) {
        User customer = userRepository.findById(booking.getUserId()).orElse(null);
        int accepted = (int) jobAssignmentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId()).stream()
                .filter(a -> a.getStatus() == JobAssignment.AssignmentStatus.ACCEPTED
                        || a.getStatus() == JobAssignment.AssignmentStatus.COMPLETED)
                .count();
        return AdminOrderSummaryDTO.builder()
                .id(booking.getId())
                .orderType(AdminOrderSummaryDTO.OrderType.CATERING_ORDER)
                .reference(booking.getBookingReference())
                .serviceType("CATERING")
                .eventType(booking.getEventType())
                .eventDate(booking.getEventDate())
                .bookedAt(booking.getCreatedAt())
                .area(booking.getDeliveryAddress())
                .location(booking.getDeliveryAddress())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .acceptedWorkerCount(accepted)
                .customer(customer == null ? null : mapCustomer(customer))
                .build();
    }

    private AdminOrderSummaryDTO mapServiceOrderSummary(ServiceRequest request) {
        User customer = request.getCreatedBy();
        List<StaffingRequest> jobs = staffingRequestRepository.findByServiceRequestIdOrderByCreatedAtAsc(request.getId());
        if (jobs.isEmpty() && "CATERING_STAFF".equalsIgnoreCase(request.getServiceType()) && customer != null) {
            jobs = staffingRequestRepository.findByCreatedByIdOrderByCreatedAtDesc(customer.getId()).stream()
                    .filter(job -> request.getEventType().equals(job.getEventType()))
                    .filter(job -> request.getEventDate().equals(job.getEventDate()))
                    .filter(job -> request.getStartTime().equals(job.getStartTime()))
                    .filter(job -> request.getEndTime().equals(job.getEndTime()))
                    .filter(job -> request.getLocation().equalsIgnoreCase(job.getLocation()))
                    .filter(job -> request.getArea().equalsIgnoreCase(job.getArea()))
                    .toList();
        }
        int accepted = jobs.stream()
                .mapToInt(job -> staffingJobAcceptanceRepository.findByStaffingRequestIdOrderByAcceptedAtDesc(job.getId()).stream()
                        .filter(a -> a.getStatus() == StaffingJobAcceptance.AcceptanceStatus.ACCEPTED
                                || a.getStatus() == StaffingJobAcceptance.AcceptanceStatus.COMPLETED)
                        .toList().size())
                .sum();
        return AdminOrderSummaryDTO.builder()
                .id(request.getId())
                .orderType(AdminOrderSummaryDTO.OrderType.SERVICE_REQUEST)
                .reference("SR-" + request.getId())
                .serviceType(request.getServiceType())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .bookedAt(request.getCreatedAt())
                .area(request.getArea())
                .location(request.getLocation())
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus() == null ? null : request.getStatus().name())
                .acceptedWorkerCount(accepted)
                .customer(customer == null ? null : mapCustomer(customer))
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

        List<JobAssignment> assignments = jobAssignmentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        List<AdminBookingDetailsDTO.AcceptedWorkerDTO> acceptedWorkers = assignments.stream()
                .filter(a -> a.getStatus() == JobAssignment.AssignmentStatus.ACCEPTED
                        || a.getStatus() == JobAssignment.AssignmentStatus.COMPLETED)
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
                .workerAssignments(assignments.stream().map(this::mapAssignment).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminServiceRequestDetailsDTO getServiceRequestDetails(Long serviceRequestId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));
        User customer = request.getCreatedBy();
        List<StaffingRequest> jobs = staffingRequestRepository.findByServiceRequestIdOrderByCreatedAtAsc(serviceRequestId);
        // Backward-compatible fallback for staff requests created before V20 linked staffing jobs to the service request.
        if (jobs.isEmpty() && "CATERING_STAFF".equalsIgnoreCase(request.getServiceType())) {
            jobs = staffingRequestRepository.findByCreatedByIdOrderByCreatedAtDesc(customer.getId()).stream()
                    .filter(job -> request.getEventType().equals(job.getEventType()))
                    .filter(job -> request.getEventDate().equals(job.getEventDate()))
                    .filter(job -> request.getStartTime().equals(job.getStartTime()))
                    .filter(job -> request.getEndTime().equals(job.getEndTime()))
                    .filter(job -> request.getLocation().equalsIgnoreCase(job.getLocation()))
                    .filter(job -> request.getArea().equalsIgnoreCase(job.getArea()))
                    .toList();
        }
        List<AdminServiceRequestDetailsDTO.AcceptedWorkerDTO> acceptedWorkers = jobs.stream()
                .flatMap(job -> staffingJobAcceptanceRepository.findByStaffingRequestIdOrderByAcceptedAtDesc(job.getId()).stream()
                        .map(acceptance -> mapServiceAcceptedWorker(acceptance, job)))
                .toList();

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
                .staffingJobs(jobs.stream().map(this::mapStaffingJobDetails).toList())
                .acceptedWorkers(acceptedWorkers)
                .build();
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
                .workerType(profile.getWorkerType()).profileStatus(profile.getStatus()).assignmentStatus(assignment.getStatus())
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

    private AdminServiceRequestDetailsDTO.StaffingJobDTO mapStaffingJobDetails(StaffingRequest job) {
        return AdminServiceRequestDetailsDTO.StaffingJobDTO.builder()
                .id(job.getId()).workerType(job.getWorkerType()).requiredWorkers(job.getRequiredWorkers())
                .acceptedWorkers(job.getAcceptedWorkers()).remainingPositions(Math.max(0, job.getRequiredWorkers() - job.getAcceptedWorkers()))
                .paymentPerWorker(job.getPayment()).status(job.getStatus()).build();
    }

    private AdminServiceRequestDetailsDTO.AcceptedWorkerDTO mapServiceAcceptedWorker(StaffingJobAcceptance acceptance, StaffingRequest job) {
        WorkerProfile profile = acceptance.getWorkerProfile();
        User user = profile.getUser();
        return AdminServiceRequestDetailsDTO.AcceptedWorkerDTO.builder()
                .acceptanceId(acceptance.getId()).staffingRequestId(job.getId()).workerProfileId(profile.getId()).userId(user.getId())
                .name(fullName(user)).username(user.getUsername()).mobileNumber(user.getPhoneNumber()).email(user.getEmail())
                .workerType(profile.getWorkerType()).profileStatus(profile.getStatus()).acceptanceStatus(acceptance.getStatus())
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
