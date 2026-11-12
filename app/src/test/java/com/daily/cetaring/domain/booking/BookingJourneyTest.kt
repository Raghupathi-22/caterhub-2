package com.daily.cetaring.domain.booking

import com.daily.cetaring.R
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.BookingImagery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CateringJourneyTest {

    private val futureDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(10).toString()

    private fun completeDraft() = BookingDraft(
        eventType = "Wedding",
        guestCount = 100,
        guestCountSelection = BookingOptions.guestSelectionPreset,
        selectedFoodServices = setOf(CateringMealServiceType.LUNCH, CateringMealServiceType.DINNER),
        cateringFoodType = BookingOptions.foodTypeVegetarian,
        cateringPlan = BookingOptions.planVegClassic,
        eventDate = futureDate,
        eventTime = "12:30",
        address = "12 Lake Road",
        area = "Madhapur",
        city = "Hyderabad",
        landmark = "Near metro",
        specialInstructions = "Gate 2"
    )

    @Test
    fun saveableRoundTripPreservesEveryField() {
        val draft = completeDraft().copy(foodRequirements = "Less spicy")
        assertEquals(draft, CateringJourney.fromSaveable(CateringJourney.toSaveable(draft)))
    }

    @Test
    fun saveableRejectsMalformedData() {
        assertNull(CateringJourney.fromSaveable(emptyList()))
        assertNull(CateringJourney.fromSaveable(listOf("v0") + List(15) { "" }))
        assertNull(CateringJourney.fromSaveable(listOf("v1", "x")))
    }

    @Test
    fun saveableIgnoresUnknownMealTypes() {
        val values = CateringJourney.toSaveable(completeDraft()).toMutableList()
        values[5] = "LUNCH,UNKNOWN"
        assertEquals(setOf(CateringMealServiceType.LUNCH), CateringJourney.fromSaveable(values)!!.selectedFoodServices)
    }

    @Test
    fun packageStepRequiresPlanAndCustomMenuDishes() {
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_PACKAGE, completeDraft().copy(cateringPlan = "")) is BookingValidationResult.Invalid)
        val custom = completeDraft().copy(cateringPlan = BookingOptions.planCustomMenu, foodRequirements = "")
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_PACKAGE, custom) is BookingValidationResult.Invalid)
        assertEquals(
            BookingValidationResult.Valid,
            CateringJourney.validateUiStep(CateringJourney.STEP_PACKAGE, custom.copy(foodRequirements = "Biryani"))
        )
        assertEquals(BookingValidationResult.Valid, CateringJourney.validateUiStep(CateringJourney.STEP_PACKAGE, completeDraft()))
    }

    @Test
    fun whenWhereStepRequiresScheduleAndLocation() {
        val draft = completeDraft()
        assertEquals(BookingValidationResult.Valid, CateringJourney.validateUiStep(CateringJourney.STEP_WHEN_WHERE, draft))
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_WHEN_WHERE, draft.copy(eventDate = "")) is BookingValidationResult.Invalid)
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_WHEN_WHERE, draft.copy(address = " ")) is BookingValidationResult.Invalid)
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_WHEN_WHERE, draft.copy(area = "")) is BookingValidationResult.Invalid)
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_WHEN_WHERE, draft.copy(city = "")) is BookingValidationResult.Invalid)
    }

    @Test
    fun earlyStepsAndReviewUseLegacyValidator() {
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_EVENT, completeDraft().copy(eventType = null)) is BookingValidationResult.Invalid)
        assertTrue(CateringJourney.validateUiStep(CateringJourney.STEP_FOOD_SERVICES, completeDraft().copy(selectedFoodServices = emptySet())) is BookingValidationResult.Invalid)
        assertEquals(BookingValidationResult.Valid, CateringJourney.validateUiStep(CateringJourney.STEP_REVIEW, completeDraft()))
    }

    @Test
    fun estimateMultipliesPublishedPriceByGuests() {
        val estimate = CateringJourney.estimate(completeDraft())!!
        assertEquals(599, estimate.perPerson)
        assertEquals(100, estimate.guests)
        assertEquals(59_900L, estimate.total)
        assertEquals(
            1_998_000L,
            CateringJourney.estimate(completeDraft().copy(cateringPlan = BookingOptions.planNonVegPremium, guestCount = 2000))!!.total
        )
    }

    @Test
    fun estimateIsNullForCustomMenuOrMissingGuests() {
        assertNull(CateringJourney.estimate(completeDraft().copy(cateringPlan = BookingOptions.planCustomMenu)))
        assertNull(CateringJourney.estimate(completeDraft().copy(guestCount = null)))
        assertNull(CateringJourney.estimate(completeDraft().copy(cateringPlan = "")))
    }

    @Test
    fun guestStepperStaysWithinBounds() {
        assertEquals(5, CateringJourney.incrementGuests(null))
        assertEquals(1, CateringJourney.decrementGuests(null))
        assertEquals(1, CateringJourney.decrementGuests(1))
        assertEquals(1, CateringJourney.decrementGuests(3))
        assertEquals(50, CateringJourney.incrementGuests(45))
        assertEquals(60, CateringJourney.incrementGuests(50))
        assertEquals(290, CateringJourney.decrementGuests(300))
        assertEquals(BookingOptions.maxGuestCount, CateringJourney.incrementGuests(BookingOptions.maxGuestCount))
    }

    @Test
    fun withGuestsKeepsPresetAndCustomFieldsConsistent() {
        val preset = CateringJourney.withGuests(BookingDraft(), 100)
        assertEquals(BookingOptions.guestSelectionPreset, preset.guestCountSelection)
        assertEquals("", preset.customGuestCountInput)
        val custom = CateringJourney.withGuests(BookingDraft(), 135)
        assertEquals(BookingOptions.guestSelectionCustom, custom.guestCountSelection)
        assertEquals("135", custom.customGuestCountInput)
        assertEquals(BookingOptions.maxGuestCount, CateringJourney.withGuests(BookingDraft(), 99_999).guestCount)
        assertEquals(1, CateringJourney.withGuests(BookingDraft(), 0).guestCount)
    }
}

