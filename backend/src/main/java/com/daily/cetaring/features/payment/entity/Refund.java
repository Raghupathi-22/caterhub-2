package com.daily.cetaring.features.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refunds_payment_id", columnList = "payment_id"),
    @Index(name = "idx_refunds_booking_id", columnList = "booking_id"),
    @Index(name = "idx_refunds_status", columnList = "status"),
    @Index(name = "idx_refunds_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refund_reference", nullable = false, unique = true, length = 100)
    private String refundReference;

    @Column(name = "razorpay_refund_id", length = 100)
    private String razorpayRefundId;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type")
    private RefundType refundType = RefundType.FULL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_to")
    private RefundMethod refundTo = RefundMethod.ORIGINAL_METHOD;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

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

    public enum RefundType {
        FULL,           // Full refund
        PARTIAL,        // Partial refund
        CANCELLATION    // Cancellation refund
    }

    public enum RefundStatus {
        INITIATED,      // Refund initiated
        PROCESSING,     // Being processed
        COMPLETED,      // Completed
        FAILED,         // Failed
        REJECTED        // Rejected
    }

    public enum RefundMethod {
        ORIGINAL_METHOD,  // Back to original payment method
        WALLET,          // To wallet
        BANK_TRANSFER    // Direct bank transfer
    }

    public Boolean isProcessed() {
        return status == RefundStatus.COMPLETED;
    }

    public Boolean isRefunding() {
        return status == RefundStatus.INITIATED || status == RefundStatus.PROCESSING;
    }
}

