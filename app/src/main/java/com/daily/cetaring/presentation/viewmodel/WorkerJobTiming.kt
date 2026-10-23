package com.daily.cetaring.presentation.viewmodel

import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Event dates/times are Hyderabad wall-clock values. The backend is authoritative
 * (via `expired` / `upcoming` flags); these helpers are a fallback for older responses.
 */
object WorkerJobTiming {
    val BUSINESS_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    fun nowInBusinessZone(): LocalDateTime = ZonedDateTime.now(BUSINESS_ZONE).toLocalDateTime()

    /** True when the event has started (start <= now). Unparseable dates are treated as not started. */
    fun hasStarted(eventDate: String?, startTime: String?, now: LocalDateTime): Boolean {
        val date = runCatching { LocalDate.parse(eventDate?.trim().orEmpty()) }.getOrNull() ?: return false
        val time = runCatching { LocalTime.parse(startTime?.trim().orEmpty()) }.getOrNull() ?: LocalTime.MIDNIGHT
        return !LocalDateTime.of(date, time).isAfter(now)
    }

    fun isAcceptable(job: StaffingJobResponse, now: LocalDateTime): Boolean =
        job.expired != true && !hasStarted(job.eventDate, job.startTime, now)

    fun availableJobs(jobs: List<StaffingJobResponse>, now: LocalDateTime): List<StaffingJobResponse> =
        jobs.filter { isAcceptable(it, now) }

    fun isUpcoming(job: WorkerJobResponse, now: LocalDateTime): Boolean =
        (job.status == "ACCEPTED" || job.status == "OFFERED") && (job.upcoming ?: !hasStarted(job.eventDate, job.startTime, now))

    /** Prefers the server-computed list; falls back to client-side filtering of [myJobs]. */
    fun upcomingJobs(
        serverUpcoming: List<WorkerJobResponse>?,
        myJobs: List<WorkerJobResponse>,
        now: LocalDateTime
    ): List<WorkerJobResponse> =
        (serverUpcoming ?: myJobs)
            .filter { isUpcoming(it, now) }
            .sortedWith(compareBy({ it.eventDate }, { it.startTime }))
}
