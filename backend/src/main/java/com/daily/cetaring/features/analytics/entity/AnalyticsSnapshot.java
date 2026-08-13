package com.daily.cetaring.features.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "analytics_snapshots", indexes = {
    @Index(name = "idx_analytics_snapshots_business_id", columnList = "business_id"),
    @Index(name = "idx_analytics_snapshots_snapshot_date", columnList = "snapshot_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_analytics_snapshots", columnNames = {"business_id", "snapshot_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_customers", nullable = false)
    private Integer totalCustomers = 0;

    @Column(name = "repeat_customers")
    private Integer repeatCustomers = 0;

    @Column(name = "new_customers")
    private Integer newCustomers = 0;

    @Column(name = "average_order_value", precision = 10, scale = 2)
    private BigDecimal averageOrderValue = BigDecimal.ZERO;

    @Column(name = "pending_orders")
    private Integer pendingOrders = 0;

    @Column(name = "completed_orders")
    private Integer completedOrders = 0;

    @Column(name = "cancelled_orders")
    private Integer cancelledOrders = 0;

    @Column(name = "total_refunds", precision = 10, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

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

    public Double getConversionRate() {
        if (totalOrders == 0) {
            return 0.0;
        }
        return (completedOrders * 100.0) / totalOrders;
    }

    public Double getCancellationRate() {
        if (totalOrders == 0) {
            return 0.0;
        }
        return (cancelledOrders * 100.0) / totalOrders;
    }

    public Double getRepeatCustomerPercentage() {
        if (totalCustomers == 0) {
            return 0.0;
        }
        return (repeatCustomers * 100.0) / totalCustomers;
    }
}

