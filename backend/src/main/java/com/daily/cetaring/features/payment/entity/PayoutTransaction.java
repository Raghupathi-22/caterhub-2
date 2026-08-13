package com.daily.cetaring.features.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "payout_transactions", indexes = {
    @Index(name = "idx_payout_transactions_business_id", columnList = "business_id"),
    @Index(name = "idx_payout_transactions_status", columnList = "status"),
    @Index(name = "idx_payout_transactions_payout_date", columnList = "payout_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "payout_reference", nullable = false, unique = true, length = 100)
    private String payoutReference;

    @Column(name = "payout_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "payout_date")
    private LocalDate payoutDate;

    @Column(name = "payout_method", length = 50)
    private String payoutMethod;

    @Column(name = "bank_account_id", length = 100)
    private String bankAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status = PayoutStatus.INITIATED;

    @Column(length = 3)
    private String currency = "INR";

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public enum PayoutStatus {
        INITIATED,     // Payout initiated
        PROCESSING,    // Being processed
        COMPLETED,     // Completed
        FAILED,        // Failed
        REJECTED       // Rejected
    }

    public Boolean isCompleted() {
        return status == PayoutStatus.COMPLETED;
    }

    public Boolean isProcessing() {
        return status == PayoutStatus.INITIATED || status == PayoutStatus.PROCESSING;
    }

    public BigDecimal calculateNetAmount() {
        if (payoutAmount != null && commissionAmount != null) {
            return payoutAmount.subtract(commissionAmount);
        }
        return payoutAmount != null ? payoutAmount : BigDecimal.ZERO;
    }
}

