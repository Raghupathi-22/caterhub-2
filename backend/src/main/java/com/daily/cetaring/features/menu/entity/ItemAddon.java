package com.daily.cetaring.features.menu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "item_addons", indexes = {
    @Index(name = "idx_item_addons_menu_item_id", columnList = "menu_item_id"),
    @Index(name = "idx_item_addons_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "addon_name", nullable = false, length = 255)
    private String addonName;

    @Column(name = "addon_description", columnDefinition = "TEXT")
    private String addonDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_vegetarian", nullable = false)
    private Boolean isVegetarian = false;

    @Column(name = "is_vegan", nullable = false)
    private Boolean isVegan = false;

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
}

