package com.daily.cetaring.features.service.repository;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();
    List<ServiceRequest> findByCreatedByUsernameOrderByCreatedAtDesc(String username);
    List<ServiceRequest> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    java.util.Optional<ServiceRequest> findByIdAndCreatedById(Long id, Long userId);

    /** Admin feed: all service requests with the customer loaded in the same query (no per-row lookups). */
    @org.springframework.data.jpa.repository.Query("select r from ServiceRequest r join fetch r.createdBy order by r.createdAt desc")
    List<ServiceRequest> findAllWithCustomerOrderByCreatedAtDesc();
}
