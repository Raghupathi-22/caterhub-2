package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.BookingValidator
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
            serviceType = "Full Catering",
            guestCount = 100,
            foodType = "Dinner",
            eventDate = "2026-08-15",
            eventTime = "19:00",
            address = "Plot 10",
            area = "Kondapur",
            city = "Hyderabad",
            pincode = "500084",
            specialInstructions = "Need vegetarian buffet setup"
        )
        assertTrue(BookingValidator.validateForSubmit(draft) is BookingValidationResult.Valid)
    }

    @Test
    fun locationRequiresSixDigitPincode() {
        val draft = BookingDraft(
            eventType = "Birthday",
            serviceType = "Full Catering",
            guestCount = 100,
            foodType = "Dinner",
            eventDate = "2026-08-15",
            eventTime = "19:00",
            address = "Plot 10",
            area = "Kondapur",
            city = "Hyderabad",
            pincode = "5000"
        )
        val result = BookingValidator.validateForSubmit(draft)
        assertTrue(result is BookingValidationResult.Invalid)
        assertEquals("Please enter a valid 6-digit pincode.", (result as BookingValidationResult.Invalid).message)
    }
}

