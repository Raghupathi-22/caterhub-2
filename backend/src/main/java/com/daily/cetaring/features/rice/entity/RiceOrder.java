package com.daily.cetaring.features.rice.entity;

import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rice_orders",
    uniqueConstraints = @UniqueConstraint(name = "uk_rice_orders_idempotency", columnNames = {"customer_user_id", "idempotency_key"}),
    indexes = {
        @Index(name = "idx_rice_orders_status", columnList = "status"),
        @Index(name = "idx_rice_orders_payment_status", columnList = "payment_status"),
        @Index(name = "idx_rice_orders_created", columnList = "created_at")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RiceOrder {
    public enum Status { PENDING, CONFIRMED, PROCESSING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED }
    public enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, length = 30)
    private String orderNumber;

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

    @Column(name = "quantity_kg", nullable = false)
    private Integer quantityKg;
    @Column(name = "base_pack_kg", nullable = false)
    private Integer basePackKg;
    @Column(name = "base_pack_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal basePackPrice;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;
    @Column(name = "delivery_charge", precision = 12, scale = 2)
    private BigDecimal deliveryCharge;
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "delivery_address", columnDefinition = "TEXT", nullable = false)
    private String deliveryAddress;
    @Column(nullable = false, length = 100)
    private String area;
    @Column(nullable = false, length = 10)
    private String pincode;
    @Column(name = "preferred_delivery_date")
    private LocalDate preferredDeliveryDate;
    @Column(name = "preferred_delivery_slot", length = 30)
    private String preferredDeliverySlot;
    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Status status;
    @Enumerated(EnumType.STRING) @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;
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
        if (status == null) status = Status.PENDING;
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
    }

    @PreUpdate void update() { updatedAt = LocalDateTime.now(); }
}
