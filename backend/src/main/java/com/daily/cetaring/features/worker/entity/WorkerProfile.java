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
        CATERING,
        CHEF,
        HEAD_CHEF,
        ASSISTANT_CHEF,
        COOK,
        BIRYANI_CHEF,
        TANDOOR_CHEF,
        SWEET_MITHAI_CHEF,
        KITCHEN_HELPER,
        SERVING_STAFF,
        WAITER,
        CATERING_BOY,
        CATERING_GIRL,
        CLEANER,
        CATERING_SUPERVISOR,

        EVENT_DECORATOR,
        WEDDING_DECORATOR,
        STAGE_DECORATOR,
        FLOWER_DECORATOR,
        BALLOON_DECORATOR,
        LIGHTING_TECHNICIAN,
        TENT_SHAMIANA_WORKER,
        STAGE_SETUP_WORKER,
        MANDAP_DECORATOR,
        ENTRANCE_DECORATOR,
        BACKDROP_DECORATOR,
        DECORATION_SUPERVISOR,

        DJ,
        SOUND_TECHNICIAN,
        SINGER,
        MALE_SINGER,
        FEMALE_SINGER,
        BAND_MEMBER,
        BAND_LEADER,
        MELAM_ARTIST,
        BAND_MELAM_ARTIST,
        DANCER,
        DANCE_PERFORMER,
        DANCE_TROUPE,
        ANCHOR,
        MC,
        MAGICIAN,
        FOLK_ARTIST,
        CULTURAL_PERFORMER,

        MAKEUP_ARTIST,
        BRIDAL_MAKEUP_ARTIST,
        GROOM_MAKEUP_ARTIST,
        HAIR_STYLIST,
        MEHENDI_ARTIST,
        SAREE_DRAPIST,
        BEAUTY_SPECIALIST,

        PHOTOGRAPHER,
        WEDDING_PHOTOGRAPHER,
        EVENT_PHOTOGRAPHER,
        VIDEOGRAPHER,
        WEDDING_VIDEOGRAPHER,
        DRONE_OPERATOR,
        PHOTO_EDITOR,
        VIDEO_EDITOR,
        PHOTO_BOOTH_OPERATOR,
        LIVE_STREAMING_OPERATOR,

        PUJARI,
        PRIEST,
        PANDIT,
        POOJA_SPECIALIST,
        HOMAM_SPECIALIST,
        WEDDING_RITUAL_SPECIALIST,

        EVENT_MANAGER,
        EVENT_SUPERVISOR,
        EVENT_COORDINATOR,
        HOST,
        REGISTRATION_STAFF,
        USHER,
        SECURITY_STAFF,
        PARKING_STAFF,
        GENERAL_HELPER,
        CLEANING_STAFF,

        CHAIR_RENTAL,
        TABLE_RENTAL,
        SOFA_RENTAL,
        CROCKERY_RENTAL,
        DINING_EQUIPMENT,
        COOKING_EQUIPMENT,
        GENERATOR_OPERATOR,
        FAN_COOLER_RENTAL,
        TENT_RENTAL,
        STAGE_EQUIPMENT,

        EVENT_DRIVER,
        GUEST_TRANSPORT_DRIVER,
        GOODS_TRANSPORT_DRIVER,
        LOADING_UNLOADING_STAFF,

        INVITATION_DESIGNER,
        CAKE_SPECIALIST,
        RETURN_GIFT_SPECIALIST,
        CUSTOM_EVENT_PROFESSIONAL,

        // Legacy values retained for existing persisted data.
        SERVING_BOY,
        SERVING_GIRL,
        SUPERVISOR,
        LIVE_STREAMER,
        BAND_MELAM,
        ANCHOR_EMCEE,
        KIDS_ENTERTAINER,
        SAREE_DRAPER,
        NAIL_ARTIST,
        STAGE_TENT_SPECIALIST,
        SECURITY_GUARD,
        VALET_DRIVER
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
