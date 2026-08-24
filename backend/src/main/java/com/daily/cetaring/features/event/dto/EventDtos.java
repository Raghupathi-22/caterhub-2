package com.daily.cetaring.features.event.dto;

import com.daily.cetaring.features.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EventDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateEventRequest {
        @NotBlank(message = "Event name is required")
        private String name;

        @NotNull(message = "Event type is required")
        private EventType eventType;

        @NotNull(message = "Event date is required")
        @FutureOrPresent(message = "Event date cannot be in the past")
        private LocalDate eventDate;

        private LocalTime startTime;
        private LocalTime endTime;

        private String location;

        private Double latitude;
        private Double longitude;

        private Integer guestCount;

        private BigDecimal budget;

        private String eventCode; // optional override
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String eventCode;
        private String name;
        private EventType eventType;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private Integer guestCount;
        private BigDecimal budget;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkspaceResponse {
        private Long id;
        private String eventCode;
        private String name;
        private EventType eventType;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private Integer guestCount;
        private BigDecimal budget;
        private BigDecimal totalPlanned;
        private BigDecimal totalBooked;
        private List<Requirement> checklist;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Requirement {
        private Long id;
        private String category;
        private BigDecimal plannedAmount;
        private BigDecimal bookedAmount;
        private boolean requiredFlag;
    }
}