class BookingTimeSlotsTest {
    @Test
    fun endAfterAddsHoursWithinTheSameDay() {
        assertEquals("21:00", BookingTimeSlots.endAfter("18:00", 3))
        assertEquals("23:30", BookingTimeSlots.endAfter("19:30", 4))
    }

    @Test
    fun endAfterRejectsMidnightCrossingAndInvalidStart() {
        assertNull(BookingTimeSlots.endAfter("19:30", 6))
        assertNull(BookingTimeSlots.endAfter("20:00", 4))
        assertNull(BookingTimeSlots.endAfter(null, 2))
        assertNull(BookingTimeSlots.endAfter("bad", 2))
    }

    @Test
    fun durationOfReturnsWholeHoursOnly() {
        assertEquals(3, BookingTimeSlots.durationOf("18:00", "21:00"))
        assertNull(BookingTimeSlots.durationOf("18:00", "20:30"))
        assertNull(BookingTimeSlots.durationOf("21:00", "18:00"))
        assertNull(BookingTimeSlots.durationOf(null, "18:00"))
    }

    @Test
    fun slotsAreValidStorageTimes() {
        BookingTimeSlots.startSlots.forEach { assertTrue(it.time.matches(Regex("\\d{2}:\\d{2}"))) }
    }
}

class ServiceJourneyCatalogTest {
    @Test
    fun everyCategoryHasACompleteJourneyEndingInReview() {
        ServiceCatalog.categories.forEach { category ->
            val journey = ServiceJourneyCatalog.forCategory(category.id)
            assertEquals(category.id, ServiceJourneyStep.entries.toSet(), journey.steps.toSet())
            assertEquals(journey.steps.lastIndex, journey.reviewIndex)
            assertEquals(journey.steps.size, journey.stepTitles().size)
        }
    }

    @Test
    fun beautyAsksForServicesBeforeOccasion() {
        val beauty = ServiceJourneyCatalog.forCategory("beauty")
        assertEquals(ServiceJourneyStep.SERVICES, beauty.steps.first())
        assertEquals("Occasion", beauty.stepTitles()[beauty.indexOf(ServiceJourneyStep.EVENT)])
    }

    @Test
    fun eventDrivenCategoriesStartWithTheEvent() {
        listOf("catering-staff", "decoration", "entertainment", "photography-video", "religious-ceremony").forEach {
            assertEquals(it, ServiceJourneyStep.EVENT, ServiceJourneyCatalog.forCategory(it).steps.first())
        }
        assertEquals("Which ceremony?", ServiceJourneyCatalog.forCategory("religious-ceremony").eventHeading)
    }
}

class ServiceRequestDraftTest {
    private val lines = listOf(
        ServiceRequestLine("waiter", "Waiter", "", 800, "person", null),
        ServiceRequestLine("tent", "Tent", "", null, "event", null)
    )
    private val futureDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(5).toString()

    private fun complete() = ServiceRequestDraft(
        quantities = mapOf("waiter" to 3, "tent" to 1),
        eventType = "Birthday",
        eventDate = futureDate,
        startTime = "18:00",
        endTime = "21:00",
        address = "Plot 4; Road=2",
        area = "Kondapur",
        notes = "Bring extra trays"
    )

    @Test
    fun saveableRoundTrip() {
        val draft = complete()
        assertEquals(draft, ServiceRequestDraft.fromSaveable(ServiceRequestDraft.toSaveable(draft)))
        assertEquals(ServiceRequestDraft(), ServiceRequestDraft.fromSaveable(ServiceRequestDraft.toSaveable(ServiceRequestDraft())))
    }

