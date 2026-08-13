package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "promotion_campaigns", indexes = {
    @Index(name = "idx_promotion_campaigns_business_id", columnList = "business_id"),
    @Index(name = "idx_promotion_campaigns_status", columnList = "status"),
    @Index(name = "idx_promotion_campaigns_start_date", columnList = "start_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "campaign_name", nullable = false, length = 255)
    private String campaignName;

    @Column(name = "campaign_description", columnDefinition = "TEXT")
    private String campaignDescription;

    @Column(name = "campaign_type", length = 100)
    private String campaignType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "target_audience", length = 255)
    private String targetAudience;

    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(precision = 10, scale = 2)
    private BigDecimal spent = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum CampaignStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }
        if (status == null) {
            status = CampaignStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
