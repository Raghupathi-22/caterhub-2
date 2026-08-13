package com.daily.cetaring.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_business_id", columnList = "business_id"),
    @Index(name = "idx_notifications_status", columnList = "status"),
    @Index(name = "idx_notifications_channel_type", columnList = "channel_type"),
    @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "notification_type", nullable = false, length = 100)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType = ChannelType.FCM;

    @Column(name = "recipient_address", length = 500)
    private String recipientAddress;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(columnDefinition = "JSON")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

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
        FCM,        // Firebase Cloud Messaging
        EMAIL,      // Email
        SMS,        // SMS
        IN_APP,     // In-app
        WHATSAPP    // WhatsApp
    }

    public enum NotificationStatus {
        PENDING,    // Pending
        SENT,       // Sent
        DELIVERED,  // Delivered
        FAILED,     // Failed
        CANCELLED   // Cancelled
    }

    public Boolean isDelivered() {
        return status == NotificationStatus.DELIVERED;
    }

    public Boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    public Boolean canRetry() {
        return retryCount < maxRetries && status == NotificationStatus.FAILED;
    }
}

