package com.daily.cetaring.features.rice.repository;

import com.daily.cetaring.features.rice.entity.RiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RiceOrderRepository extends JpaRepository<RiceOrder, Long> {
    Optional<RiceOrder> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    List<RiceOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<RiceOrder> findByIdAndCustomerId(Long id, Long customerId);

    /** {@code search} must already be lower-cased and wrapped in {@code %} wildcards, or null. */
    @Query("""
        SELECT o FROM RiceOrder o
        WHERE (:status IS NULL OR o.status = :status)
          AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
          AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (:toDate IS NULL OR o.createdAt < :toDate)
          AND (:search IS NULL
               OR LOWER(o.customerName) LIKE :search
               OR LOWER(o.mobileNumber) LIKE :search
               OR LOWER(o.orderNumber) LIKE :search
               OR LOWER(o.area) LIKE :search
               OR LOWER(o.pincode) LIKE :search)
        ORDER BY o.createdAt DESC, o.id DESC
        """)
    List<RiceOrder> search(
        @Param("status") RiceOrder.Status status,
        @Param("paymentStatus") RiceOrder.PaymentStatus paymentStatus,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("search") String search
    );
}
