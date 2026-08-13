package com.daily.cetaring.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notification_preferences_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "fcm_enabled", nullable = false)
    private Boolean fcmEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "order_notifications", nullable = false)
    private Boolean orderNotifications = true;

    @Column(name = "payment_notifications", nullable = false)
    private Boolean paymentNotifications = true;

    @Column(name = "promotional_notifications", nullable = false)
    private Boolean promotionalNotifications = false;

    @Column(name = "system_notifications", nullable = false)
    private Boolean systemNotifications = true;

    @Column(name = "marketing_notifications", nullable = false)
    private Boolean marketingNotifications = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(length = 50)
    private String timezone = "UTC";

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

    public Boolean shouldNotify(NotificationType type) {
        return switch (type) {
            case ORDER -> orderNotifications;
            case PAYMENT -> paymentNotifications;
            case PROMOTIONAL -> promotionalNotifications;
            case SYSTEM -> systemNotifications;
            case MARKETING -> marketingNotifications;
        };
    }

    public Boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return !now.isBefore(quietHoursStart) && !now.isAfter(quietHoursEnd);
        } else {
            return !now.isBefore(quietHoursStart) || !now.isAfter(quietHoursEnd);
        }
    }

    public enum NotificationType {
        ORDER,
        PAYMENT,
        PROMOTIONAL,
        SYSTEM,
        MARKETING
    }
}

