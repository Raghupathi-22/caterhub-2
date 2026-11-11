package com.daily.cetaring.features.service.dto;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ServiceRequestDtos {
    private ServiceRequestDtos() {}

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank @Size(max = 30) public String serviceType;
        @NotBlank @Size(max = 100) public String eventType;
        @NotNull public LocalDate eventDate;
        @NotNull public LocalTime startTime;
        @NotNull public LocalTime endTime;
        @NotBlank @Size(max = 255, message = "Event address must be 255 characters or fewer.") public String location;
        @NotBlank @Size(max = 100, message = "Area must be 100 characters or fewer.") public String area;
        @NotNull @Size(min = 1, max = 50) public List<@NotBlank @Size(max = 150) String> selectedServices;
        @Size(max = 2000, message = "Instructions must be 2000 characters or fewer.") public String instructions;
        @Size(max = 4000) public String details;
        public Boolean quoteBased;
        @NotNull @DecimalMin("0.00") public BigDecimal totalAmount;
        /** Optional: worker jobs created atomically with the request (replaces separate client calls). */
        @Size(max = 20) public List<@NotNull @Valid StaffingLine> staffing;

        public CreateRequest(String serviceType, String eventType, LocalDate eventDate, LocalTime startTime,
                             LocalTime endTime, String location, String area, List<String> selectedServices,
                             String instructions, String details, Boolean quoteBased, BigDecimal totalAmount) {
            this(serviceType, eventType, eventDate, startTime, endTime, location, area, selectedServices,
                instructions, details, quoteBased, totalAmount, null);
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateCateringStaffRequest {
        @NotBlank @Size(max = 100) public String eventType;
        @NotNull public LocalDate eventDate;
        @NotNull public LocalTime startTime;
        @NotNull public LocalTime endTime;
        @NotBlank @Size(max = 255, message = "Event address must be 255 characters or fewer.") public String location;
        @NotBlank @Size(max = 100, message = "Area must be 100 characters or fewer.") public String area;
        @NotNull @Size(min = 1, max = 50) public List<@NotBlank @Size(max = 150) String> selectedServices;
        @Size(max = 2000, message = "Instructions must be 2000 characters or fewer.") public String instructions;
        @Size(max = 4000) public String details;
        @NotNull @DecimalMin("0.00") public BigDecimal totalAmount;
        @NotNull @Size(min = 1, max = 20) public List<@NotNull @Valid StaffingLine> staffing;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StaffingLine {
        @NotNull public com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType workerType;
        @NotNull @Min(1) @Max(1000) public Integer requiredWorkers;
        @NotNull @DecimalMin(value = "0.01") public BigDecimal payment;
    }

    @Data @Builder
    public static class Response {
        public Long id; public String serviceType; public String eventType; public LocalDate eventDate;
        public LocalTime startTime; public LocalTime endTime; public String location; public String area;
        public List<String> selectedServices; public String instructions; public String details;
        public Boolean quoteBased; public BigDecimal totalAmount; public ServiceRequest.Status status;
        public LocalDateTime createdAt; public LocalDateTime updatedAt;
    }
}
