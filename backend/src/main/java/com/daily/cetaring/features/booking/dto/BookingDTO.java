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
public class BookingDTO {

    private Long id;
    private Long businessId;
    private Long userId;
    private String bookingReference;
    private String eventType;
    private Integer guestCount;
    private String mealType;
    private LocalDateTime eventDateTime;
    private String deliveryAddress;
    private String specialInstructions;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
}