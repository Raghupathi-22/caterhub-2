package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.JobAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    /** Admin view: non-deleted assignments of a booking with worker profile and user loaded in one query. */
    @Query("select a from JobAssignment a join fetch a.workerProfile w join fetch w.user u "
        + "where a.booking.id = :bookingId and a.deletedAt is null order by a.createdAt desc, a.id desc")
    List<JobAssignment> findActiveWithWorkersByBookingId(@Param("bookingId") Long bookingId);

    /** Admin counts: rows of [bookingId, count of distinct workers] for non-deleted assignments in the given statuses. */
    @Query("select a.booking.id, count(distinct a.workerProfile.id) from JobAssignment a "
        + "where a.booking.id in :bookingIds and a.deletedAt is null and a.status in :statuses group by a.booking.id")
    List<Object[]> countWorkersByBookingIds(
        @Param("bookingIds") Collection<Long> bookingIds,
        @Param("statuses") Collection<JobAssignment.AssignmentStatus> statuses
    );
}
