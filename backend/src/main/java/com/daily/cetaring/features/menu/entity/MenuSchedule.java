package com.daily.cetaring.features.menu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "menu_schedules", indexes = {
    @Index(name = "idx_menu_schedules_menu_id", columnList = "menu_id"),
    @Index(name = "idx_menu_schedules_start_date", columnList = "start_date"),
    @Index(name = "idx_menu_schedules_end_date", columnList = "end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type")
    private ScheduleType scheduleType = ScheduleType.DAILY;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;  // 0=Sunday, 1=Monday, etc.

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(length = 255)
    private String description;

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

    public enum ScheduleType {
        DAILY,      // Available every day
        WEEKLY,     // Specific day of week
        MONTHLY,    // Specific date each month
        CUSTOM      // Custom date range
    }

    public Boolean isAvailableNow() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!isAvailable) {
            return false;
        }

        switch (scheduleType) {
            case DAILY:
                return startTime != null && endTime != null &&
                       !now.isBefore(startTime) && !now.isAfter(endTime);

            case WEEKLY:
                int currentDayOfWeek = today.getDayOfWeek().getValue() % 7;
                return this.dayOfWeek != null && currentDayOfWeek == this.dayOfWeek &&
                       startTime != null && endTime != null &&
                       !now.isBefore(startTime) && !now.isAfter(endTime);

            case MONTHLY:
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                return startDate != null && dayOfMonth == startDate.getDayOfMonth() &&
                       startTime != null && endTime != null &&
                       !now.isBefore(startTime) && !now.isAfter(endTime);

            case CUSTOM:
                return startDate != null && endDate != null &&
                       (today.isEqual(startDate) || today.isAfter(startDate)) &&
                       (today.isEqual(endDate) || today.isBefore(endDate)) &&
                       startTime != null && endTime != null &&
                       !now.isBefore(startTime) && !now.isAfter(endTime);

            default:
                return false;
        }
    }
}
