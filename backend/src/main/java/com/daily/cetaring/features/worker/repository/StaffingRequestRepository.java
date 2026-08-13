package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
    List<StaffingRequest> findByStatusOrderByEventDateAscStartTimeAsc(StaffingRequest.StaffingStatus status);

    List<StaffingRequest> findByStatusAndWorkerTypeOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        WorkerProfile.WorkerType workerType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StaffingRequest s where s.id = :id")
    Optional<StaffingRequest> findByIdForUpdate(@Param("id") Long id);
}

