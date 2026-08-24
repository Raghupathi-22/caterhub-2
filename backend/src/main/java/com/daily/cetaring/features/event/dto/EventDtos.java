package com.daily.cetaring.features.event.dto;

import com.daily.cetaring.features.event.EventGroup;
import com.daily.cetaring.features.event.EventStatus;
import com.daily.cetaring.features.event.EventType;
import com.daily.cetaring.features.event.RequirementStatus;
import com.daily.cetaring.features.event.RequirementUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class EventDtos {
    private EventDtos() {
    }

    @Data
    public static class EventTypeResponse {
        private EventType code;
        private String displayName;
        private EventGroup group;
    }

    @Data
    public static class EventTypeGroupResponse {
        private EventGroup group;
        private List<EventTypeResponse> types;
    }

    @Data
    public static class ChecklistPreviewRequest {
        @NotBlank
        private String eventType;
        private Integer guestCount;
        private BigDecimal budget;
        private String poojaKind;
        private String ageGroup;
    }

    @Data
    public static class CreateEventRequest {
        @NotBlank
        private String eventType;
        private String eventName;
        @NotNull
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        @NotBlank
        private String location;
        private String city;
        private String area;
        private Double latitude;
        private Double longitude;
        @NotNull
        @Min(1)
        private Integer guestCount;
        @NotNull
        @DecimalMin("0.0")
        private BigDecimal estimatedBudget;
        private String venueSetting;
        private String foodPreference;
        private String foodStyle;
        private String specialRequirements;
        private String notes;
        private String poojaKind;
        private String ageGroup;
        private List<SelectedRequirement> selectedServices;
        private List<CustomRequirement> customRequirements;
    }

    @Data
    public static class UpdateEventRequest {
        private String eventName;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String city;
        private String area;
        private Integer guestCount;
        private BigDecimal estimatedBudget;
        private String notes;
        private EventStatus status;
    }

    @Data
    public static class SelectedRequirement {
        private String serviceKey;
        private Boolean selected;
        private Integer quantity;
        private BigDecimal customerBudget;
        private String notes;
    }

    @Data
    public static class CustomRequirement {
        @NotBlank
        private String category;
        @NotBlank
        private String serviceName;
        private String description;
        private Integer quantity;
        private RequirementUnit unit;
        private BigDecimal customerBudget;
        private String notes;
    }

    @Data
    public static class UpdateRequirementRequest {
        private Integer quantity;
        private BigDecimal customerBudget;
        private String notes;
        private Boolean selected;
        private RequirementStatus status;
    }

    @Data
    public static class BookRequirementRequest {
        @NotNull
        private Long providerId;
        private String packageName;
        private BigDecimal amount;
        private String notes;
    }

    @Data
    public static class StaffRequirementRequest {
        @NotNull
        private Integer requiredWorkers;
        private BigDecimal payment;
        private String notes;
    }

    @Data
    @Builder
    public static class EventSummary {
        private Long id;
        private String eventCode;
        private EventType eventType;
        private String eventName;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String city;
        private Integer guestCount;
        private EventStatus status;
        private BigDecimal estimatedBudget;
        private BigDecimal totalEstimatedCost;
        private BigDecimal totalBookedAmount;
        private BigDecimal remainingBudget;
        private int bookedRequired;
        private int requiredCount;
        private int selectedCount;
        private boolean overBudget;
    }

    @Data
    @Builder
    public static class RequirementResponse {
        private Long id;
        private String category;
        private String serviceKey;
        private String serviceName;
        private String description;
        private Integer quantity;
        private RequirementUnit unit;
        private boolean required;
        private BigDecimal estimatedBudget;
        private BigDecimal customerBudget;
        private BigDecimal actualBookedAmount;
        private RequirementStatus status;
        private Long vendorId;
        private Long bookingId;
        private Long staffingRequestId;
        private Integer confirmedWorkers;
        private Integer remainingWorkers;
        private String notes;
    }

    @Data
    @Builder
    public static class TimelineResponse {
        private Long id;
        private String title;
        private String detail;
        private LocalDateTime occurredAt;
    }

    @Data
    @Builder
    public static class EventDashboard {
        private EventSummary event;
        private List<RequirementResponse> requirements;
        private List<TimelineResponse> timeline;
        private String budgetWarning;
    }

    @Data
    public static class ProviderSearchRequest {
        private String city;
        private String sort;
        private Boolean verifiedOnly;
        private Boolean vegetarian;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
        private int page = 0;
        private int size = 20;
    }

    @Data
    @Builder
    public static class ProviderResponse {
        private Long id;
        private String name;
        private String description;
        private String logoUrl;
        private String city;
        private String area;
        private BigDecimal rating;
        private Integer totalReviews;
        private Boolean verified;
        private Integer minCapacity;
        private Integer maxCapacity;
        private Integer serviceRadiusKm;
        private BigDecimal pricePerUnit;
        private String serviceCategory;
        private Integer completedEvents;
        private boolean eligible;
        private String ineligibilityReason;
    }
}
