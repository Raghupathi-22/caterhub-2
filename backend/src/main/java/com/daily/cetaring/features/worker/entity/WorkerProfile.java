package com.daily.cetaring.features.worker.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "worker_profiles", indexes = {
    @Index(name = "idx_worker_profiles_user_id", columnList = "user_id"),
    @Index(name = "idx_worker_profiles_worker_type", columnList = "worker_type"),
    @Index(name = "idx_worker_profiles_status", columnList = "status"),
    @Index(name = "idx_worker_profiles_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

    public enum WorkerType {
        CHEF,
        ASSISTANT_CHEF,
        SERVING_BOY,
        SERVING_GIRL,
        CLEANER,
        KITCHEN_HELPER,
        SUPERVISOR
    }

    public enum WorkerStatus {
        PENDING_VERIFICATION,
        ACTIVE,
        SUSPENDED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_worker_profiles_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "worker_type", nullable = false, length = 30)
    private WorkerType workerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkerStatus status;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "preferred_areas", columnDefinition = "TEXT")
    private String preferredAreas;

    @Column(columnDefinition = "TEXT")
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_ratings", nullable = false)
    private Integer totalRatings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", foreignKey = @ForeignKey(name = "fk_worker_profiles_approved_by"))
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

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
        if (status == null) {
            status = WorkerStatus.PENDING_VERIFICATION;
        }
        if (experienceYears == null) {
            experienceYears = 0;
        }
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }
        if (totalRatings == null) {
            totalRatings = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
