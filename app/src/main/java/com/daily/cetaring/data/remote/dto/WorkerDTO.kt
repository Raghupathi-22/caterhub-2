package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * All service-provider roles supported by CaterHub.
 * The enum names must match backend WorkerProfile.WorkerType values.
 */
enum class WorkerType(val label: String, val category: String) {
    CHEF("Chef", "Catering & Food"),
    ASSISTANT_CHEF("Assistant Chef", "Catering & Food"),
    SERVING_BOY("Serving Staff", "Catering & Food"),
    SERVING_GIRL("Serving Staff", "Catering & Food"),
    CLEANER("Cleaning Staff", "Catering & Food"),
    KITCHEN_HELPER("Kitchen Helper", "Catering & Food"),
    SUPERVISOR("Event Supervisor", "Event Support"),

    PUJARI("Pujari / Priest", "Religious & Ceremony"),

    PHOTOGRAPHER("Photographer", "Photography & Media"),
    VIDEOGRAPHER("Videographer", "Photography & Media"),
    LIVE_STREAMER("Live Event Streamer", "Photography & Media"),

    DJ("DJ", "Entertainment"),
    BAND_MELAM("Band / Melam", "Entertainment"),
    SINGER("Singer", "Entertainment"),
    DANCER("Dance Performer", "Entertainment"),
    ANCHOR_EMCEE("Anchor / Emcee", "Entertainment"),
    MAGICIAN("Magician", "Entertainment"),
    KIDS_ENTERTAINER("Kids Entertainer", "Entertainment"),

    MAKEUP_ARTIST("Makeup Artist", "Beauty & Personal Care"),
    BRIDAL_MAKEUP_ARTIST("Bridal Makeup Artist", "Beauty & Personal Care"),
    MEHENDI_ARTIST("Mehendi Artist", "Beauty & Personal Care"),
    HAIR_STYLIST("Hair Stylist", "Beauty & Personal Care"),
    SAREE_DRAPER("Saree Draping Specialist", "Beauty & Personal Care"),
    NAIL_ARTIST("Nail Artist", "Beauty & Personal Care"),

    EVENT_DECORATOR("Event Decorator", "Decoration & Event Setup"),
    FLOWER_DECORATOR("Flower Decorator", "Decoration & Event Setup"),
    LIGHTING_TECHNICIAN("Lighting Technician", "Decoration & Event Setup"),
    SOUND_TECHNICIAN("Sound Technician", "Decoration & Event Setup"),
    STAGE_TENT_SPECIALIST("Stage / Tent Specialist", "Decoration & Event Setup"),

    SECURITY_GUARD("Security Staff", "Event Support"),
    VALET_DRIVER("Valet / Driver", "Transport & Guest Travel"),
    EVENT_COORDINATOR("Event Coordinator", "Event Support");

    companion object {
        fun displayRoles(): List<WorkerType> =
            entries.distinctBy { it.label }
    }
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
    @SerializedName("workerType") val workerType: WorkerType,
    @SerializedName("experienceYears") val experienceYears: Int,
    val skills: String?,
    @SerializedName("preferredAreas") val preferredAreas: String?,
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
    fun validateRole(workerType: WorkerType?): String? =
        if (workerType == null) "Please select a service role." else null

    fun validateExperience(experienceYears: Int?): String? = when {
        experienceYears == null -> "Please enter experience in years."
        experienceYears < 0 -> "Experience cannot be negative."
        experienceYears > 60 -> "Please enter a valid experience value."
        else -> null
    }

    fun validateRequired(value: String, field: String): String? =
        if (value.isBlank()) "Please enter $field." else null
}
