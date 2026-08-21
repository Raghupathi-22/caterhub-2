package com.daily.cetaring.features.service.dto;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ServiceRequestDtos {
    private ServiceRequestDtos() {}

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank public String serviceType;
        @NotBlank public String eventType;
        @NotNull public LocalDate eventDate;
        @NotNull public LocalTime startTime;
        @NotBlank public String location;
        @NotBlank public String area;
        public String details;
        @NotNull @DecimalMin("0.00") public BigDecimal totalAmount;
    }

    @Data @Builder
    public static class Response {
        public Long id; public String serviceType; public String eventType; public LocalDate eventDate;
        public LocalTime startTime; public String location; public String area; public String details;
        public BigDecimal totalAmount; public ServiceRequest.Status status; public LocalDateTime createdAt;
    }
}
