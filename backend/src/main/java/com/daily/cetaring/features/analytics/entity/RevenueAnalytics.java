package com.daily.cetaring.features.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "revenue_analytics", indexes = {
    @Index(name = "idx_revenue_analytics_business_id", columnList = "business_id"),
    @Index(name = "idx_revenue_analytics_date", columnList = "analytics_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_revenue_analytics", columnNames = {"business_id", "analytics_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    @Column(name = "gross_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossRevenue;

    @Column(name = "commissions_paid", precision = 10, scale = 2)
    private BigDecimal commissionsPaid;

    @Column(name = "refunds_issued", precision = 10, scale = 2)
    private BigDecimal refundsIssued;

    @Column(name = "net_revenue", precision = 10, scale = 2)
    private BigDecimal netRevenue;

    @Column(name = "payment_method_breakdown", columnDefinition = "JSON")
    private String paymentMethodBreakdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateNetRevenue();
    }

    public void calculateNetRevenue() {
        BigDecimal commissions = commissionsPaid != null ? commissionsPaid : BigDecimal.ZERO;
        BigDecimal refunds = refundsIssued != null ? refundsIssued : BigDecimal.ZERO;
        netRevenue = grossRevenue.subtract(commissions).subtract(refunds);
    }

    public Double getCommissionPercentage() {
        if (grossRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return (commissionsPaid.doubleValue() * 100.0) / grossRevenue.doubleValue();
    }

    public Double getRefundPercentage() {
        if (grossRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return (refundsIssued.doubleValue() * 100.0) / grossRevenue.doubleValue();
    }
}

