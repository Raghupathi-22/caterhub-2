package com.daily.cetaring.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates", indexes = {
    @Index(name = "idx_notification_templates_business_id", columnList = "business_id"),
    @Index(name = "idx_notification_templates_channel_type", columnList = "channel_type"),
    @Index(name = "idx_notification_templates_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType = ChannelType.FCM;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "template_variables", length = 500)
    private String templateVariables;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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

    public String interpolateTemplate(java.util.Map<String, String> variables) {
        String result = body;
        if (variables != null) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return result;
    }
}

