package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateBookingRequest(
    @SerializedName("businessId") val businessId: Long = 1L,
    @SerializedName("userId") val userId: Long,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("guestCount") val guestCount: Int,
    @SerializedName("mealType") val mealType: String,
    @SerializedName("eventDateTime") val eventDateTime: String,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("specialInstructions") val specialInstructions: String?,
    @SerializedName("estimatedAmount") val estimatedAmount: BigDecimal
)

data class CreateMyBookingRequest(
    @SerializedName("businessId") val businessId: Long? = null,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("guestCount") val guestCount: Int,
    @SerializedName("mealType") val mealType: String,
    @SerializedName("eventDateTime") val eventDateTime: String,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("specialInstructions") val specialInstructions: String?,
    @SerializedName("estimatedAmount") val estimatedAmount: BigDecimal
)

data class BookingResponse(
    val id: Long,
    val businessId: Long?,
    val userId: Long?,
    val bookingReference: String?,
    val eventType: String,
    val guestCount: Int,
    val mealType: String,
    val eventDateTime: String,
    val deliveryAddress: String,
    val specialInstructions: String?,
    val totalAmount: BigDecimal?,
    val status: String,
    val paymentStatus: String?,
    val createdAt: String?
)

data class ServiceRequestBookingResponse(
    val id: Long,
    val serviceType: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String?,
    val location: String,
    val area: String,
    val selectedServices: List<String> = emptyList(),
    val instructions: String?,
    val details: String?,
    val quoteBased: Boolean = false,
    val totalAmount: BigDecimal?,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?
)

enum class CustomerBookingSource {
    CATERING,
    SERVICE_REQUEST
}

data class CustomerBookingUiModel(
    val id: Long,
    val source: CustomerBookingSource,
    val categoryId: String,
    val categoryName: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String?,
    val address: String,
    val area: String?,
    val services: List<String>,
    val totalAmount: BigDecimal?,
    val quoteBased: Boolean,
    val status: String,
    val createdAt: String?
)

/**
 * Draft used only by the customer catering flow.
 *
 * Catering is always "Full Catering" here. Staff requests are deliberately
 * kept out of this flow; staff booking has its own entry point on Home.
 */
data class BookingDraft(
    val eventType: String? = null,
    val guestCount: Int? = null,
    // Legacy fields kept for source/test compatibility. The new UI does not expose them.
    val foodService: String = "",
    val staffingRequirements: Map<WorkerType, StaffingRequirement> = emptyMap(),
    val staffingEndTime: String = "",
    val cateringPlan: String = "",
    val foodRequirements: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val address: String = "",
    val area: String = "",
    val city: String = "Hyderabad",
    val landmark: String = "",
    val specialInstructions: String = "",
) {
    fun deliveryAddress(): String = listOf(address, area, city, landmark)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(", ")

    fun eventDateTimeIso(): String = "${eventDate}T${eventTime}:00"

    fun mealTypeForBackend(): String {
        val plan = cateringPlan.ifBlank {
            if (foodService.isNotBlank()) foodService else BookingOptions.fullCatering
        }
        return if (plan == BookingOptions.fullCatering) {
            BookingOptions.fullCatering
        } else {
            "Full Catering - $plan"
        }
    }
}

data class StaffingRequirement(
    val quantity: Int = 0,
    val paymentPerWorker: BigDecimal? = null
)

object BookingOptions {
    val eventTypes = EventTypeCatalog.eventTypes.map { it.backendValue }

    const val fullCatering = "Full Catering"

    val cateringPlans = listOf(
        "Basic",
        "Classic",
        "Premium",
        "Customized"
    )

    val menuCategories = listOf(
        "Breakfast & Tiffin",
        "Lunch",
        "Dinner",
        "Snacks",
        "Starters",
        "Main Course",
        "Biryani",
        "Curries",
        "Rice",
        "Breads",
        "Desserts",
        "Beverages"
    )

    val guestQuickOptions = listOf(20, 50, 100, 150, 200)
    const val maxGuestCount = 5000
}

sealed class BookingValidationResult {
    data object Valid : BookingValidationResult()
    data class Invalid(val message: String) : BookingValidationResult()
}

object BookingValidator {
    fun validateStep(step: Int, draft: BookingDraft): BookingValidationResult = when (step) {
        0 -> validateEvent(draft)
        1 -> validatePlanAndLocation(draft)
        2 -> validateDateTime(draft)
        else -> BookingValidationResult.Valid
    }

    fun validateForSubmit(draft: BookingDraft): BookingValidationResult {
        for (step in 0..2) {
            val result = validateStep(step, draft)
            if (result is BookingValidationResult.Invalid) return result
        }
        return BookingValidationResult.Valid
    }

    fun validateGuests(guestCount: Int?): BookingValidationResult = when {
        guestCount == null -> BookingValidationResult.Invalid("Please enter guest count.")
        guestCount <= 0 -> BookingValidationResult.Invalid("Guest count must be a positive number.")
        guestCount > BookingOptions.maxGuestCount ->
            BookingValidationResult.Invalid("Guest count cannot exceed ${BookingOptions.maxGuestCount}.")
        else -> BookingValidationResult.Valid
    }

    private fun validateEvent(draft: BookingDraft): BookingValidationResult {
        val eventType = if (draft.eventType.isNullOrBlank()) {
            BookingValidationResult.Invalid("Please select an event type.")
        } else {
            BookingValidationResult.Valid
        }
        return if (eventType is BookingValidationResult.Invalid) eventType
        else validateGuests(draft.guestCount)
    }

    private fun validatePlanAndLocation(draft: BookingDraft): BookingValidationResult = when {
        draft.cateringPlan.isBlank() &&
            draft.foodService.isBlank() &&
            draft.staffingRequirements.values.none { it.quantity > 0 } ->
            BookingValidationResult.Invalid("Please select a catering plan.")

        draft.staffingRequirements.values.any {
            it.quantity > 0 &&
                (it.paymentPerWorker == null || it.paymentPerWorker <= BigDecimal.ZERO)
        } ->
            BookingValidationResult.Invalid("Please enter a positive offer for every staff service.")

        draft.staffingRequirements.values.any { it.quantity > 0 } &&
            draft.staffingEndTime.isBlank() ->
            BookingValidationResult.Invalid("Please select when the staffing shift ends.")

        draft.address.isBlank() ->
            BookingValidationResult.Invalid("Please enter the event address.")

        draft.area.isBlank() ->
            BookingValidationResult.Invalid("Please enter the area.")

        draft.city.isBlank() ->
            BookingValidationResult.Invalid("Please enter the city.")

        else -> BookingValidationResult.Valid
    }

    private fun validateDateTime(draft: BookingDraft): BookingValidationResult {
        if (draft.eventDate.isBlank()) {
            return BookingValidationResult.Invalid("Please select an event date.")
        }
        if (draft.eventTime.isBlank()) {
            return BookingValidationResult.Invalid("Please select an event time.")
        }

        val eventDate = runCatching { LocalDate.parse(draft.eventDate) }.getOrNull()
            ?: return BookingValidationResult.Invalid("Please select a valid event date.")

        val eventTime = runCatching { LocalTime.parse(draft.eventTime) }.getOrNull()
            ?: return BookingValidationResult.Invalid("Please select a valid event time.")

        if (LocalDateTime.of(eventDate, eventTime).isBefore(LocalDateTime.now())) {
            return BookingValidationResult.Invalid("Please select a future event date and time.")
        }

        return BookingValidationResult.Valid
    }
}
