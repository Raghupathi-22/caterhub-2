package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * All service-provider roles supported by CaterHub.
 * The enum names must match backend WorkerProfile.WorkerType values.
 */
enum class WorkerType(val label: String, val category: String) {
    CATERING("Catering", "Catering & Food"),
    CHEF("Chef", "Catering & Food"),
    HEAD_CHEF("Head Chef", "Catering & Food"),
    ASSISTANT_CHEF("Assistant Chef", "Catering & Food"),
    COOK("Cook", "Catering & Food"),
    BIRYANI_CHEF("Biryani Chef", "Catering & Food"),
    TANDOOR_CHEF("Tandoor Chef", "Catering & Food"),
    SWEET_MITHAI_CHEF("Sweet/Mithai Chef", "Catering & Food"),
    KITCHEN_HELPER("Kitchen Helper", "Catering & Food"),
    SERVING_STAFF("Serving Staff", "Catering & Food"),
    WAITER("Waiter", "Catering & Food"),
    CATERING_BOY("Catering Boy", "Catering & Food"),
    CATERING_GIRL("Catering Girl", "Catering & Food"),
    CLEANER("Cleaner", "Catering & Food"),
    CATERING_SUPERVISOR("Catering Supervisor", "Catering & Food"),

    EVENT_DECORATOR("Event Decorator", "Decoration"),
    WEDDING_DECORATOR("Wedding Decorator", "Decoration"),
    STAGE_DECORATOR("Stage Decorator", "Decoration"),
    FLOWER_DECORATOR("Flower Decorator", "Decoration"),
    BALLOON_DECORATOR("Balloon Decorator", "Decoration"),
    LIGHTING_TECHNICIAN("Lighting Technician", "Decoration"),
    TENT_SHAMIANA_WORKER("Tent/Shamiana Worker", "Decoration"),
    STAGE_SETUP_WORKER("Stage Setup Worker", "Decoration"),
    MANDAP_DECORATOR("Mandap Decorator", "Decoration"),
    ENTRANCE_DECORATOR("Entrance Decorator", "Decoration"),
    BACKDROP_DECORATOR("Backdrop Decorator", "Decoration"),
    DECORATION_SUPERVISOR("Decoration Supervisor", "Decoration"),

    DJ("DJ", "Entertainment"),
    SOUND_TECHNICIAN("Sound Technician", "Entertainment"),
    SINGER("Singer", "Entertainment"),
    MALE_SINGER("Male Singer", "Entertainment"),
    FEMALE_SINGER("Female Singer", "Entertainment"),
    BAND_MEMBER("Band Member", "Entertainment"),
    BAND_LEADER("Band Leader", "Entertainment"),
    MELAM_ARTIST("Melam Artist", "Entertainment"),
    BAND_MELAM_ARTIST("Band Melam Artist", "Entertainment"),
    DANCER("Dancer", "Entertainment"),
    DANCE_PERFORMER("Dance Performer", "Entertainment"),
    DANCE_TROUPE("Dance Troupe", "Entertainment"),
    ANCHOR("Anchor", "Entertainment"),
    MC("MC", "Entertainment"),
    MAGICIAN("Magician", "Entertainment"),
    FOLK_ARTIST("Folk Artist", "Entertainment"),
    CULTURAL_PERFORMER("Cultural Performer", "Entertainment"),

    MAKEUP_ARTIST("Makeup Artist", "Beauty"),
    BRIDAL_MAKEUP_ARTIST("Bridal Makeup Artist", "Beauty"),
    GROOM_MAKEUP_ARTIST("Groom Makeup Artist", "Beauty"),
    HAIR_STYLIST("Hair Stylist", "Beauty"),
    MEHENDI_ARTIST("Mehendi Artist", "Beauty"),
    SAREE_DRAPIST("Saree Drapist", "Beauty"),
    BEAUTY_SPECIALIST("Beauty Specialist", "Beauty"),

    PHOTOGRAPHER("Photographer", "Photography & Video"),
    WEDDING_PHOTOGRAPHER("Wedding Photographer", "Photography & Video"),
    EVENT_PHOTOGRAPHER("Event Photographer", "Photography & Video"),
    VIDEOGRAPHER("Videographer", "Photography & Video"),
    WEDDING_VIDEOGRAPHER("Wedding Videographer", "Photography & Video"),
    DRONE_OPERATOR("Drone Operator", "Photography & Video"),
    PHOTO_EDITOR("Photo Editor", "Photography & Video"),
    VIDEO_EDITOR("Video Editor", "Photography & Video"),
    PHOTO_BOOTH_OPERATOR("Photo Booth Operator", "Photography & Video"),
    LIVE_STREAMING_OPERATOR("Live Streaming Operator", "Photography & Video"),

