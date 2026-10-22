package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StaffingJobAcceptanceRepository extends JpaRepository<StaffingJobAcceptance, Long> {
    boolean existsByStaffingRequestIdAndWorkerProfileId(Long staffingRequestId, Long workerProfileId);

    List<StaffingJobAcceptance> findByWorkerProfileIdOrderByAcceptedAtDesc(Long workerProfileId);

    List<StaffingJobAcceptance> findByStaffingRequestIdOrderByAcceptedAtDesc(Long staffingRequestId);

    /** Admin view: every acceptance for the given jobs with worker profile and user loaded in one query. */
    @Query("select a from StaffingJobAcceptance a join fetch a.staffingRequest s join fetch a.workerProfile w join fetch w.user u "
        + "where s.id in :jobIds order by a.acceptedAt desc, a.id desc")
    List<StaffingJobAcceptance> findWithWorkersByStaffingRequestIds(@Param("jobIds") Collection<Long> jobIds);

    /**
     * Admin counts: rows of [staffingRequestId, count of distinct workers] for the given statuses.
     * The (staffing_request_id, worker_profile_id) unique key already prevents duplicates; distinct keeps it robust.
     */
    @Query("select a.staffingRequest.id, count(distinct a.workerProfile.id) from StaffingJobAcceptance a "
        + "where a.staffingRequest.id in :jobIds and a.status in :statuses group by a.staffingRequest.id")
    List<Object[]> countWorkersByStaffingRequestIds(
        @Param("jobIds") Collection<Long> jobIds,
        @Param("statuses") Collection<StaffingJobAcceptance.AcceptanceStatus> statuses
    );
}
