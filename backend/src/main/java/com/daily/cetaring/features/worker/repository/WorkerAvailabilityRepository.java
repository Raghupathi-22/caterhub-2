package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.WorkerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkerAvailabilityRepository extends JpaRepository<WorkerAvailability, Long> {

    List<WorkerAvailability> findByWorkerProfileIdOrderByAvailableDateAscStartTimeAsc(Long workerProfileId);

    List<WorkerAvailability> findByWorkerProfileIdAndAvailableDateBetweenOrderByAvailableDateAscStartTimeAsc(
        Long workerProfileId,
        LocalDate startDate,
        LocalDate endDate
    );
}

