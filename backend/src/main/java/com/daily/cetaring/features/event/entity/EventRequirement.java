package com.daily.cetaring.features.event.entity;

import com.daily.cetaring.features.event.RequirementStatus;
import com.daily.cetaring.features.event.RequirementUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "service_key", nullable = false, length = 80)
    private String serviceKey;

    @Column(name = "service_name", nullable = false, length = 150)
    private String serviceName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequirementUnit unit;

    @Column(name = "required_flag", nullable = false)
    private boolean requiredFlag;

    @Column(name = "estimated_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "customer_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal customerBudget;

    @Column(name = "actual_booked_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal actualBookedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequirementStatus status;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "staffing_request_id")
    private Long staffingRequestId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (quantity == null) {
            quantity = 1;
        }
        if (unit == null) {
            unit = RequirementUnit.ITEM;
        }
        if (status == null) {
            status = requiredFlag ? RequirementStatus.SELECTED : RequirementStatus.NOT_SELECTED;
        }
        if (estimatedBudget == null) {
            estimatedBudget = BigDecimal.ZERO;
        }
        if (customerBudget == null) {
            customerBudget = estimatedBudget;
        }
        if (actualBookedAmount == null) {
            actualBookedAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
