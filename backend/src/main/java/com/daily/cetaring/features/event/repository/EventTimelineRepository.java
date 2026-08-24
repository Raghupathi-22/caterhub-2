package com.daily.cetaring.features.event.repository;

import com.daily.cetaring.features.event.entity.EventTimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventTimelineRepository extends JpaRepository<EventTimelineEntry, Long> {
    List<EventTimelineEntry> findByEvent_IdOrderByOccurredAtAsc(Long eventId);
}
