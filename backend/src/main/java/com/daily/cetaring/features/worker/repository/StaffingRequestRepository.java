package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
    List<StaffingRequest> findAllByOrderByCreatedAtDesc();

    List<StaffingRequest> findByServiceRequestIdOrderByCreatedAtAsc(Long serviceRequestId);

    List<StaffingRequest> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    List<StaffingRequest> findByStatusOrderByEventDateAscStartTimeAsc(StaffingRequest.StaffingStatus status);

    List<StaffingRequest> findByStatusAndWorkerTypeOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        WorkerProfile.WorkerType workerType
    );

    List<StaffingRequest> findByStatusAndWorkerTypeInOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        Collection<WorkerProfile.WorkerType> workerTypes
    );

    // Offerable-job queries: exclude events dated before today (business zone). Same-day jobs whose
    // start time has passed are removed by WorkerService using the business clock.
    List<StaffingRequest> findByStatusAndEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        LocalDate fromDate
    );

    List<StaffingRequest> findByStatusAndWorkerTypeAndEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        WorkerProfile.WorkerType workerType,
        LocalDate fromDate
    );

    List<StaffingRequest> findByStatusAndWorkerTypeInAndEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(
        StaffingRequest.StaffingStatus status,
        Collection<WorkerProfile.WorkerType> workerTypes,
        LocalDate fromDate
    );

    List<StaffingRequest> findByServiceRequestIdIn(Collection<Long> serviceRequestIds);

    List<StaffingRequest> findByServiceRequestIdIsNullAndCreatedByIdIn(Collection<Long> createdByIds);

    List<StaffingRequest> findByServiceRequestIdIsNullOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StaffingRequest s where s.id = :id")
    Optional<StaffingRequest> findByIdForUpdate(@Param("id") Long id);
}

