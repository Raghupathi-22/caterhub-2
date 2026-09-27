package com.daily.cetaring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Unified admin order list entry covering both catering orders and service requests. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderSummaryDTO {
    public enum OrderType { CATERING_ORDER, SERVICE_REQUEST }

    private Long id;
    private OrderType orderType;
    private String reference;
    private String serviceType;
    private String eventType;
    private LocalDate eventDate;
    /** Server-side booking timestamp captured automatically when the customer submits the request. */
    private LocalDateTime bookedAt;
    private String area;
    private String location;
    private BigDecimal totalAmount;
    private String status;
    private Integer acceptedWorkerCount;
    private AdminBookingDetailsDTO.CustomerDTO customer;
}
