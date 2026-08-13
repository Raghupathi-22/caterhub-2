package com.daily.cetaring.features.admin.dto;

import com.daily.cetaring.features.admin.entity.Coupon;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferCreateRequest {

    @NotNull
    private Long businessId;

    @NotBlank
    private String couponCode;

    @NotBlank
    private String description;

    @NotNull
    private Coupon.DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscount;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validUntil;
}
