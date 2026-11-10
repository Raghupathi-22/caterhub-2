package com.daily.cetaring.features.rice.repository;

import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RiceSampleRequestRepository extends JpaRepository<RiceSampleRequest, Long> {
    Optional<RiceSampleRequest> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    Optional<RiceSampleRequest> findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(
        Long customerId, Collection<RiceSampleRequest.Status> statuses);

    List<RiceSampleRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** {@code search} must already be lower-cased and wrapped in {@code %} wildcards, or null. */
    @Query("""
        SELECT s FROM RiceSampleRequest s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:fromDate IS NULL OR s.createdAt >= :fromDate)
          AND (:toDate IS NULL OR s.createdAt < :toDate)
          AND (:search IS NULL
               OR LOWER(s.customerName) LIKE :search
               OR LOWER(s.mobileNumber) LIKE :search
               OR LOWER(s.requestNumber) LIKE :search)
        ORDER BY s.createdAt DESC, s.id DESC
        """)
    List<RiceSampleRequest> search(
        @Param("status") RiceSampleRequest.Status status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("search") String search
    );
}
