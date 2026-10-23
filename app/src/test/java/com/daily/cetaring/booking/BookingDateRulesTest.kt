package com.daily.cetaring.booking

import com.daily.cetaring.presentation.components.BookingDateRules
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingDateRulesTest {

    private fun utcMillis(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `stored date round-trips through picker millis without day shift`() {
        val millis = BookingDateRules.utcMillisForIsoDate("2026-03-15")
        assertEquals(utcMillis(LocalDate.of(2026, 3, 15)), millis)
        assertEquals("2026-03-15", BookingDateRules.isoDateForUtcMillis(millis!!))
    }

    @Test
    fun `blank or invalid stored date has no initial selection`() {
        assertNull(BookingDateRules.utcMillisForIsoDate(""))
        assertNull(BookingDateRules.utcMillisForIsoDate("15/03/2026"))
    }

    @Test
    fun `today and future dates are selectable but past dates are not`() {
        val today = LocalDate.of(2026, 3, 15)
        assertTrue(BookingDateRules.isSelectableDate(utcMillis(today), today))
        assertTrue(BookingDateRules.isSelectableDate(utcMillis(today.plusDays(1)), today))
        assertFalse(BookingDateRules.isSelectableDate(utcMillis(today.minusDays(1)), today))
    }

    @Test
    fun `past years are not selectable`() {
        val today = LocalDate.of(2026, 3, 15)
        assertFalse(BookingDateRules.isSelectableYear(2025, today))
        assertTrue(BookingDateRules.isSelectableYear(2026, today))
        assertTrue(BookingDateRules.isSelectableYear(2027, today))
    }
}
