package com.daily.cetaring.features.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "delivery_zones", indexes = {
    @Index(name = "idx_delivery_zones_business_id", columnList = "business_id"),
    @Index(name = "idx_delivery_zones_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "zone_name", nullable = false, length = 255)
    private String zoneName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "radius_km", precision = 5, scale = 2)
    private BigDecimal radiusKm;

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

    public Boolean isLocationWithinZone(BigDecimal lat, BigDecimal lng) {
        if (latitude == null || longitude == null || radiusKm == null) {
            return false;
        }

        // Simple distance calculation (Haversine formula would be more accurate)
        double latDiff = latitude.subtract(lat).doubleValue();
        double lngDiff = longitude.subtract(lng).doubleValue();
        double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff) * 111; // Rough km conversion

        return distance <= radiusKm.doubleValue();
    }
}

