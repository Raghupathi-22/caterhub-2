package com.daily.cetaring.features.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MenuItemDTO {

    private Long id;

    @JsonProperty("menu_id")
    private Long menuId;

    @JsonProperty("category_id")
    private Long categoryId;

    private String name;

    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("is_vegetarian")
    private Boolean isVegetarian;

    @JsonProperty("is_vegan")
    private Boolean isVegan;

    @JsonProperty("spicy_level")
    private Integer spicyLevel;

    @JsonProperty("preparation_time_minutes")
    private Integer preparationTimeMinutes;

    private BigDecimal rating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("available_on_days")
    private String availableOnDays;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

