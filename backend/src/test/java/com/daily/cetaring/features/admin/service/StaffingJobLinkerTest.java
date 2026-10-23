package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.shared.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffingJobLinkerTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Test
    void explicitLinkWinsAndIsNeverReattributed() {
        ServiceRequest a = request(1L, 10L, T0);
        StaffingRequest linked = job(100L, 10L, 1L, T0);
        StaffingRequest linkedElsewhere = job(101L, 10L, 999L, T0);

        StaffingJobLinker.LinkResult result = StaffingJobLinker.link(List.of(a), List.of(linked, linkedElsewhere));

        assertEquals(List.of(100L), ids(result.jobsFor(1L)));
        assertTrue(result.unmatchedJobs().isEmpty(), "a job linked to another request is not shown as standalone either");
    }

    @Test
    void unlinkedJobMatchesSameCustomerAndEventFieldsIgnoringCaseAndWhitespace() {
        ServiceRequest a = request(1L, 10L, T0);
        StaffingRequest unlinked = job(100L, 10L, null, T0.plusSeconds(1));
        unlinked.setLocation("  kondapur hall ");
        unlinked.setArea("KONDAPUR");

        assertEquals(List.of(100L), ids(StaffingJobLinker.link(List.of(a), List.of(unlinked)).jobsFor(1L)));
    }

    @Test
    void unlinkedJobOfAnotherCustomerOrDifferentEventIsUnmatched() {
        ServiceRequest a = request(1L, 10L, T0);
        StaffingRequest otherCustomer = job(100L, 11L, null, T0);
        StaffingRequest otherDate = job(101L, 10L, null, T0);
        otherDate.setEventDate(otherDate.getEventDate().plusDays(1));
        StaffingRequest otherStart = job(102L, 10L, null, T0);
        otherStart.setStartTime(LocalTime.of(9, 0));

        StaffingJobLinker.LinkResult result = StaffingJobLinker.link(List.of(a), List.of(otherCustomer, otherDate, otherStart));

        assertTrue(result.jobsFor(1L).isEmpty());
        assertEquals(List.of(100L, 101L, 102L), ids(result.unmatchedJobs()));
    }

    @Test
    void identicalRequestsDoNotDoubleCountAJob() {
        ServiceRequest first = request(1L, 10L, T0);
        ServiceRequest second = request(2L, 10L, T0.plusHours(2));
        StaffingRequest postedWithSecond = job(100L, 10L, null, T0.plusHours(2).plusSeconds(1));
        StaffingRequest postedWithFirst = job(101L, 10L, null, T0.minusSeconds(1));

        StaffingJobLinker.LinkResult result = StaffingJobLinker.link(List.of(first, second), List.of(postedWithSecond, postedWithFirst));

        assertEquals(List.of(101L), ids(result.jobsFor(1L)));
        assertEquals(List.of(100L), ids(result.jobsFor(2L)));
    }

    private static List<Long> ids(List<StaffingRequest> jobs) {
        return jobs.stream().map(StaffingRequest::getId).toList();
    }

    private static User user(Long id) {
        return User.builder().id(id).username("u" + id).build();
    }

    private static ServiceRequest request(Long id, Long customerId, LocalDateTime createdAt) {
        return ServiceRequest.builder().id(id).createdBy(user(customerId)).serviceType("DECORATION").eventType("Wedding")
            .eventDate(LocalDate.of(2026, 12, 5)).startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(23, 0))
            .location("Kondapur Hall").area("Kondapur").createdAt(createdAt).build();
    }

    private static StaffingRequest job(Long id, Long creatorId, Long serviceRequestId, LocalDateTime createdAt) {
        return StaffingRequest.builder().id(id).createdBy(user(creatorId)).serviceRequestId(serviceRequestId).eventType("wedding")
            .workerType(WorkerProfile.WorkerType.EVENT_DECORATOR).eventDate(LocalDate.of(2026, 12, 5))
            .startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(23, 0)).location("Kondapur Hall").area("Kondapur")
            .requiredWorkers(3).acceptedWorkers(0).createdAt(createdAt).build();
    }
}
