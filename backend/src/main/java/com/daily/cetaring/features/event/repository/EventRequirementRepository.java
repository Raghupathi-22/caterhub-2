package com.daily.cetaring.features.event.repository;

import com.daily.cetaring.features.event.EventRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRequirementRepository extends JpaRepository<EventRequirement, Long> {
    List<EventRequirement> findByEventId(Long eventId);
}
