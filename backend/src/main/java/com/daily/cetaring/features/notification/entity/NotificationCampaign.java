package com.daily.cetaring.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_campaigns", indexes = {
    @Index(name = "idx_notification_campaigns_business_id", columnList = "business_id"),
    @Index(name = "idx_notification_campaigns_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "campaign_name", nullable = false, length = 255)
    private String campaignName;

    @Column(name = "campaign_type", length = 100)
    private String campaignType;

    @Column(name = "target_audience", length = 500)
    private String targetAudience;

    @Column(name = "template_id")
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType = ChannelType.FCM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "total_recipients", nullable = false)
    private Integer totalRecipients = 0;

    @Column(name = "sent_count", nullable = false)
    private Integer sentCount = 0;

    @Column(name = "delivered_count", nullable = false)
    private Integer deliveredCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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

    public enum ChannelType {
        FCM,
        EMAIL,
        SMS,
        IN_APP,
        WHATSAPP
    }

    public enum CampaignStatus {
        DRAFT,      // Draft
        SCHEDULED,  // Scheduled
        SENDING,    // Sending in progress
        SENT,       // Sent
        PAUSED,     // Paused
        CANCELLED   // Cancelled
    }

    public Double getDeliveryRate() {
        if (sentCount == 0) {
            return 0.0;
        }
        return (deliveredCount * 100.0) / sentCount;
    }

    public Boolean isSending() {
        return status == CampaignStatus.SENDING;
    }

    public Boolean isCompleted() {
        return status == CampaignStatus.SENT || status == CampaignStatus.CANCELLED;
    }
}

