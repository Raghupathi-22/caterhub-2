package com.daily.cetaring.features.rice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rice_products")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RiceProduct {
    public static final String VILLAGE_FARM_RICE = "VILLAGE_FARM_RICE";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 255)
    private String tagline;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String highlights;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(nullable = false)
    private Boolean active;

    @Column(name = "base_pack_kg", nullable = false)
    private Integer basePackKg;
    @Column(name = "base_pack_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal basePackPrice;
    @Column(name = "min_order_kg", nullable = false)
    private Integer minOrderKg;
    @Column(name = "order_step_kg", nullable = false)
    private Integer orderStepKg;
    @Column(name = "max_order_kg", nullable = false)
    private Integer maxOrderKg;
    /** Null means delivery charges are confirmed by the team after ordering. */
    @Column(name = "delivery_charge", precision = 12, scale = 2)
    private BigDecimal deliveryCharge;
    @Column(name = "payment_note", columnDefinition = "TEXT")
    private String paymentNote;

    @Column(name = "sample_enabled", nullable = false)
    private Boolean sampleEnabled;
    @Column(name = "sample_weight_grams", nullable = false)
    private Integer sampleWeightGrams;
    @Column(name = "sample_pickup_location", columnDefinition = "TEXT")
    private String samplePickupLocation;
    @Column(name = "sample_pickup_instructions", columnDefinition = "TEXT")
    private String samplePickupInstructions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void create() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (sampleEnabled == null) sampleEnabled = true;
    }

    @PreUpdate void update() { updatedAt = LocalDateTime.now(); }
}
