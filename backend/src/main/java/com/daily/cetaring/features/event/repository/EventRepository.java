package com.daily.cetaring.features.event.repository;

import com.daily.cetaring.features.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCustomer_IdOrderByEventDateDesc(Long customerId);

    Optional<Event> findByIdAndCustomer_Id(Long id, Long customerId);

    long countByEventCodeStartingWith(String prefix);
}
