package com.daily.cetaring.features.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
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
public class CreateMyBookingRequest {
    private Long businessId;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @NotBlank(message = "Meal type is required")
    private String mealType;

    @NotNull(message = "Event date time is required")
    @Future(message = "Event date time must be in the future")
    private LocalDateTime eventDateTime;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String specialInstructions;

    @NotNull(message = "Estimated amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated amount must be greater than 0")
    private BigDecimal estimatedAmount;
}

