package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class WorkerJobTimingTest {
    private val now = LocalDateTime.of(2026, 10, 22, 10, 0)

    @Test
    fun pastAndAlreadyStartedJobsAreNotAcceptable() {
        assertFalse(WorkerJobTiming.isAcceptable(job(1, "2026-09-27", "18:00"), now))
        assertFalse(WorkerJobTiming.isAcceptable(job(2, "2026-10-22", "09:30:00"), now))
        assertFalse(WorkerJobTiming.isAcceptable(job(3, "2026-10-22", "10:00"), now))
        assertTrue(WorkerJobTiming.isAcceptable(job(4, "2026-10-22", "10:30"), now))
        assertTrue(WorkerJobTiming.isAcceptable(job(5, "2026-12-01", "08:00"), now))
    }

    @Test
    fun serverExpiredFlagWins() {
        assertFalse(WorkerJobTiming.isAcceptable(job(1, "2026-12-01", "08:00", expired = true), now))
        val listed = WorkerJobTiming.availableJobs(
            listOf(job(1, "2026-09-27", "18:00"), job(2, "2026-12-01", "08:00"), job(3, "2026-12-01", "08:00", expired = true)),
            now
        )
        assertEquals(listOf(2L), listed.map { it.id })
    }

    @Test
    fun upcomingContainsOnlyFutureAcceptedJobsSorted() {
        val myJobs = listOf(
            myJob(1, "2026-09-27", "18:00", "ACCEPTED"),
            myJob(2, "2026-12-01", "08:00", "ACCEPTED"),
            myJob(3, "2026-11-01", "08:00", "ACCEPTED"),
            myJob(4, "2026-12-05", "08:00", "COMPLETED"),
            myJob(5, "2026-12-06", "08:00", "CANCELLED")
        )
        assertEquals(listOf(3L, 2L), WorkerJobTiming.upcomingJobs(null, myJobs, now).map { it.jobId })
    }

    @Test
    fun serverUpcomingFlagIsRespected() {
        val server = listOf(myJob(7, "2026-12-01", "08:00", "ACCEPTED", upcoming = true))
        assertEquals(listOf(7L), WorkerJobTiming.upcomingJobs(server, emptyList(), now).map { it.jobId })
        val stale = listOf(myJob(8, "2026-12-01", "08:00", "ACCEPTED", upcoming = false))
        assertTrue(WorkerJobTiming.upcomingJobs(null, stale, now).isEmpty())
    }

    private fun job(id: Long, date: String, start: String, expired: Boolean? = null) = StaffingJobResponse(
        id = id, eventType = "Wedding", workerType = WorkerType.CHEF, eventDate = date, startTime = start,
        endTime = "23:00", location = "Venue", area = "Madhapur", requiredWorkers = 2, acceptedWorkers = 0,
        remainingPositions = 2, payment = BigDecimal.TEN, additionalRequirements = null, status = "OPEN",
        expired = expired
    )

    private fun myJob(id: Long, date: String, start: String, status: String, upcoming: Boolean? = null) = WorkerJobResponse(
        acceptanceId = id, jobId = id, eventType = "Wedding", workerType = WorkerType.CHEF, eventDate = date,
        startTime = start, endTime = "23:00", location = "Venue", area = "Madhapur", payment = BigDecimal.TEN,
        status = status, acceptedAt = null, upcoming = upcoming
    )
}
