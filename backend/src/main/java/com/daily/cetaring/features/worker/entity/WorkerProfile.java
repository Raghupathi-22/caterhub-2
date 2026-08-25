package com.daily.cetaring.features.worker.entity;

import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "worker_profiles")
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
        SUPERVISOR,

        PUJARI,

        PHOTOGRAPHER,
        VIDEOGRAPHER,
        LIVE_STREAMER,

        DJ,
        BAND_MELAM,
        SINGER,
        DANCER,
        ANCHOR_EMCEE,
        MAGICIAN,
        KIDS_ENTERTAINER,

        MAKEUP_ARTIST,
        BRIDAL_MAKEUP_ARTIST,
        MEHENDI_ARTIST,
        HAIR_STYLIST,
        SAREE_DRAPER,
        NAIL_ARTIST,

        EVENT_DECORATOR,
        FLOWER_DECORATOR,
        LIGHTING_TECHNICIAN,
        SOUND_TECHNICIAN,
        STAGE_TENT_SPECIALIST,

        SECURITY_GUARD,
        VALET_DRIVER,
        EVENT_COORDINATOR
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
    @Column(name = "worker_type", nullable = false, length = 40)
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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_ratings", nullable = false)
    private Integer totalRatings = 0;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (rating == null) rating = BigDecimal.ZERO;
        if (totalRatings == null) totalRatings = 0;
        if (status == null) status = WorkerStatus.PENDING_VERIFICATION;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
