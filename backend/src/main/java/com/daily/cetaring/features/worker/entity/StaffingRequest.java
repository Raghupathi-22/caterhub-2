package com.daily.cetaring.features.worker.entity;

import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "staffing_requests", indexes = {
    @Index(name = "idx_staffing_requests_status_role", columnList = "status, worker_type"),
    @Index(name = "idx_staffing_requests_area", columnList = "area"),
    @Index(name = "idx_staffing_requests_event_date", columnList = "event_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffingRequest {
    public enum StaffingStatus {
        PENDING,
        OPEN,
        FILLED,
        CANCELLED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_staffing_requests_created_by"))
    private User createdBy;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "worker_type", nullable = false, length = 64)
    private WorkerProfile.WorkerType workerType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 100)
    private String area;

    @Column(name = "required_workers", nullable = false)
    private Integer requiredWorkers;

    @Column(name = "accepted_workers", nullable = false)
    private Integer acceptedWorkers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal payment;

    @Column(name = "additional_requirements", columnDefinition = "TEXT")
    private String additionalRequirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (acceptedWorkers == null) acceptedWorkers = 0;
        if (status == null) status = StaffingStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
