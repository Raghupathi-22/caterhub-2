package com.daily.cetaring.features.menu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredient_inventory", indexes = {
    @Index(name = "idx_ingredient_inventory_business_id", columnList = "business_id"),
    @Index(name = "idx_ingredient_inventory_is_active", columnList = "is_active"),
    @Index(name = "idx_ingredient_inventory_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "ingredient_name", nullable = false, length = 255)
    private String ingredientName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(name = "min_stock_level", precision = 10, scale = 2)
    private BigDecimal minStockLevel;

    @Column(name = "max_stock_level", precision = 10, scale = 2)
    private BigDecimal maxStockLevel;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

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

    public Boolean isLowStock() {
        return quantity.compareTo(minStockLevel) <= 0;
    }

    public Boolean isOverstocked() {
        return quantity.compareTo(maxStockLevel) >= 0;
    }

    public Boolean isExpiredOrExpiring() {
        return expiresAt != null && expiresAt.isBefore(LocalDate.now().plusDays(7));
    }
}

