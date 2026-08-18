package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.StaffingRequirement
import com.daily.cetaring.data.remote.dto.WorkerType
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingValidatorTest {
    @Test
    fun eventTypeIsMandatory() {
        val result = BookingValidator.validateStep(0, BookingDraft())
        assertTrue(result is BookingValidationResult.Invalid)
        assertEquals("Please select an event type.", (result as BookingValidationResult.Invalid).message)
    }

    @Test
    fun guestCountMustBePositiveAndWithinBusinessLimit() {
        assertTrue(BookingValidator.validateGuests(0) is BookingValidationResult.Invalid)
        assertTrue(BookingValidator.validateGuests(BookingOptions.maxGuestCount + 1) is BookingValidationResult.Invalid)
        assertTrue(BookingValidator.validateGuests(100) is BookingValidationResult.Valid)
    }

    @Test
    fun fullDraftIsValidForSubmit() {
        val draft = BookingDraft(
            eventType = "Birthday",
            guestCount = 100,
            foodService = "Full Catering",
            eventDate = LocalDate.now().plusDays(1).toString(),
            eventTime = "19:00",
            address = "Plot 10",
            area = "Kondapur",
            city = "Hyderabad",
            specialInstructions = "Need vegetarian buffet setup"
        )
        assertTrue(BookingValidator.validateForSubmit(draft) is BookingValidationResult.Valid)
    }

    @Test
    fun staffingRequiresPositiveWorkerOfferAndEndTime() {
        val draft = BookingDraft(
            eventType = "Birthday",
            guestCount = 100,
            staffingRequirements = mapOf(
                WorkerType.SERVING_BOY to StaffingRequirement(quantity = 10, paymentPerWorker = BigDecimal("0"))
            ),
            eventDate = LocalDate.now().plusDays(1).toString(),
            eventTime = "19:00",
            address = "Plot 10",
            area = "Kondapur",
            city = "Hyderabad"
        )
        val result = BookingValidator.validateForSubmit(draft)
        assertTrue(result is BookingValidationResult.Invalid)
        assertEquals("Please enter a positive offer for every staff service.", (result as BookingValidationResult.Invalid).message)
    }

    @Test
    fun customGuestCountIsValidatedAsPositiveInteger() {
        assertTrue(BookingValidator.validateGuests(null) is BookingValidationResult.Invalid)
        assertTrue(BookingValidator.validateGuests(-1) is BookingValidationResult.Invalid)
        assertTrue(BookingValidator.validateGuests(175) is BookingValidationResult.Valid)
    }
}
