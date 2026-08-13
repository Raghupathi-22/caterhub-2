package com.daily.cetaring.features.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_wallet_transactions_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType = TransactionType.DEBIT;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        CREDIT,        // Credit to wallet
        DEBIT,         // Debit from wallet
        REFUND,        // Refund to wallet
        BONUS          // Bonus credit
    }

    public Boolean isCredit() {
        return transactionType == TransactionType.CREDIT ||
               transactionType == TransactionType.REFUND ||
               transactionType == TransactionType.BONUS;
    }

    public Boolean isDebit() {
        return transactionType == TransactionType.DEBIT;
    }

    public BigDecimal getNetAmount() {
        return isCredit() ? amount : amount.negate();
    }
}

