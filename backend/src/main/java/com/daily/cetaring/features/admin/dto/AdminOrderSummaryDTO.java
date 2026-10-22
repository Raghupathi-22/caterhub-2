package com.daily.cetaring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Unified admin order list entry covering both catering orders and service requests. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderSummaryDTO {
    /** STAFFING_JOB rows are open/accepted worker jobs that are not attributable to any customer service request. */
    public enum OrderType { CATERING_ORDER, SERVICE_REQUEST, STAFFING_JOB }

    private Long id;
    private OrderType orderType;
    private String reference;
    private String serviceType;
    /** Human-readable service category (from the catalog) when known. */
    private String serviceCategory;
    private String eventType;
    /** Scheduled event date (Asia/Kolkata wall-clock). Not the booking timestamp. */
    private LocalDate eventDate;
    /** Scheduled event start time, when the record has one. */
    private LocalTime startTime;
    private LocalTime endTime;
    /** Server-side booking timestamp captured automatically when the customer submits the request. */
    private LocalDateTime bookedAt;
    private String area;
    private String location;
    private BigDecimal totalAmount;
    private String status;
    /** Distinct workers with an ACCEPTED/COMPLETED acceptance (or assignment for catering orders). */
    private Integer acceptedWorkerCount;
    /** Total positions requested across the linked worker jobs; null when the record has no requested count. */
    private Integer requestedWorkerCount;
    /** Worker roles requested by the linked jobs (worker type identifiers). */
    private List<String> requestedRoles;
    private AdminBookingDetailsDTO.CustomerDTO customer;
}
