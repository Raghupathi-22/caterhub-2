package com.daily.cetaring.booking

import com.daily.cetaring.presentation.components.EventTimeRules
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTimeRulesTest {

    @Test
    fun `12-hour selections convert to 24-hour storage`() {
        assertEquals("00:00", EventTimeRules.toStorage(EventTimeRules.of12Hour(12, 0, pm = false)))
        assertEquals("12:00", EventTimeRules.toStorage(EventTimeRules.of12Hour(12, 0, pm = true)))
        assertEquals("09:05", EventTimeRules.toStorage(EventTimeRules.of12Hour(9, 5, pm = false)))
        assertEquals("21:45", EventTimeRules.toStorage(EventTimeRules.of12Hour(9, 45, pm = true)))
    }

    @Test
    fun `stored times split back into 12-hour parts`() {
        val midnight = LocalTime.of(0, 30)
        assertEquals(12, EventTimeRules.hour12(midnight))
        assertFalse(EventTimeRules.isPm(midnight))
        val evening = LocalTime.of(18, 15)
        assertEquals(6, EventTimeRules.hour12(evening))
        assertTrue(EventTimeRules.isPm(evening))
    }

    @Test
    fun `display uses consistent 12-hour AM PM format`() {
        assertEquals("6:00 PM", EventTimeRules.display("18:00"))
        assertEquals("12:00 AM", EventTimeRules.display("00:00"))
        assertEquals("12:30 PM", EventTimeRules.display("12:30"))
        assertNull(EventTimeRules.display(""))
        assertNull(EventTimeRules.display("6 pm"))
    }

    @Test
    fun `end time must be strictly after start time`() {
        assertTrue(EventTimeRules.isEndAfterStart("10:00", "13:00"))
        assertFalse(EventTimeRules.isEndAfterStart("13:00", "13:00"))
        assertFalse(EventTimeRules.isEndAfterStart("18:00", "09:00"))
        assertFalse(EventTimeRules.isEndAfterStart("", "09:00"))
        assertFalse(EventTimeRules.isEndAfterStart("10:00", null))
    }

    @Test
    fun `picker offers every hour and five-minute steps`() {
        assertEquals((1..12).toList(), EventTimeRules.hourOptions)
        assertEquals(12, EventTimeRules.minuteOptions.size)
        assertEquals(0, EventTimeRules.minuteOptions.first())
        assertEquals(55, EventTimeRules.minuteOptions.last())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid hour is rejected`() {
        EventTimeRules.of12Hour(13, 0, pm = false)
    }
}
