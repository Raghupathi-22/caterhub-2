package com.daily.cetaring.features.event.repository;

import com.daily.cetaring.features.event.entity.BusinessServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessServiceOfferingRepository extends JpaRepository<BusinessServiceOffering, Long> {
    List<BusinessServiceOffering> findByServiceKeyAndEnabledTrue(String serviceKey);
}
