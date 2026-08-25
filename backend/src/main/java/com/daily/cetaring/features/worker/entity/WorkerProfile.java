package com.daily.cetaring.features.worker.entity;

import com.daily.cetaring.features.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "worker_profiles")
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

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "total_ratings", nullable = false)
    private Integer totalRatings = 0;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WorkerProfile() {
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public WorkerType getWorkerType() { return workerType; }
    public void setWorkerType(WorkerType workerType) { this.workerType = workerType; }
    public WorkerStatus getStatus() { return status; }
    public void setStatus(WorkerStatus status) { this.status = status; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getPreferredAreas() { return preferredAreas; }
    public void setPreferredAreas(String preferredAreas) { this.preferredAreas = preferredAreas; }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (rating == null) rating = 0.0;
        if (totalRatings == null) totalRatings = 0;
        if (status == null) status = WorkerStatus.PENDING_VERIFICATION;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
