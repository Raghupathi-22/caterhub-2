package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffingJobAcceptanceRepository extends JpaRepository<StaffingJobAcceptance, Long> {
    boolean existsByStaffingRequestIdAndWorkerProfileId(Long staffingRequestId, Long workerProfileId);

    List<StaffingJobAcceptance> findByWorkerProfileIdOrderByAcceptedAtDesc(Long workerProfileId);

    List<StaffingJobAcceptance> findByStaffingRequestIdOrderByAcceptedAtDesc(Long staffingRequestId);
}

