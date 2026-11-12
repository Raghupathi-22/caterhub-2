package com.daily.cetaring.domain.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceListingCatalog
import com.daily.cetaring.presentation.components.EventCalendarRules
import com.daily.cetaring.presentation.components.EventTimeRules
import java.time.LocalTime

/* ------------------------------------------------------------------------------------------------
 * Price display shared by the review screens. Never fabricates a price and never shows ₹0 for a
 * quote-only selection.
 * ---------------------------------------------------------------------------------------------- */
object BookingPriceDisplay {
    const val PRICE_ON_QUOTE = "Price on quote"

    /** Total label for service requests mixing published-price and quote-only lines. */
    fun serviceTotal(fixedTotal: Int, hasQuote: Boolean, rupees: (Int) -> String): String = when {
        hasQuote && fixedTotal > 0 -> "${rupees(fixedTotal)} + quote"
        hasQuote -> PRICE_ON_QUOTE
        else -> rupees(fixedTotal)
    }

    fun serviceTotalLabel(hasQuote: Boolean, fixedTotal: Int): String = when {
        hasQuote && fixedTotal > 0 -> "Total (published prices)"
        hasQuote -> "Total"
        else -> "Estimated total"
    }

    /** Catering total: the estimate when the package has a published price, otherwise "Price on quote". */
    fun cateringTotal(draft: BookingDraft, rupees: (Long) -> String): String =
        CateringJourney.estimate(draft)?.let { rupees(it.total) } ?: PRICE_ON_QUOTE
}

/* ------------------------------------------------------------------------------------------------
 * Catering journey: Event & guests → Food services → Package → When & where → Review.
 * The legacy [BookingValidator] step indices are kept unchanged (tests and submit validation rely on
 * them); this object maps the new UI steps onto them.
 * ---------------------------------------------------------------------------------------------- */
object CateringJourney {
    const val STEP_EVENT = 0
    const val STEP_FOOD_SERVICES = 1
    const val STEP_PACKAGE = 2
    const val STEP_WHEN_WHERE = 3
    const val STEP_REVIEW = 4

    val stepTitles = listOf("Event & guests", "Food services", "Package", "When & where", "Review")

    fun validateUiStep(step: Int, draft: BookingDraft): BookingValidationResult = when (step) {
        STEP_EVENT -> BookingValidator.validateStep(0, draft)
        STEP_FOOD_SERVICES -> BookingValidator.validateStep(1, draft)
        STEP_PACKAGE -> validatePackage(draft)
        STEP_WHEN_WHERE -> BookingValidator.validateStep(3, draft).orElse { validateLocation(draft) }
        else -> BookingValidator.validateForSubmit(draft)
    }

    fun validatePackage(draft: BookingDraft): BookingValidationResult = when {
        draft.cateringPlan.isBlank() -> BookingValidationResult.Invalid("Please choose a catering package.")
        draft.cateringPlan == BookingOptions.planCustomMenu && draft.foodRequirements.isBlank() ->
            BookingValidationResult.Invalid("Tell us the dishes you would like on your custom menu.")
        else -> BookingValidationResult.Valid
    }

    fun validateLocation(draft: BookingDraft): BookingValidationResult = when {
        draft.address.isBlank() -> BookingValidationResult.Invalid("Please enter the event address.")
        draft.area.isBlank() -> BookingValidationResult.Invalid("Please enter the area.")
        draft.city.isBlank() -> BookingValidationResult.Invalid("Please enter the city.")
        else -> BookingValidationResult.Valid
    }

    /**
     * Client-side estimate shown to the customer; it mirrors the amount sent as `estimatedAmount`.
     * Null when the plan has no published price (custom menu → "Price on quote").
     */
    fun estimate(draft: BookingDraft): CateringEstimate? {
        val plan = BookingOptions.planByBackendValue(draft.cateringPlan) ?: return null
        val perPerson = plan.pricePerPerson ?: return null
        val guests = draft.guestCount ?: return null
        return CateringEstimate(perPerson, guests, perPerson.toLong() * guests)
    }

    /** Guest stepper increments: fine control for small events, larger jumps for big ones. */
    fun guestStep(current: Int): Int = when {
        current < 50 -> 5
        current < 300 -> 10
        else -> 50
    }

    fun incrementGuests(current: Int?): Int {
        val value = current ?: 0
        return (value + guestStep(value)).coerceIn(1, BookingOptions.maxGuestCount)
    }

    fun decrementGuests(current: Int?): Int {
        val value = current ?: 0
        if (value <= 1) return 1
        val step = guestStep((value - 1).coerceAtLeast(0))
        return (value - step).coerceAtLeast(1)
    }

