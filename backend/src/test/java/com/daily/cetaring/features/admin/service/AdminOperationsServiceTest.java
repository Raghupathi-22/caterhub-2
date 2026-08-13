package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminDashboardSummaryDTO;
import com.daily.cetaring.features.admin.dto.OfferCreateRequest;
import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import com.daily.cetaring.features.admin.repository.PromotionCampaignRepository;
import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOperationsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private PromotionCampaignRepository promotionCampaignRepository;

    @InjectMocks
    private AdminOperationsService adminOperationsService;

    @Test
    void shouldBuildDashboardSummary() {
        Booking pending = Booking.builder().status("PENDING").totalAmount(new BigDecimal("1000")).build();
        Booking delivered = Booking.builder().status("DELIVERED").totalAmount(new BigDecimal("2000")).build();
        Booking cancelled = Booking.builder().status("CANCELLED").totalAmount(new BigDecimal("1500")).build();

        when(bookingRepository.findByBusinessId(1L)).thenReturn(List.of(pending, delivered, cancelled));

        AdminDashboardSummaryDTO summary = adminOperationsService.getDashboardSummary(1L);

        assertEquals(3L, summary.getTotalOrders());
        assertEquals(1L, summary.getPendingOrders());
        assertEquals(1L, summary.getDeliveredOrders());
        assertEquals(1L, summary.getCancelledOrders());
        assertEquals(new BigDecimal("2000"), summary.getTotalRevenue());
        assertEquals(new BigDecimal("1500.00"), summary.getAverageOrderValue());
    }

    @Test
    void shouldRejectOfferWhenDateRangeIsInvalid() {
        OfferCreateRequest request = OfferCreateRequest.builder()
                .businessId(1L)
                .couponCode("SAVE10")
                .description("Offer")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .validFrom(LocalDateTime.now().plusDays(2))
                .validUntil(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> adminOperationsService.createOffer(request));
    }
}
