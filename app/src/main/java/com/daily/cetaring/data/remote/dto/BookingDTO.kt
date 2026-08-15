package com.daily.cetaring.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

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

data class BookingDraft(
    val eventType: String = "",
    val guestCount: Int? = null,
    val foodService: String = "",
    val staffingRequirements: Map<WorkerType, StaffingRequirement> = emptyMap(),
    val foodRequirements: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val staffingEndTime: String = "",
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

    fun mealTypeForBackend(): String = listOf(
        foodService,
        staffingRequirements.entries
            .filter { it.value.quantity > 0 }
            .joinToString { "${it.value.quantity} ${it.key.label}" }
    )
        .filter { it.isNotBlank() }
        .joinToString(" - ")
}

data class StaffingRequirement(
    val quantity: Int = 0,
    val paymentPerWorker: BigDecimal? = null
)

object BookingOptions {
    val eventTypes = listOf("Wedding", "Birthday", "Engagement", "Housewarming", "Corporate", "Baby Shower", "Naming Ceremony", "Festival", "Other")
    val foodServices = listOf("Full Catering", "Food Only")
    val staffingServices = listOf(
        WorkerType.CHEF,
        WorkerType.SERVING_BOY,
        WorkerType.KITCHEN_HELPER,
        WorkerType.CLEANER
    )
    val menuCategories = listOf(
        "Breakfast", "Lunch", "Dinner", "Snacks", "Starters", "Main Course",
        "Biryani", "Curries", "Rice", "Breads", "Desserts", "Beverages"
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
        1 -> validateServicesAndLocation(draft)
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
        guestCount > BookingOptions.maxGuestCount -> BookingValidationResult.Invalid("Guest count cannot exceed ${BookingOptions.maxGuestCount}.")
        else -> BookingValidationResult.Valid
    }

    private fun validateEvent(draft: BookingDraft): BookingValidationResult {
        val eventType = requireText(draft.eventType, "Please select an event type.")
        return if (eventType is BookingValidationResult.Invalid) eventType else validateGuests(draft.guestCount)
    }

    private fun validateDateTime(draft: BookingDraft): BookingValidationResult {
        if (draft.eventDate.isBlank()) return BookingValidationResult.Invalid("Please select an event date.")
        if (draft.eventTime.isBlank()) return BookingValidationResult.Invalid("Please select an event time.")
        if (draft.staffingRequirements.values.any { it.quantity > 0 } && draft.staffingEndTime.isBlank()) {
            return BookingValidationResult.Invalid("Please select when the staffing shift ends.")
        }
        return BookingValidationResult.Valid
    }

    private fun validateServicesAndLocation(draft: BookingDraft): BookingValidationResult = when {
        draft.foodService.isBlank() && draft.staffingRequirements.values.none { it.quantity > 0 } ->
            BookingValidationResult.Invalid("Please select at least one catering service.")
        draft.staffingRequirements.values.any { it.quantity > 0 && (it.paymentPerWorker == null || it.paymentPerWorker <= BigDecimal.ZERO) } ->
            BookingValidationResult.Invalid("Please enter a positive offer for every staff service.")
        draft.address.isBlank() -> BookingValidationResult.Invalid("Please enter the event address.")
        draft.area.isBlank() -> BookingValidationResult.Invalid("Please enter the area.")
        draft.city.isBlank() -> BookingValidationResult.Invalid("Please enter the city.")
        else -> BookingValidationResult.Valid
    }

    private fun requireText(value: String, message: String): BookingValidationResult =
        if (value.isBlank()) BookingValidationResult.Invalid(message) else BookingValidationResult.Valid
}
