package com.daily.cetaring.features.event.repository;

import com.daily.cetaring.features.event.entity.EventRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRequirementRepository extends JpaRepository<EventRequirement, Long> {
    List<EventRequirement> findByEvent_IdOrderByCategoryAscIdAsc(Long eventId);

    Optional<EventRequirement> findByIdAndEvent_Id(Long id, Long eventId);

    boolean existsByEvent_IdAndServiceKey(Long eventId, String serviceKey);
}
