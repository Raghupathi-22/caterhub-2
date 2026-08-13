package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_dashboards", indexes = {
    @Index(name = "idx_admin_dashboards_business_id", columnList = "business_id"),
    @Index(name = "idx_admin_dashboards_user_id", columnList = "user_id"),
    @Index(name = "idx_admin_dashboards_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dashboard_name", nullable = false, length = 255)
    private String dashboardName;

    @Enumerated(EnumType.STRING)
    @Column(name = "dashboard_type")
    private DashboardType dashboardType = DashboardType.DEFAULT;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

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

    public enum DashboardType {
        DEFAULT,        // Standard dashboard
        CUSTOM,         // User-customized
        ANALYTICS,      // Analytics-focused
        OPERATIONS      // Operations-focused
    }
}

