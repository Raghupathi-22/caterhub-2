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
    val guestCountSelection: String = BookingOptions.guestSelectionPreset,
    val customGuestCountInput: String = "",
    // Legacy fields kept for source/test compatibility. The new UI does not expose them.
    val foodService: String = "",
    val selectedFoodServices: Set<CateringMealServiceType> = emptySet(),
    val staffingRequirements: Map<WorkerType, StaffingRequirement> = emptyMap(),
    val staffingEndTime: String = "",
    val cateringFoodType: String = BookingOptions.foodTypeNonVegetarian,
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
        val selectedServices = selectedFoodServices.takeIf { it.isNotEmpty() }
            ?.sortedBy { it.sortOrder }
            ?.joinToString(" + ") { it.backendLabel }

        return when {
            selectedServices != null && plan == BookingOptions.fullCatering -> selectedServices
            selectedServices != null -> "$selectedServices | $plan"
            plan == BookingOptions.fullCatering -> BookingOptions.fullCatering
            else -> "Full Catering - $plan"
        }
    }

    fun selectedFoodServicesLabel(): String {
        if (selectedFoodServices.isEmpty()) return "Not selected"
        return selectedFoodServices
            .sortedBy { it.sortOrder }
            .joinToString(" • ") { it.displayName }
    }
}

enum class CateringMealServiceType(
    val displayName: String,
    val backendLabel: String,
    val description: String,
    val sortOrder: Int
) {
    BREAKFAST(
        displayName = "Tiffin / Breakfast",
        backendLabel = "Breakfast",
        description = "Breakfast and morning tiffin service",
        sortOrder = 0
    ),
    LUNCH(
        displayName = "Lunch",
        backendLabel = "Lunch",
        description = "Complete lunch catering service",
        sortOrder = 1
    ),
    SNACKS(
        displayName = "Snacks",
        backendLabel = "Snacks",
        description = "Evening snacks and refreshments",
        sortOrder = 2
    ),
    BEVERAGES(
        displayName = "Beverages",
        backendLabel = "Beverages",
        description = "Tea, coffee, juices and refreshments",
        sortOrder = 3
    ),
    DINNER(
        displayName = "Dinner",
        backendLabel = "Dinner",
        description = "Complete dinner catering service",
        sortOrder = 4
    )
}

data class CateringMealServiceOption(
    val type: CateringMealServiceType,
    val emoji: String
) {
    val displayName: String = type.displayName
    val description: String = type.description
}

data class StaffingRequirement(
    val quantity: Int = 0,
    val paymentPerWorker: BigDecimal? = null
)

object BookingOptions {
    val eventTypes = EventTypeCatalog.eventTypes.map { it.backendValue }

    const val fullCatering = "Full Catering"

    const val guestSelectionPreset = "PRESET"
    const val guestSelectionCustom = "CUSTOM"
    const val foodTypeVegetarian = "VEGETARIAN"
    const val foodTypeNonVegetarian = "NON_VEGETARIAN"
    const val foodTypeCustom = "CUSTOM"
    const val planVegBasic = "Veg Basic"
    const val planVegClassic = "Veg Classic"
    const val planVegPremium = "Veg Premium"
    const val planNonVegBasic = "Non-Veg Basic"
    const val planNonVegClassic = "Non-Veg Classic"
    const val planNonVegPremium = "Non-Veg Premium"
    const val planCustomMenu = "Custom Menu"

    val cateringMealServiceOptions = listOf(
        CateringMealServiceOption(CateringMealServiceType.BREAKFAST, "🍳"),
        CateringMealServiceOption(CateringMealServiceType.LUNCH, "🍛"),
        CateringMealServiceOption(CateringMealServiceType.SNACKS, "🍿"),
        CateringMealServiceOption(CateringMealServiceType.BEVERAGES, "🥤"),
        CateringMealServiceOption(CateringMealServiceType.DINNER, "🍽")
    )

    fun defaultMealServices(): Set<CateringMealServiceType> = setOf(CateringMealServiceType.LUNCH)

    data class CateringPlanOption(
        val backendValue: String,
        val foodType: String,
        val title: String,
        val pricePerPerson: Int?,
        val menuItems: List<String>,
        val description: String? = null,
        val popular: Boolean = false
    )

