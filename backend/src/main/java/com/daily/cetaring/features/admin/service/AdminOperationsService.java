package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminDashboardSummaryDTO;
import com.daily.cetaring.features.admin.dto.EventCreateRequest;
import com.daily.cetaring.features.admin.dto.OfferCreateRequest;
import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import com.daily.cetaring.features.admin.repository.PromotionCampaignRepository;
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

    public Booking updateOrderStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(status.toUpperCase());
        return bookingRepository.save(booking);
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