    @Test
    fun saveableRejectsMalformedInput() {
        assertNull(ServiceRequestDraft.fromSaveable(listOf("v1")))
        assertNull(ServiceRequestDraft.fromSaveable(listOf("v2") + List(8) { "" }))
        val values = ServiceRequestDraft.toSaveable(complete()).toMutableList()
        values[1] = "waiter=abc;=3;tent=1;bad"
        assertEquals(mapOf("tent" to 1), ServiceRequestDraft.fromSaveable(values)!!.quantities)
    }

    @Test
    fun withQuantityClampsAndRemoves() {
        val draft = ServiceRequestDraft().withQuantity("waiter", 99_999)
        assertEquals(ServiceRequestDraft.MAX_QUANTITY, draft.quantityOf("waiter"))
        assertFalse(draft.withQuantity("waiter", 0).quantities.containsKey("waiter"))
        assertFalse(draft.withQuantity("waiter", -4).quantities.containsKey("waiter"))
    }

    @Test
    fun totalsIgnoreQuoteOnlyLines() {
        val draft = complete()
        assertEquals(2400, draft.fixedTotal(lines))
        assertTrue(draft.hasQuoteServices(lines))
        assertFalse(draft.withQuantity("tent", 0).hasQuoteServices(lines))
        assertEquals("Kondapur, Plot 4; Road=2", draft.location())
    }

    @Test
    fun selectionIgnoresIdsNotInCatalogue() {
        val draft = ServiceRequestDraft(quantities = mapOf("unknown" to 2))
        assertFalse(draft.hasSelection(lines))
        assertEquals(0, draft.fixedTotal(lines))
    }

    @Test
    fun initialPreselectsOnlyKnownServices() {
        assertEquals(mapOf("tent" to 1), ServiceRequestDraft.initial(lines, "tent").quantities)
        assertTrue(ServiceRequestDraft.initial(lines, "nope").quantities.isEmpty())
        assertTrue(ServiceRequestDraft.initial(lines, null).quantities.isEmpty())
    }

    @Test
    fun validatorMessagesMatchEachStep() {
        val draft = complete()
        assertNull(ServiceRequestValidator.validateForSubmit(draft, lines))
        assertEquals("Please select at least one service.", ServiceRequestValidator.validateStep(ServiceJourneyStep.SERVICES, ServiceRequestDraft(), lines))
        assertEquals("Please select an event type.", ServiceRequestValidator.validateStep(ServiceJourneyStep.EVENT, draft.copy(eventType = null), lines))
        assertEquals("Please select a date.", ServiceRequestValidator.validateSchedule(draft.copy(eventDate = "")))
        assertEquals("Please select a start time.", ServiceRequestValidator.validateSchedule(draft.copy(startTime = "")))
        assertEquals("Please select an end time.", ServiceRequestValidator.validateSchedule(draft.copy(endTime = "")))
        assertEquals("End time must be after start time", ServiceRequestValidator.validateSchedule(draft.copy(endTime = "17:00")))
        assertEquals("Please choose a start time later than now.", ServiceRequestValidator.validateSchedule(draft.copy(eventDate = "2020-01-01")))
        assertEquals("Please enter the event address.", ServiceRequestValidator.validateStep(ServiceJourneyStep.WHEN_WHERE, draft.copy(address = "  "), lines))
        assertEquals("Please enter the area/locality.", ServiceRequestValidator.validateStep(ServiceJourneyStep.WHEN_WHERE, draft.copy(area = ""), lines))
    }

    @Test
    fun catalogueLinesMirrorCustomerRoles() {
        ServiceCatalog.categories.forEach { category ->
            val roles = ServiceCatalog.customerRolesForCategory(category.id).distinctBy { it.id }
            val catalogLines = ServiceRequestCatalog.linesFor(category.id)
            assertEquals(roles.map { it.id }, catalogLines.map { it.id })
            catalogLines.zip(roles).forEach { (line, role) ->
                assertEquals(role.defaultUnitPrice, line.unitPrice)
                assertEquals(role.quoteOnly, line.quoteOnly)
                assertEquals(role.workerType, line.workerType)
            }
        }
    }
}

class BookingImageryTest {
    @Test
    fun everyEventHasSpecificPhotography() {
        EventTypeCatalog.eventTypes.forEach { event ->
            val photo = BookingImagery.eventPhoto(event.id)
            assertNotEquals(event.id, 0, photo)
            if (event.id != "other") assertNotEquals(event.id, R.drawable.public_offer_family, photo)
        }
    }

    @Test
    fun everyMealAndPlanHasPhotography() {
        CateringMealServiceType.entries.forEach { assertNotEquals(0, BookingImagery.mealPhoto(it)) }
        BookingOptions.cateringPlanOptions.forEach { assertNotEquals(0, BookingImagery.packagePhoto(it)) }
    }
}