    val cateringPlanOptions = listOf(
        CateringPlanOption(
            backendValue = planVegBasic,
            foodType = foodTypeVegetarian,
            title = "VEG BASIC",
            pricePerPerson = 399,
            menuItems = listOf("Rice", "2 Curries", "Dal", "Raita", "Sweet")
        ),
        CateringPlanOption(
            backendValue = planVegClassic,
            foodType = foodTypeVegetarian,
            title = "VEG CLASSIC",
            pricePerPerson = 599,
            menuItems = listOf("2 Starters", "Rice", "3 Curries", "Dal", "Raita", "Sweet"),
            popular = true
        ),
        CateringPlanOption(
            backendValue = planVegPremium,
            foodType = foodTypeVegetarian,
            title = "VEG PREMIUM",
            pricePerPerson = 899,
            menuItems = listOf("2 Starters", "Rice", "4 Curries", "Dal", "Raita", "2 Desserts", "Beverages")
        ),
        CateringPlanOption(
            backendValue = planNonVegBasic,
            foodType = foodTypeNonVegetarian,
            title = "NON-VEG BASIC",
            pricePerPerson = 499,
            menuItems = listOf("Chicken Biryani", "2 Curries", "Rice", "Raita", "Sweet")
        ),
        CateringPlanOption(
            backendValue = planNonVegClassic,
            foodType = foodTypeNonVegetarian,
            title = "NON-VEG CLASSIC",
            pricePerPerson = 699,
            menuItems = listOf("2 Starters", "Chicken Biryani", "3 Curries", "Dal", "Raita", "Sweet"),
            popular = true
        ),
        CateringPlanOption(
            backendValue = planNonVegPremium,
            foodType = foodTypeNonVegetarian,
            title = "NON-VEG PREMIUM",
            pricePerPerson = 999,
            menuItems = listOf("2 Starters", "Biryani", "4 Curries", "Dal", "Raita", "2 Desserts", "Beverages")
        ),
        CateringPlanOption(
            backendValue = planCustomMenu,
            foodType = foodTypeCustom,
            title = "CUSTOM MENU",
            pricePerPerson = null,
            menuItems = listOf("Choose starters, biryani, curries, rice, sweets, beverages and more."),
            description = "Build your own menu"
        )
    )

    val cateringPlans = cateringPlanOptions.map { it.backendValue }

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
    const val maxGuestCount = 2000
    const val maxGuestInputLength = 4

    fun plansByFoodType(foodType: String): List<CateringPlanOption> =
        cateringPlanOptions.filter { it.foodType == foodType }

    fun defaultPlanForFoodType(foodType: String): CateringPlanOption =
        plansByFoodType(foodType).firstOrNull() ?: cateringPlanOptions.first()

    fun planByBackendValue(value: String?): CateringPlanOption? =
        cateringPlanOptions.firstOrNull { it.backendValue == value }

    fun normalizePlanValue(value: String?): String {
        val normalized = value?.trim().orEmpty()
        return when (normalized) {
            "Basic" -> planNonVegBasic
            "Classic" -> planNonVegClassic
            "Premium" -> planNonVegPremium
            "Customized" -> planCustomMenu
            in cateringPlans -> normalized
            else -> ""
        }
    }
}

sealed class BookingValidationResult {
    data object Valid : BookingValidationResult()
    data class Invalid(val message: String) : BookingValidationResult()
}

object BookingValidator {
    fun validateStep(step: Int, draft: BookingDraft): BookingValidationResult = when (step) {
        0 -> validateEvent(draft)
        1 -> validateFoodServices(draft)
        2 -> validatePlanAndLocation(draft)
        3 -> validateDateTime(draft)
        else -> BookingValidationResult.Valid
    }

    fun validateForSubmit(draft: BookingDraft): BookingValidationResult {
        for (step in 0..3) {
            val result = validateStep(step, draft)
            if (result is BookingValidationResult.Invalid) return result
        }
        return BookingValidationResult.Valid
    }

    fun validateGuests(guestCount: Int?): BookingValidationResult = when {
        guestCount == null -> BookingValidationResult.Invalid("Please enter guest count.")
        guestCount <= 0 || guestCount > BookingOptions.maxGuestCount ->
            BookingValidationResult.Invalid("Enter a guest count between 1 and 2,000.")
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

    private fun validateFoodServices(draft: BookingDraft): BookingValidationResult {
        return if (draft.selectedFoodServices.isEmpty()) {
            BookingValidationResult.Invalid("Please select at least one food service.")
        } else {
            BookingValidationResult.Valid
        }
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
