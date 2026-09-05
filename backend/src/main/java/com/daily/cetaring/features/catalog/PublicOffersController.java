package com.daily.cetaring.features.catalog;

import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class PublicOffersController {

    private final CouponRepository couponRepository;

    @Value
    @Builder
    public static class PublicOfferResponse {
        Long id;
        String title;
        String description;
        String applicableCategory;
        LocalDateTime validFrom;
        LocalDateTime validUntil;
        String ctaLabel;
    }

    @GetMapping("/active")
    public List<PublicOfferResponse> getActiveOffers() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByIsActiveTrueAndValidFromLessThanEqualAndValidUntilGreaterThanEqualOrderByValidUntilAsc(now, now)
            .stream()
            .map(offer -> PublicOfferResponse.builder()
                .id(offer.getId())
                .title(offer.getCouponCode())
                .description(offer.getDescription())
                .applicableCategory("All services")
                .validFrom(offer.getValidFrom())
                .validUntil(offer.getValidUntil())
                .ctaLabel(resolveCtaLabel(offer))
                .build())
            .toList();
    }

    private String resolveCtaLabel(Coupon offer) {
        return switch (offer.getDiscountType()) {
            case PERCENTAGE -> "Claim Percentage Offer";
            case FLAT_AMOUNT -> "Claim Flat Discount";
            case FREE_DELIVERY -> "Claim Free Delivery";
            case BUY_ONE_GET_ONE -> "Claim BOGO Offer";
        };
    }
}

