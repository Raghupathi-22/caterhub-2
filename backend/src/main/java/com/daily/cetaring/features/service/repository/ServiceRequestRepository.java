package com.daily.cetaring.features.service.repository;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();
    List<ServiceRequest> findByCreatedByUsernameOrderByCreatedAtDesc(String username);
}
