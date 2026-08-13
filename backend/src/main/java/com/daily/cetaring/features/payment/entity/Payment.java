package com.daily.cetaring.features.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_booking_id", columnList = "booking_id"),
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
    @Index(name = "idx_payments_business_id", columnList = "business_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_payment_reference", columnList = "payment_reference"),
    @Index(name = "idx_payments_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "gateway_id", nullable = false)
    private Long gatewayId;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 100)
    private String paymentReference;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "INR";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType = PaymentType.FULL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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

    public enum PaymentType {
        ADVANCE,       // Advance payment
        BALANCE,       // Balance payment
        FULL,          // Full payment
        ONLINE         // Online payment
    }

    public enum PaymentStatus {
        INITIATED,     // Payment initiated
        PENDING,       // Awaiting verification
        PROCESSING,    // Being processed
        COMPLETED,     // Successfully completed
        FAILED,        // Payment failed
        REFUNDED       // Refunded
    }

    public Boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    public Boolean isPending() {
        return status == PaymentStatus.INITIATED || status == PaymentStatus.PENDING;
    }
}

