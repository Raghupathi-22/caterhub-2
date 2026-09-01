package com.daily.cetaring.data.repository

import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.HealthApiService
import com.daily.cetaring.data.remote.WorkerApiService
import com.daily.cetaring.data.remote.dto.AcceptStaffingJobResponse
import com.daily.cetaring.data.remote.dto.AssignmentResponse
import com.daily.cetaring.data.remote.dto.AssignmentStatus
import com.daily.cetaring.data.remote.dto.CreateStaffingRequest
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.RespondAssignmentRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestResponse
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.UpdateAvailabilityToggleRequest
import com.daily.cetaring.data.remote.dto.WorkerDashboardResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerType
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class WorkerRepository(
    private val workerApiService: WorkerApiService,
    private val healthApiService: HealthApiService,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun getMyProfile(): WorkerProfileResponse =
        executeNetworkCall { workerApiService.getMyProfile(bearerToken()) }

    suspend fun createMyProfile(request: CreateWorkerProfileRequest): WorkerProfileResponse =
        executeNetworkCall { workerApiService.createMyProfile(bearerToken(), request) }

    suspend fun getAssignmentsForWorker(profileId: Long): List<AssignmentResponse> =
        executeNetworkCall { workerApiService.getAssignmentsForWorker(bearerToken(), profileId) }

    suspend fun acceptAssignment(assignmentId: Long): AssignmentResponse =
        executeNetworkCall {
            workerApiService.respondToAssignment(
                bearerToken(),
                assignmentId,
                RespondAssignmentRequest(status = AssignmentStatus.ACCEPTED)
            )
        }

    suspend fun getDashboard(): WorkerDashboardResponse =
        executeNetworkCall { workerApiService.getDashboard(bearerToken()) }

    suspend fun getAvailableJobs(
        role: WorkerType? = null,
        area: String? = null,
        search: String? = null
    ): List<StaffingJobResponse> =
        executeNetworkCall { workerApiService.getAvailableJobs(bearerToken(), role, area, search) }

    suspend fun getJob(jobId: Long): StaffingJobResponse =
        executeNetworkCall { workerApiService.getJob(bearerToken(), jobId) }

    suspend fun acceptJob(jobId: Long): AcceptStaffingJobResponse =
        executeNetworkCall { workerApiService.acceptJob(bearerToken(), jobId) }

    suspend fun getMyJobs(): List<WorkerJobResponse> =
        executeNetworkCall { workerApiService.getMyJobs(bearerToken()) }

    suspend fun updateAvailability(available: Boolean) {
        executeNetworkCall(WorkerApiOperation.AVAILABILITY_UPDATE) {
            workerApiService.updateAvailability(
                bearerToken(),
                UpdateAvailabilityToggleRequest(available = available)
            )
        }
    }

    suspend fun createServiceRequest(request: ServiceRequestRequest): ServiceRequestResponse =
        executeNetworkCall { workerApiService.createServiceRequest(bearerToken(), request) }

    suspend fun createStaffingRequest(request: CreateStaffingRequest): StaffingJobResponse =
        executeNetworkCall { workerApiService.createStaffingRequest(bearerToken(), request) }

    private suspend fun bearerToken(): String {
        val token = authLocalDataSource.accessTokenFlow.first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Your session has expired. Please sign in again.")
        }
        return "Bearer $token"
    }

    private suspend fun <T> executeNetworkCall(
        operation: WorkerApiOperation = WorkerApiOperation.DEFAULT,
        block: suspend () -> T
    ): T {
        return try {
            block()
        } catch (exception: Exception) {
            throw mapNetworkException(exception, operation)
        }
    }

    private fun mapNetworkException(
        exception: Exception,
        operation: WorkerApiOperation
    ): Exception {
        if (exception is HttpException && exception.code() == 409 && operation == WorkerApiOperation.AVAILABILITY_UPDATE) {
            return IllegalArgumentException("Availability can be enabled after your profile is verified.")
        }
        return ApiErrorMapper.map(
            exception = exception,
            contextLabel = "worker request",
            badRequestFallback = "Please check the worker details and try again.",
            defaultFallback = "Unable to complete worker request. Please try again."
        )
    }

    private enum class WorkerApiOperation {
        DEFAULT,
        AVAILABILITY_UPDATE
    }
}
