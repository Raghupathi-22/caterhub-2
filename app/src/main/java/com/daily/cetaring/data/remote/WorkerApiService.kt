package com.daily.cetaring.data.remote

import com.daily.cetaring.data.remote.dto.AcceptStaffingJobResponse
import com.daily.cetaring.data.remote.dto.AssignmentResponse
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.CreateStaffingRequest
import com.daily.cetaring.data.remote.dto.RespondAssignmentRequest
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.UpdateAvailabilityToggleRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestResponse
import com.daily.cetaring.data.remote.dto.WorkerDashboardResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerType
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface WorkerApiService {
    @GET("workers/profiles/me")
    suspend fun getMyProfile(
        @Header("Authorization") authorization: String
    ): WorkerProfileResponse

    @POST("workers/profiles/me")
    suspend fun createMyProfile(
        @Header("Authorization") authorization: String,
        @Body request: CreateWorkerProfileRequest
    ): WorkerProfileResponse

    @GET("workers/profiles/{profileId}/assignments")
    suspend fun getAssignmentsForWorker(
        @Header("Authorization") authorization: String,
        @Path("profileId") profileId: Long
    ): List<AssignmentResponse>

    @PATCH("workers/assignments/{assignmentId}/response")
    suspend fun respondToAssignment(
        @Header("Authorization") authorization: String,
        @Path("assignmentId") assignmentId: Long,
        @Body request: RespondAssignmentRequest
    ): AssignmentResponse

    @GET("workers/dashboard/me")
    suspend fun getDashboard(@Header("Authorization") authorization: String): WorkerDashboardResponse

    @GET("workers/jobs")
    suspend fun getAvailableJobs(
        @Header("Authorization") authorization: String,
        @Query("role") role: WorkerType? = null,
        @Query("area") area: String? = null,
        @Query("search") search: String? = null
    ): List<StaffingJobResponse>

    @GET("workers/jobs/{jobId}")
    suspend fun getJob(
        @Header("Authorization") authorization: String,
        @Path("jobId") jobId: Long
    ): StaffingJobResponse

    @POST("workers/jobs/{jobId}/accept")
    suspend fun acceptJob(
        @Header("Authorization") authorization: String,
        @Path("jobId") jobId: Long
    ): AcceptStaffingJobResponse

    @GET("workers/jobs/me")
    suspend fun getMyJobs(@Header("Authorization") authorization: String): List<WorkerJobResponse>

    @PUT("workers/availability/me")
    suspend fun updateAvailability(
        @Header("Authorization") authorization: String,
        @Body request: UpdateAvailabilityToggleRequest
    )

    @GET("service-requests/me")
    suspend fun getMyServiceRequests(@Header("Authorization") authorization: String): List<ServiceRequestResponse>

    @GET("workers/staffing-requests/me")
    suspend fun getMyStaffingRequests(@Header("Authorization") authorization: String): List<StaffingJobResponse>

    @POST("service-requests")
    suspend fun createServiceRequest(
        @Header("Authorization") authorization: String,
        @Body request: ServiceRequestRequest
    ): ServiceRequestResponse

    @POST("workers/staffing-requests")
    suspend fun createStaffingRequest(
        @Header("Authorization") authorization: String,
        @Body request: CreateStaffingRequest
    ): StaffingJobResponse
}
