package com.daily.cetaring.features.booking.dto;

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
public class MenuDTO {

    private Long id;
    private Long businessId;
    private String name;
    private String description;
    private String cuisineType;
    private String imageUrl;
    private BigDecimal basePrice;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Integer spicyLevel;
    private Integer preparationTimeMinutes;
    private BigDecimal rating;
    private Integer totalReviews;
    private Boolean isActive;
    private String availableOnDays;
    private LocalDateTime createdAt;
}