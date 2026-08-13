package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_coupon_code", columnList = "coupon_code"),
    @Index(name = "idx_coupons_business_id", columnList = "business_id"),
    @Index(name = "idx_coupons_is_active", columnList = "is_active"),
    @Index(name = "idx_coupons_valid_until", columnList = "valid_until")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType = DiscountType.PERCENTAGE;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "max_usage_per_user")
    private Integer maxUsagePerUser;

    @Column(name = "max_total_usage")
    private Integer maxTotalUsage;

    @Column(name = "current_usage", nullable = false)
    private Integer currentUsage = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DiscountType {
        PERCENTAGE,         // % discount
        FLAT_AMOUNT,       // Fixed amount
        FREE_DELIVERY,     // Free delivery
        BUY_ONE_GET_ONE    // BOGO
    }

    public Boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
               !now.isBefore(validFrom) &&
               !now.isAfter(validUntil) &&
               (maxTotalUsage == null || currentUsage < maxTotalUsage);
    }

    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (!isValid() || minOrderValue != null && orderTotal.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        switch (discountType) {
            case PERCENTAGE:
                discount = orderTotal.multiply(discountValue).divide(new BigDecimal(100));
                break;
            case FLAT_AMOUNT:
                discount = discountValue;
                break;
            case FREE_DELIVERY:
                // Handled separately
                discount = BigDecimal.ZERO;
                break;
            case BUY_ONE_GET_ONE:
                // Handled separately
                discount = BigDecimal.ZERO;
                break;
        }

        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }

        return discount;
    }
}

