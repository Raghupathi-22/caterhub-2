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
    val serviceType: String = "",
    val guestCount: Int? = null,
    val foodType: String = "",
    val foodRequirements: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val address: String = "",
    val area: String = "",
    val city: String = "Hyderabad",
    val pincode: String = "",
    val specialInstructions: String = "",
    val workerCount: Int? = null
) {
    fun deliveryAddress(): String = listOf(address, area, city, pincode)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(", ")

    fun eventDateTimeIso(): String = "${eventDate}T${eventTime}:00"

    fun mealTypeForBackend(): String = listOf(foodType, serviceType)
        .filter { it.isNotBlank() }
        .joinToString(" - ")
}

object BookingOptions {
    val eventTypes = listOf("Wedding", "Birthday", "Engagement", "Housewarming", "Corporate", "Baby Shower", "Naming Ceremony", "Festival", "Other")
    val serviceTypes = listOf("Full Catering", "Food Only", "Chef", "Serving Staff", "Cleaning Staff", "Kitchen Helper", "Custom")
    val foodTypes = listOf("Breakfast", "Tiffin", "Lunch", "Dinner", "Snacks", "Beverages", "Full Day")
    val guestQuickOptions = listOf(20, 50, 100, 150, 200)
    const val maxGuestCount = 5000
}

sealed class BookingValidationResult {
    data object Valid : BookingValidationResult()
    data class Invalid(val message: String) : BookingValidationResult()
}

object BookingValidator {
    fun validateStep(step: Int, draft: BookingDraft): BookingValidationResult = when (step) {
        0 -> requireText(draft.eventType, "Please select an event type.")
        1 -> requireText(draft.serviceType, "Please select what you need.")
        2 -> validateGuests(draft.guestCount)
        3 -> requireText(draft.foodType, "Please select a food service type.")
        4 -> validateDateTime(draft.eventDate, draft.eventTime)
        5 -> validateLocation(draft)
        else -> BookingValidationResult.Valid
    }

    fun validateForSubmit(draft: BookingDraft): BookingValidationResult {
        for (step in 0..5) {
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

    private fun validateDateTime(date: String, time: String): BookingValidationResult {
        if (date.isBlank()) return BookingValidationResult.Invalid("Please select an event date.")
        if (time.isBlank()) return BookingValidationResult.Invalid("Please select an event time.")
        return BookingValidationResult.Valid
    }

    private fun validateLocation(draft: BookingDraft): BookingValidationResult = when {
        draft.address.isBlank() -> BookingValidationResult.Invalid("Please enter the event address.")
        draft.area.isBlank() -> BookingValidationResult.Invalid("Please enter the area.")
        draft.city.isBlank() -> BookingValidationResult.Invalid("Please enter the city.")
        draft.pincode.length != 6 || draft.pincode.any { !it.isDigit() } -> BookingValidationResult.Invalid("Please enter a valid 6-digit pincode.")
        else -> BookingValidationResult.Valid
    }

    private fun requireText(value: String, message: String): BookingValidationResult =
        if (value.isBlank()) BookingValidationResult.Invalid(message) else BookingValidationResult.Valid
}