    PUJARI("Pujari", "Religious & Ceremony"),
    PRIEST("Priest", "Religious & Ceremony"),
    PANDIT("Pandit", "Religious & Ceremony"),
    POOJA_SPECIALIST("Pooja Specialist", "Religious & Ceremony"),
    HOMAM_SPECIALIST("Homam Specialist", "Religious & Ceremony"),
    WEDDING_RITUAL_SPECIALIST("Wedding Ritual Specialist", "Religious & Ceremony"),

    EVENT_MANAGER("Event Manager", "Event Support"),
    EVENT_SUPERVISOR("Event Supervisor", "Event Support"),
    EVENT_COORDINATOR("Event Coordinator", "Event Support"),
    HOST("Host", "Event Support"),
    REGISTRATION_STAFF("Registration Staff", "Event Support"),
    USHER("Usher", "Event Support"),
    SECURITY_STAFF("Security Staff", "Event Support"),
    PARKING_STAFF("Parking Staff", "Event Support"),
    GENERAL_HELPER("General Helper", "Event Support"),
    CLEANING_STAFF("Cleaning Staff", "Event Support"),

    CHAIR_RENTAL("Chair Rental", "Rentals"),
    TABLE_RENTAL("Table Rental", "Rentals"),
    SOFA_RENTAL("Sofa Rental", "Rentals"),
    CROCKERY_RENTAL("Crockery Rental", "Rentals"),
    DINING_EQUIPMENT("Dining Equipment", "Rentals"),
    COOKING_EQUIPMENT("Cooking Equipment", "Rentals"),
    GENERATOR_OPERATOR("Generator", "Rentals"),
    FAN_COOLER_RENTAL("Fan/Cooler Rental", "Rentals"),
    TENT_RENTAL("Tent Rental", "Rentals"),
    STAGE_EQUIPMENT("Stage Equipment", "Rentals"),

    EVENT_DRIVER("Event Driver", "Transport & Logistics"),
    GUEST_TRANSPORT_DRIVER("Guest Transport Driver", "Transport & Logistics"),
    GOODS_TRANSPORT_DRIVER("Goods Transport Driver", "Transport & Logistics"),
    LOADING_UNLOADING_STAFF("Loading/Unloading Staff", "Transport & Logistics"),

    INVITATION_DESIGNER("Invitation Designer", "Other Event Services"),
    CAKE_SPECIALIST("Cake Specialist", "Other Event Services"),
    RETURN_GIFT_SPECIALIST("Return Gift Specialist", "Other Event Services"),
    CUSTOM_EVENT_PROFESSIONAL("Custom Event Professional", "Other Event Services"),

    // Legacy values kept for backward compatibility with existing backend data.
    SERVING_BOY("Serving Staff", "Catering & Food"),
    SERVING_GIRL("Serving Staff", "Catering & Food"),
    SUPERVISOR("Event Supervisor", "Event Support"),
    LIVE_STREAMER("Live Event Streamer", "Photography & Video"),
    BAND_MELAM("Band / Melam", "Entertainment"),
    ANCHOR_EMCEE("Anchor / Emcee", "Entertainment"),
    KIDS_ENTERTAINER("Kids Entertainer", "Entertainment"),
    SAREE_DRAPER("Saree Draping Specialist", "Beauty"),
    NAIL_ARTIST("Nail Artist", "Beauty"),
    STAGE_TENT_SPECIALIST("Stage / Tent Specialist", "Decoration"),
    SECURITY_GUARD("Security Staff", "Event Support"),
    VALET_DRIVER("Valet / Driver", "Transport & Logistics");

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
        if (workerType == null) "Please select a worker role." else null

    fun validateExperience(experienceYears: Int?): String? = when {
        experienceYears == null -> "Please enter experience in years."
        experienceYears < 0 -> "Experience cannot be negative."
        experienceYears > 60 -> "Please enter a valid experience value."
        else -> null
    }

    fun validateRequired(value: String, field: String): String? =
        if (value.isBlank()) "Please enter $field." else null
}