    /** Applies a guest count so the legacy preset/custom fields stay consistent. */
    fun withGuests(draft: BookingDraft, guests: Int): BookingDraft {
        val value = guests.coerceIn(1, BookingOptions.maxGuestCount)
        return if (value in BookingOptions.guestQuickOptions) {
            draft.copy(
                guestCount = value,
                guestCountSelection = BookingOptions.guestSelectionPreset,
                customGuestCountInput = ""
            )
        } else {
            draft.copy(
                guestCount = value,
                guestCountSelection = BookingOptions.guestSelectionCustom,
                customGuestCountInput = value.toString()
            )
        }
    }

    // ---- Process-death persistence (Bundle-compatible list of strings) ----

    private const val SAVE_VERSION = "v1"

    fun toSaveable(draft: BookingDraft): List<String> = listOf(
        SAVE_VERSION,
        draft.eventType.orEmpty(),
        draft.guestCount?.toString().orEmpty(),
        draft.guestCountSelection,
        draft.customGuestCountInput,
        draft.selectedFoodServices.sortedBy { it.sortOrder }.joinToString(",") { it.name },
        draft.cateringFoodType,
        draft.cateringPlan,
        draft.foodRequirements,
        draft.eventDate,
        draft.eventTime,
        draft.address,
        draft.area,
        draft.city,
        draft.landmark,
        draft.specialInstructions
    )

    fun fromSaveable(values: List<String>): BookingDraft? {
        if (values.size != 16 || values[0] != SAVE_VERSION) return null
        return BookingDraft(
            eventType = values[1].ifBlank { null },
            guestCount = values[2].toIntOrNull(),
            guestCountSelection = values[3].ifBlank { BookingOptions.guestSelectionPreset },
            customGuestCountInput = values[4],
            selectedFoodServices = values[5].split(',')
                .mapNotNull { name -> CateringMealServiceType.entries.firstOrNull { it.name == name } }
                .toSet(),
            cateringFoodType = values[6].ifBlank { BookingOptions.foodTypeNonVegetarian },
            cateringPlan = values[7],
            foodRequirements = values[8],
            eventDate = values[9],
            eventTime = values[10],
            address = values[11],
            area = values[12],
            city = values[13],
            landmark = values[14],
            specialInstructions = values[15]
        )
    }
}

data class CateringEstimate(val perPerson: Int, val guests: Int, val total: Long)

private inline fun BookingValidationResult.orElse(next: () -> BookingValidationResult): BookingValidationResult =
    if (this is BookingValidationResult.Invalid) this else next()

/* ------------------------------------------------------------------------------------------------
 * Quick time slots shared by every booking flow. Values are `HH:mm` like the existing pickers.
 * ---------------------------------------------------------------------------------------------- */
data class TimeSlot(val label: String, val time: String)

object BookingTimeSlots {
    val startSlots = listOf(
        TimeSlot("Breakfast", "08:00"),
        TimeSlot("Morning", "10:00"),
        TimeSlot("Lunch", "12:30"),
        TimeSlot("Afternoon", "15:00"),
        TimeSlot("Evening", "18:00"),
        TimeSlot("Dinner", "19:30")
    )

    val durationHours = listOf(2, 3, 4, 6, 8)

    /** End time `hours` after [start], or null when it would cross midnight (backend needs same-day end). */
    fun endAfter(start: String?, hours: Int): String? {
        val s = EventTimeRules.parse(start) ?: return null
        val minutes = s.hour * 60 + s.minute + hours * 60
        if (minutes >= 24 * 60) return null
        return EventTimeRules.toStorage(LocalTime.of(minutes / 60, minutes % 60))
    }

    /** Whole-hour duration between start and end, or null when not a whole number of hours. */
    fun durationOf(start: String?, end: String?): Int? {
        val s = EventTimeRules.parse(start) ?: return null
        val e = EventTimeRules.parse(end) ?: return null
        val diff = (e.toSecondOfDay() - s.toSecondOfDay()) / 60
        return if (diff > 0 && diff % 60 == 0) diff / 60 else null
    }
}

/* ------------------------------------------------------------------------------------------------
 * Non-catering service journeys (catering staff, decoration, entertainment, beauty, photography,
 * religious, rentals…). Steps are configured per category so customers only see relevant questions.
 * ---------------------------------------------------------------------------------------------- */
enum class ServiceJourneyStep { EVENT, SERVICES, WHEN_WHERE, REVIEW }

data class ServiceJourney(
    val categoryId: String,
    val steps: List<ServiceJourneyStep>,
    val eventHeading: String,
    val eventSubtitle: String,
    val servicesTitle: String,
    val servicesHeading: String,
    val servicesSubtitle: String,
    val whenWhereHeading: String
) {
    val reviewIndex: Int get() = steps.indexOf(ServiceJourneyStep.REVIEW)

    fun indexOf(step: ServiceJourneyStep): Int = steps.indexOf(step)

    fun stepTitles(): List<String> = steps.map {
        when (it) {
            ServiceJourneyStep.EVENT -> if (categoryId == "beauty") "Occasion" else "Event"
            ServiceJourneyStep.SERVICES -> servicesTitle
            ServiceJourneyStep.WHEN_WHERE -> "When & where"
            ServiceJourneyStep.REVIEW -> "Review"
        }
    }
}

