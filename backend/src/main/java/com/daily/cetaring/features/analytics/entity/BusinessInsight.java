package com.daily.cetaring.features.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_insights", indexes = {
    @Index(name = "idx_business_insights_business_id", columnList = "business_id"),
    @Index(name = "idx_business_insights_type", columnList = "insight_type"),
    @Index(name = "idx_business_insights_priority", columnList = "priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "insight_type", nullable = false, length = 100)
    private String insightType;  // TREND, OPPORTUNITY, ALERT, RECOMMENDATION

    @Column(name = "insight_title", nullable = false, length = 255)
    private String insightTitle;

    @Column(name = "insight_description", columnDefinition = "TEXT")
    private String insightDescription;

    @Column(name = "insight_value", length = 255)
    private String insightValue;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "priority", length = 50)
    private String priority;  // LOW, MEDIUM, HIGH, CRITICAL

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

    public enum InsightType {
        TREND,           // Business trend
        OPPORTUNITY,     // Growth opportunity
        ALERT,           // Alert/Warning
        RECOMMENDATION   // Recommendation
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public Boolean isUrgent() {
        return "HIGH".equals(priority) || "CRITICAL".equals(priority);
    }
}

