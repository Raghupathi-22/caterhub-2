package com.daily.cetaring.features.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "report_schedules", indexes = {
    @Index(name = "idx_report_schedules_business_id", columnList = "business_id"),
    @Index(name = "idx_report_schedules_next_scheduled_at", columnList = "next_scheduled_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "schedule_name", nullable = false, length = 255)
    private String scheduleName;

    @Column(name = "frequency", length = 50)
    private String frequency;  // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    @Column(name = "day_of_week")
    private Integer dayOfWeek;  // 0=Sunday, 6=Saturday

    @Column(name = "day_of_month")
    private Integer dayOfMonth;  // 1-31

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "recipients_email", columnDefinition = "TEXT")
    private String recipientsEmail;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;

    @Column(name = "next_scheduled_at")
    private LocalDateTime nextScheduledAt;

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

    public Boolean isDue() {
        if (!isActive || nextScheduledAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(nextScheduledAt);
    }

    public void markAsGenerated() {
        lastGeneratedAt = LocalDateTime.now();
        calculateNextScheduleTime();
    }

    public void calculateNextScheduleTime() {
        if ("DAILY".equals(frequency)) {
            nextScheduledAt = LocalDateTime.now().plusDays(1).with(scheduledTime);
        } else if ("WEEKLY".equals(frequency)) {
            nextScheduledAt = LocalDateTime.now().plusWeeks(1).with(scheduledTime);
        } else if ("MONTHLY".equals(frequency)) {
            nextScheduledAt = LocalDateTime.now().plusMonths(1).with(scheduledTime);
        } else if ("QUARTERLY".equals(frequency)) {
            nextScheduledAt = LocalDateTime.now().plusMonths(3).with(scheduledTime);
        } else if ("YEARLY".equals(frequency)) {
            nextScheduledAt = LocalDateTime.now().plusYears(1).with(scheduledTime);
        }
    }
}