object ServiceJourneyCatalog {
    private val defaultSteps = listOf(
        ServiceJourneyStep.EVENT,
        ServiceJourneyStep.SERVICES,
        ServiceJourneyStep.WHEN_WHERE,
        ServiceJourneyStep.REVIEW
    )

    fun forCategory(categoryId: String): ServiceJourney = when (categoryId) {
        "catering-staff" -> journey(
            categoryId,
            servicesTitle = "Staff",
            servicesHeading = "Who do you need?",
            servicesSubtitle = "Choose the roles and how many people you need for each."
        )
        "decoration" -> journey(
            categoryId,
            servicesTitle = "Decoration",
            servicesHeading = "Choose your decoration",
            servicesSubtitle = "Select the décor services for your venue. Pricing is confirmed by CaterHub."
        )
        "entertainment" -> journey(
            categoryId,
            servicesTitle = "Performers",
            servicesHeading = "Who will entertain your guests?",
            servicesSubtitle = "Pick one or more performers or services."
        )
        "photography-video" -> journey(
            categoryId,
            servicesTitle = "Coverage",
            servicesHeading = "How should we capture it?",
            servicesSubtitle = "Select photography, video or live-streaming coverage."
        )
        "religious-ceremony" -> journey(
            categoryId,
            eventHeading = "Which ceremony?",
            eventSubtitle = "Choose the ceremony or occasion so we can match the right purohit.",
            servicesTitle = "Purohit",
            servicesHeading = "Ceremony requirements",
            servicesSubtitle = "Confirm the purohit service you need."
        )
        "tent-tables-equipment" -> journey(
            categoryId,
            servicesTitle = "Rentals",
            servicesHeading = "What do you need delivered?",
            servicesSubtitle = "Set quantities for tables and chairs; tents and serving equipment are quoted."
        )
        // Beauty is personal: the customer picks the service first, then the occasion.
        "beauty" -> ServiceJourney(
            categoryId = categoryId,
            steps = listOf(
                ServiceJourneyStep.SERVICES,
                ServiceJourneyStep.EVENT,
                ServiceJourneyStep.WHEN_WHERE,
                ServiceJourneyStep.REVIEW
            ),
            eventHeading = "What's the occasion?",
            eventSubtitle = "Helps your artist plan the right look.",
            servicesTitle = "Services",
            servicesHeading = "Choose your beauty services",
            servicesSubtitle = "Select one or more services for your appointment.",
            whenWhereHeading = "Appointment details"
        )
        else -> journey(
            categoryId,
            servicesTitle = "Services",
            servicesHeading = "Choose your services",
            servicesSubtitle = "Select one or more services for your event."
        )
    }

    private fun journey(
        categoryId: String,
        servicesTitle: String,
        servicesHeading: String,
        servicesSubtitle: String,
        eventHeading: String = "What are you celebrating?",
        eventSubtitle: String = "Choose the occasion for this booking."
    ) = ServiceJourney(
        categoryId = categoryId,
        steps = defaultSteps,
        eventHeading = eventHeading,
        eventSubtitle = eventSubtitle,
        servicesTitle = servicesTitle,
        servicesHeading = servicesHeading,
        servicesSubtitle = servicesSubtitle,
        whenWhereHeading = "When & where?"
    )
}

/** A bookable line of a service category, derived from the existing customer catalogue. */
data class ServiceRequestLine(
    val id: String,
    val name: String,
    val description: String,
    /** Published unit price, or null when CaterHub quotes the price. */
    val unitPrice: Int?,
    val priceUnit: String,
    val workerType: WorkerType?
) {
    val quoteOnly: Boolean get() = unitPrice == null
}

object ServiceRequestCatalog {
    fun linesFor(categoryId: String): List<ServiceRequestLine> =
        ServiceCatalog.customerRolesForCategory(categoryId).distinctBy { it.id }.map { role ->
            ServiceRequestLine(
                id = role.id,
                name = role.title,
                description = ServiceListingCatalog.descriptionFor(categoryId, role.id),
                unitPrice = role.defaultUnitPrice,
                priceUnit = ServiceListingCatalog.priceUnitFor(categoryId, role),
                workerType = role.workerType
            )
        }
}

/**
 * Complete, saveable state of a non-catering service request. Held in `rememberSaveable`, so the
 * selection survives the OTP login round trip, configuration changes and process death.
 */
