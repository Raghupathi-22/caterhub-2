package com.daily.cetaring.features.event.entity;

import com.daily.cetaring.features.event.EventStatus;
import com.daily.cetaring.features.event.EventType;
import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_code", nullable = false, unique = true, length = 50)
    private String eventCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false)
    private String location;

    private String city;
    private String area;
    private Double latitude;
    private Double longitude;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "venue_setting", length = 30)
    private String venueSetting;

    @Column(name = "food_preference", length = 30)
    private String foodPreference;

    @Column(name = "food_style", length = 100)
    private String foodStyle;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "estimated_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "total_estimated_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEstimatedCost;

    @Column(name = "total_booked_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBookedAmount;

    @Column(name = "remaining_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBudget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = EventStatus.PLANNING;
        }
        if (estimatedBudget == null) {
            estimatedBudget = BigDecimal.ZERO;
        }
        if (totalEstimatedCost == null) {
            totalEstimatedCost = BigDecimal.ZERO;
        }
        if (totalBookedAmount == null) {
            totalBookedAmount = BigDecimal.ZERO;
        }
        if (remainingBudget == null) {
            remainingBudget = estimatedBudget;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
