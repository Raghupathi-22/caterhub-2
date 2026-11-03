package com.daily.cetaring.booking

import com.daily.cetaring.presentation.components.EventCalendarRules
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventCalendarRulesTest {

    private val today = LocalDate.of(2026, 3, 15)

    @Test
    fun `month grid always has six full weeks of seven columns`() {
        listOf(YearMonth.of(2026, 2), YearMonth.of(2026, 3), YearMonth.of(2024, 2), YearMonth.of(2026, 8)).forEach { month ->
            val grid = EventCalendarRules.monthGrid(month, DayOfWeek.MONDAY)
            assertEquals(42, grid.size)
            assertEquals(month.lengthOfMonth(), grid.filterNotNull().size)
        }
    }

    @Test
    fun `first day lands in the correct weekday column`() {
        // 1 March 2026 is a Sunday.
        val mondayFirst = EventCalendarRules.monthGrid(YearMonth.of(2026, 3), DayOfWeek.MONDAY)
        assertEquals(6, mondayFirst.indexOfFirst { it != null })
        val sundayFirst = EventCalendarRules.monthGrid(YearMonth.of(2026, 3), DayOfWeek.SUNDAY)
        assertEquals(0, sundayFirst.indexOfFirst { it != null })
        assertEquals(LocalDate.of(2026, 3, 1), sundayFirst[0])
    }

    @Test
    fun `every cell sits under its matching weekday header`() {
        val first = DayOfWeek.SUNDAY
        val headers = EventCalendarRules.weekdays(first)
        assertEquals(7, headers.size)
        assertEquals(DayOfWeek.SUNDAY, headers.first())
        assertEquals(DayOfWeek.SATURDAY, headers.last())
        EventCalendarRules.monthGrid(YearMonth.of(2026, 5), first).forEachIndexed { index, date ->
            if (date != null) assertEquals(headers[index % 7], date.dayOfWeek)
        }
    }

    @Test
    fun `days are consecutive with no gaps or duplicates`() {
        val dates = EventCalendarRules.monthGrid(YearMonth.of(2024, 2), DayOfWeek.MONDAY).filterNotNull()
        dates.zipWithNext().forEach { (a, b) -> assertEquals(a.plusDays(1), b) }
        assertEquals(29, dates.last().dayOfMonth)
    }

    @Test
    fun `today and future dates are selectable but past dates are not`() {
        assertTrue(EventCalendarRules.isSelectable(today, today))
        assertTrue(EventCalendarRules.isSelectable(today.plusDays(1), today))
        assertFalse(EventCalendarRules.isSelectable(today.minusDays(1), today))
        assertFalse(EventCalendarRules.isSelectable(LocalDate.of(2025, 12, 31), today))
    }

    @Test
    fun `month navigation cannot go before the current month`() {
        assertFalse(EventCalendarRules.canGoToPreviousMonth(YearMonth.of(2026, 3), today))
        assertTrue(EventCalendarRules.canGoToPreviousMonth(YearMonth.of(2026, 4), today))
        assertEquals(YearMonth.of(2026, 3), EventCalendarRules.clampMonth(YearMonth.of(2025, 11), today))
        assertEquals(YearMonth.of(2027, 1), EventCalendarRules.clampMonth(YearMonth.of(2027, 1), today))
    }

    @Test
    fun `year choices start at the current year`() {
        val years = EventCalendarRules.selectableYears(today)
        assertEquals(2026, years.first())
        assertEquals(EventCalendarRules.YEAR_CHOICES, years.size)
    }

    @Test
    fun `initial month follows the selected date or falls back to today`() {
        assertEquals(YearMonth.of(2026, 7), EventCalendarRules.initialMonth("2026-07-04", today))
        assertEquals(YearMonth.of(2026, 3), EventCalendarRules.initialMonth(null, today))
        assertEquals(YearMonth.of(2026, 3), EventCalendarRules.initialMonth("2025-01-10", today))
    }

    @Test
    fun `stored ISO dates round-trip without day shift`() {
        val date = EventCalendarRules.parseIsoDate("2026-03-15")
        assertEquals(LocalDate.of(2026, 3, 15), date)
        assertEquals("2026-03-15", EventCalendarRules.toIsoDate(date!!))
        // Late-evening and year-boundary dates must not shift.
        assertEquals("2026-12-31", EventCalendarRules.toIsoDate(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `blank or invalid stored date has no selection`() {
        assertNull(EventCalendarRules.parseIsoDate(""))
        assertNull(EventCalendarRules.parseIsoDate(null))
        assertNull(EventCalendarRules.parseIsoDate("15/03/2026"))
    }

    @Test
    fun `quick-pick dates start today and are consecutive`() {
        val upcoming = EventCalendarRules.upcomingDates(today, 14)
        assertEquals(14, upcoming.size)
        assertEquals(today, upcoming.first())
        assertEquals(today.plusDays(13), upcoming.last())
    }

    @Test
    fun `first day of week follows the locale`() {
        assertEquals(DayOfWeek.SUNDAY, EventCalendarRules.firstDayOfWeek(Locale.US))
        assertEquals(DayOfWeek.MONDAY, EventCalendarRules.firstDayOfWeek(Locale.UK))
    }
}
