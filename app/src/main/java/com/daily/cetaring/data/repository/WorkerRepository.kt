package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.WorkerApiService
import com.daily.cetaring.data.remote.dto.AcceptStaffingJobResponse
import com.daily.cetaring.data.remote.dto.AssignmentResponse
import com.daily.cetaring.data.remote.dto.AssignmentStatus
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.CreateStaffingRequest
import com.daily.cetaring.data.remote.dto.RespondAssignmentRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.UpdateAvailabilityToggleRequest
import com.daily.cetaring.data.remote.dto.WorkerDashboardResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerType
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException

class WorkerRepository(
    private val workerApiService: WorkerApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun getMyProfile(): WorkerProfileResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getMyProfile(bearerToken()) }
    }

    suspend fun createMyProfile(request: CreateWorkerProfileRequest): WorkerProfileResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.createMyProfile(bearerToken(), request) }
    }

    suspend fun getAssignmentsForWorker(profileId: Long): List<AssignmentResponse> {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getAssignmentsForWorker(bearerToken(), profileId) }
    }

    suspend fun acceptAssignment(assignmentId: Long): AssignmentResponse {
        ensureBackendAvailable()
        return executeNetworkCall {
            workerApiService.respondToAssignment(
                bearerToken(),
                assignmentId,
                RespondAssignmentRequest(status = AssignmentStatus.ACCEPTED)
            )
        }
    }

    suspend fun getDashboard(): WorkerDashboardResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getDashboard(bearerToken()) }
    }

    suspend fun getAvailableJobs(role: WorkerType? = null, area: String? = null, search: String? = null): List<StaffingJobResponse> {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getAvailableJobs(bearerToken(), role, area, search) }
    }

    suspend fun getJob(jobId: Long): StaffingJobResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getJob(bearerToken(), jobId) }
    }

    suspend fun acceptJob(jobId: Long): AcceptStaffingJobResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.acceptJob(bearerToken(), jobId) }
    }

    suspend fun getMyJobs(): List<WorkerJobResponse> {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.getMyJobs(bearerToken()) }
    }

    suspend fun updateAvailability(available: Boolean) {
        ensureBackendAvailable()
        executeNetworkCall { workerApiService.updateAvailability(bearerToken(), UpdateAvailabilityToggleRequest(available = available)) }
    }

    suspend fun createStaffingRequest(request: CreateStaffingRequest): StaffingJobResponse {
        ensureBackendAvailable()
        return executeNetworkCall { workerApiService.createStaffingRequest(bearerToken(), request) }
    }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Please login again")
        }
        return "Bearer $token"
    }

    private suspend fun ensureBackendAvailable() {
        try {
            val health = executeNetworkCall { healthApiService.health() }
            if (!health.isUp) {
                throw ServerOfflineException()
            }
        } catch (exception: ServerOfflineException) {
            throw exception
        } catch (exception: Exception) {
            throw mapNetworkException(exception)
        }
    }

    private suspend fun <T> executeNetworkCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (exception: Exception) {
            throw mapNetworkException(exception)
        }
    }

    private fun mapNetworkException(exception: Exception): Exception {
        return when (exception) {
            is ServerOfflineException -> exception
            is SocketTimeoutException -> ServerOfflineException("Connection timed out. Please try again.")
            is EOFException, is ProtocolException -> IllegalStateException("Received an invalid response from the server. Please try again.")
            is IOException -> ServerOfflineException()
            is HttpException -> when (exception.code()) {
                400 -> IllegalArgumentException("Please check the worker details and try again.")
                401 -> IllegalStateException("Please login again")
                403 -> IllegalArgumentException("You do not have permission to access this worker profile.")
                404 -> IllegalStateException("Worker profile or job not found")
                409 -> IllegalArgumentException("Sorry, all positions have been filled or this job was already accepted.")
                in 500..599 -> ServerOfflineException("Something went wrong. Please try again.")
                else -> IllegalArgumentException("Unable to complete worker request. Please try again.")
            }
            else -> exception
        }
    }
}
