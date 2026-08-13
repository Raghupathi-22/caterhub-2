package com.daily.cetaring.features.worker.entity;

import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_assignments", indexes = {
    @Index(name = "idx_job_assignments_booking", columnList = "booking_id"),
    @Index(name = "idx_job_assignments_worker_status", columnList = "worker_profile_id, status"),
    @Index(name = "idx_job_assignments_status", columnList = "status"),
    @Index(name = "idx_job_assignments_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAssignment {

    public enum AssignmentStatus {
        OFFERED,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_assignments_booking"))
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_assignments_worker_profile"))
    private WorkerProfile workerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false, foreignKey = @ForeignKey(name = "fk_job_assignments_assigned_by"))
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "worker_type", nullable = false, length = 30)
    private WorkerProfile.WorkerType workerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "offered_at", nullable = false)
    private LocalDateTime offeredAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "decline_reason", length = 500)
    private String declineReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (offeredAt == null) {
            offeredAt = now;
        }
        if (status == null) {
            status = AssignmentStatus.OFFERED;
        }
        if (workerType == null && workerProfile != null) {
            workerType = workerProfile.getWorkerType();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

