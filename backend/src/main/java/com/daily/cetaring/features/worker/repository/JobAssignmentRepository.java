package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.JobAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAssignmentRepository extends JpaRepository<JobAssignment, Long> {

    List<JobAssignment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    List<JobAssignment> findByWorkerProfileIdOrderByCreatedAtDesc(Long workerProfileId);

    List<JobAssignment> findByWorkerProfileIdAndStatusOrderByCreatedAtDesc(
        Long workerProfileId,
        JobAssignment.AssignmentStatus status
    );

    boolean existsByBookingIdAndWorkerProfileId(Long bookingId, Long workerProfileId);
}