data class ServiceRequestDraft(
    val quantities: Map<String, Int> = emptyMap(),
    val eventType: String? = null,
    val eventDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val address: String = "",
    val area: String = "",
    val notes: String = ""
) {
    fun quantityOf(id: String): Int = quantities[id] ?: 0

    fun withQuantity(id: String, quantity: Int): ServiceRequestDraft {
        val value = quantity.coerceIn(0, MAX_QUANTITY)
        return copy(quantities = if (value == 0) quantities - id else quantities + (id to value))
    }

    fun selectedLines(lines: List<ServiceRequestLine>): List<ServiceRequestLine> =
        lines.filter { quantityOf(it.id) > 0 }

    fun hasSelection(lines: List<ServiceRequestLine>): Boolean = selectedLines(lines).isNotEmpty()

    fun hasQuoteServices(lines: List<ServiceRequestLine>): Boolean = selectedLines(lines).any { it.quoteOnly }

    /** Sum of published prices only; quote-only lines contribute nothing. */
    fun fixedTotal(lines: List<ServiceRequestLine>): Int =
        selectedLines(lines).sumOf { (it.unitPrice ?: 0) * quantityOf(it.id) }

    fun location(): String = listOf(area.trim(), address.trim()).filter { it.isNotEmpty() }.joinToString(", ")

    companion object {
        const val MAX_QUANTITY = 5000
        // Mirror backend column/DTO limits.
        const val ADDRESS_MAX = 255
        const val AREA_MAX = 100
        const val NOTES_MAX = 2000
        private const val SAVE_VERSION = "v1"

        fun initial(lines: List<ServiceRequestLine>, preselectedServiceId: String?): ServiceRequestDraft =
            if (preselectedServiceId != null && lines.any { it.id == preselectedServiceId }) {
                ServiceRequestDraft(quantities = mapOf(preselectedServiceId to 1))
            } else {
                ServiceRequestDraft()
            }

        fun toSaveable(draft: ServiceRequestDraft): List<String> = listOf(
            SAVE_VERSION,
            draft.quantities.entries.sortedBy { it.key }.joinToString(";") { "${it.key}=${it.value}" },
            draft.eventType.orEmpty(),
            draft.eventDate,
            draft.startTime,
            draft.endTime,
            draft.address,
            draft.area,
            draft.notes
        )

        fun fromSaveable(values: List<String>): ServiceRequestDraft? {
            if (values.size != 9 || values[0] != SAVE_VERSION) return null
            val quantities = values[1].split(';').mapNotNull { entry ->
                val parts = entry.split('=')
                val qty = parts.getOrNull(1)?.toIntOrNull()
                if (parts.size == 2 && parts[0].isNotBlank() && qty != null && qty > 0) parts[0] to qty else null
            }.toMap()
            return ServiceRequestDraft(
                quantities = quantities,
                eventType = values[2].ifBlank { null },
                eventDate = values[3],
                startTime = values[4],
                endTime = values[5],
                address = values[6],
                area = values[7],
                notes = values[8]
            )
        }
    }
}

object ServiceRequestValidator {
    fun validateServices(draft: ServiceRequestDraft, lines: List<ServiceRequestLine>): String? =
        if (!draft.hasSelection(lines)) "Please select at least one service." else null

    fun validateEvent(draft: ServiceRequestDraft): String? =
        if (draft.eventType.isNullOrBlank()) "Please select an event type." else null

    fun validateSchedule(draft: ServiceRequestDraft): String? = when {
        draft.eventDate.isBlank() || EventCalendarRules.parseIsoDate(draft.eventDate) == null -> "Please select a date."
        draft.startTime.isBlank() -> "Please select a start time."
        draft.endTime.isBlank() -> "Please select an end time."
        !EventTimeRules.isEndAfterStart(draft.startTime, draft.endTime) -> "End time must be after start time"
        !EventTimeRules.startsInFuture(draft.eventDate, draft.startTime) -> "Please choose a start time later than now."
        else -> null
    }

    fun validateLocation(draft: ServiceRequestDraft): String? = when {
        draft.address.trim().isEmpty() -> "Please enter the event address."
        draft.area.trim().isEmpty() -> "Please enter the area/locality."
        else -> null
    }

    fun validateStep(
        step: ServiceJourneyStep,
        draft: ServiceRequestDraft,
        lines: List<ServiceRequestLine>
    ): String? = when (step) {
        ServiceJourneyStep.EVENT -> validateEvent(draft)
        ServiceJourneyStep.SERVICES -> validateServices(draft, lines)
        ServiceJourneyStep.WHEN_WHERE -> validateSchedule(draft) ?: validateLocation(draft)
        ServiceJourneyStep.REVIEW -> validateForSubmit(draft, lines)
    }

    fun validateForSubmit(draft: ServiceRequestDraft, lines: List<ServiceRequestLine>): String? =
        validateServices(draft, lines) ?: validateEvent(draft) ?: validateSchedule(draft) ?: validateLocation(draft)
}
