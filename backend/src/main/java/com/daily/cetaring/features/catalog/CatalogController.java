package com.daily.cetaring.features.catalog;

import com.daily.cetaring.features.admin.entity.Coupon;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CouponRepository couponRepository;

    @Value
    @Builder
    public static class CatalogResponse {
        List<ServiceCatalog.CategoryDefinition> categories;
    }

    @Value
    @Builder
    public static class PublicOfferResponse {
        Long id;
        String couponCode;
        String description;
        Coupon.DiscountType discountType;
        java.math.BigDecimal discountValue;
        java.math.BigDecimal minOrderValue;
        java.math.BigDecimal maxDiscount;
        java.time.LocalDateTime validFrom;
        java.time.LocalDateTime validUntil;
    }

    @GetMapping
    public CatalogResponse getCatalog() {
        return CatalogResponse.builder()
            .categories(ServiceCatalog.categories())
            .build();
    }

    @GetMapping("/categories")
    public List<ServiceCatalog.CategoryDefinition> getCategories() {
        return ServiceCatalog.categories();
    }

    @GetMapping("/categories/{categoryId}")
    public ServiceCatalog.CategoryDefinition getCategory(@PathVariable String categoryId) {
        ServiceCatalog.CategoryDefinition category = ServiceCatalog.categoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        return category;
    }

    @GetMapping("/offers")
    public List<PublicOfferResponse> getPublicOffers() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository
            .findByIsActiveTrueAndValidFromLessThanEqualAndValidUntilGreaterThanEqualOrderByCreatedAtDesc(now, now)
            .stream()
            .map(coupon -> PublicOfferResponse.builder()
                .id(coupon.getId())
                .couponCode(coupon.getCouponCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscount(coupon.getMaxDiscount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .build())
            .toList();
    }
}
