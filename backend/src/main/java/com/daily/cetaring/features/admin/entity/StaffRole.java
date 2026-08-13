package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_roles", indexes = {
    @Index(name = "idx_staff_roles_business_id", columnList = "business_id"),
    @Index(name = "idx_staff_roles_is_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_staff_roles", columnNames = {"business_id", "role_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_description", columnDefinition = "TEXT")
    private String roleDescription;

    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

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

    public Boolean canBeDeleted() {
        return !isSystemRole;
    }

    public Boolean canBeModified() {
        return !isSystemRole;
    }
}

