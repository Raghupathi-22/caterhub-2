package com.daily.cetaring.features.rice.entity;

import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rice_sample_requests",
    uniqueConstraints = @UniqueConstraint(name = "uk_rice_samples_idempotency", columnNames = {"customer_user_id", "idempotency_key"}),
    indexes = {
        @Index(name = "idx_rice_samples_status", columnList = "status"),
        @Index(name = "idx_rice_samples_created", columnList = "created_at")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RiceSampleRequest {
    public enum Status { REQUESTED, PICKUP_SCHEDULED, COLLECTED, CANCELLED }
    public static final String FULFILLMENT_PICKUP = "PICKUP";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, length = 30)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private RiceProduct product;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;
    @Column(name = "sample_weight_grams", nullable = false)
    private Integer sampleWeightGrams;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal price;
    @Column(name = "fulfillment_type", nullable = false, length = 20)
    private String fulfillmentType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Status status;
    @Column(name = "pickup_location", columnDefinition = "TEXT")
    private String pickupLocation;
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;
    @Column(name = "pickup_instructions", columnDefinition = "TEXT")
    private String pickupInstructions;
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void create() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = Status.REQUESTED;
        enforceFreePickup();
    }

    @PreUpdate void update() {
        updatedAt = LocalDateTime.now();
        enforceFreePickup();
    }

    /** Samples are always free and pickup-only, whatever the caller set. */
    private void enforceFreePickup() {
        price = BigDecimal.ZERO.setScale(2);
        fulfillmentType = FULFILLMENT_PICKUP;
    }
}
