package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {

    Optional<WorkerProfile> findByUserIdAndDeletedAtIsNull(Long userId);

    List<WorkerProfile> findByWorkerTypeAndStatusAndDeletedAtIsNull(
        WorkerProfile.WorkerType workerType,
        WorkerProfile.WorkerStatus status
    );

    List<WorkerProfile> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(WorkerProfile.WorkerStatus status);

    List<WorkerProfile> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<WorkerProfile> findByWorkerTypeAndDeletedAtIsNullOrderByCreatedAtDesc(WorkerProfile.WorkerType workerType);

    List<WorkerProfile> findByWorkerTypeAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
        WorkerProfile.WorkerType workerType,
        WorkerProfile.WorkerStatus status
    );

    boolean existsByUserIdAndDeletedAtIsNull(Long userId);
}
