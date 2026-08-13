package com.daily.cetaring.features.menu.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "item_customizations", indexes = {
    @Index(name = "idx_item_customizations_menu_item_id", columnList = "menu_item_id"),
    @Index(name = "idx_item_customizations_type", columnList = "customization_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCustomization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "customization_name", nullable = false, length = 255)
    private String customizationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "customization_type", nullable = false, length = 20)
    private CustomizationType customizationType = CustomizationType.SELECTION;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum CustomizationType {
        SELECTION,
        MULTIPLE,
        ADDON,
        SIZE
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}