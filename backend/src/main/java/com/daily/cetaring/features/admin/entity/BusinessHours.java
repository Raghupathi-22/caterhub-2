package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_hours", indexes = {
    @Index(name = "idx_business_hours_business_id", columnList = "business_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_business_hours", columnNames = {"business_id", "day_of_week"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // 0=Sunday, 1=Monday, etc.

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDayName() {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return dayOfWeek >= 0 && dayOfWeek < 7 ? days[dayOfWeek] : "Unknown";
    }
}

