package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

enum class WorkerType(val label: String) {
    CHEF("Chef"),
    ASSISTANT_CHEF("Assistant Chef"),
    SERVING_BOY("Serving Staff"),
    SERVING_GIRL("Serving Staff"),
    CLEANER("Cleaner"),
    KITCHEN_HELPER("Kitchen Helper"),
    SUPERVISOR("Event Supervisor")
}

enum class WorkerStatus(val label: String) {
    PENDING_VERIFICATION("Pending verification"),
    ACTIVE("Approved"),
    SUSPENDED("Suspended"),
    REJECTED("Rejected")
}

data class ServiceRequestRequest(
    val serviceType: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String,
    val location: String,
    val area: String,
    val details: String? = null,
    val totalAmount: BigDecimal
)

data class ServiceRequestResponse(
    val id: Long, val serviceType: String, val eventType: String, val eventDate: String,
    val startTime: String, val location: String, val area: String, val details: String?,
    val totalAmount: BigDecimal, val status: String, val createdAt: String?
)

data class CreateWorkerProfileRequest(
    @SerializedName("workerType")
    val workerType: WorkerType,
    @SerializedName("experienceYears")
    val experienceYears: Int,
    val skills: String?,
    @SerializedName("preferredAreas")
    val preferredAreas: String?,
    val languages: String?,
    val bio: String?
)

data class CreateStaffingRequest(
    val eventType: String,
    val workerType: WorkerType,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val area: String,
    val requiredWorkers: Int,
    val payment: BigDecimal,
    val additionalRequirements: String? = null
)

data class WorkerProfileResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val fullName: String,
    val workerType: WorkerType,
    val status: WorkerStatus,
    val experienceYears: Int,
    val skills: String?,
    val preferredAreas: String?,
    val languages: String?,
    val bio: String?,
    val rating: Double,
    val totalRatings: Int,
    val approvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

enum class AssignmentStatus(val label: String) {
    OFFERED("Offered"),
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class AssignmentResponse(
    val id: Long,
    val bookingId: Long,
    val workerProfileId: Long,
    val assignedByUserId: Long?,
    val workerType: WorkerType,
    val status: AssignmentStatus,
    val offeredAt: String?,
    val respondedAt: String?,
    val completedAt: String?,
    val notes: String?,
    val declineReason: String?
)

data class RespondAssignmentRequest(
    val status: AssignmentStatus,
    val declineReason: String? = null
)

data class StaffingJobResponse(
    val id: Long,
    val eventType: String,
    val workerType: WorkerType,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val area: String,
    val requiredWorkers: Int,
    val acceptedWorkers: Int,
    val remainingPositions: Int,
    val payment: BigDecimal,
    val additionalRequirements: String?,
    val status: String,
    val alreadyAccepted: Boolean? = false,
    val createdAt: String? = null
)

data class WorkerJobResponse(
    val acceptanceId: Long,
    val jobId: Long,
    val eventType: String,
    val workerType: WorkerType,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val area: String,
    val payment: BigDecimal,
    val status: String,
    val acceptedAt: String?
)

data class AcceptStaffingJobResponse(
    val job: StaffingJobResponse,
    val myJob: WorkerJobResponse,
    val message: String
)

data class WorkerDashboardResponse(
    val profile: WorkerProfileResponse,
    val profileCompletionPercent: Int,
    val availableForWork: Boolean,
    val nearbyOpportunities: List<StaffingJobResponse>,
    val myJobs: List<WorkerJobResponse>
)

data class UpdateAvailabilityToggleRequest(
    val available: Boolean,
    val notes: String? = null
)

object WorkerOnboardingValidator {
    fun validateRole(workerType: WorkerType?): String? = if (workerType == null) "Please select a worker role." else null

    fun validateExperience(experienceYears: Int?): String? = when {
        experienceYears == null -> "Please enter experience in years."
        experienceYears < 0 -> "Experience cannot be negative."
        experienceYears > 60 -> "Please enter a valid experience value."
        else -> null
    }

    fun validateRequired(value: String, field: String): String? = if (value.isBlank()) "Please enter $field." else null
}
