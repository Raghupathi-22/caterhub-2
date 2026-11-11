package com.daily.cetaring.presentation.components

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Pure calendar rules for the event-date picker.
 *
 * Dates are handled as [LocalDate] in the device's local calendar and stored as ISO `yyyy-MM-dd`,
 * so there is no millisecond/UTC conversion that could shift the chosen day.
 */
object EventCalendarRules {
    const val WEEK_LENGTH = 7
    const val GRID_ROWS = 6
    const val YEAR_CHOICES = 10

    fun firstDayOfWeek(locale: Locale = Locale.getDefault()): DayOfWeek =
        WeekFields.of(locale).firstDayOfWeek

    /** The seven weekday columns, starting on [firstDayOfWeek]. */
    fun weekdays(firstDayOfWeek: DayOfWeek): List<DayOfWeek> =
        (0 until WEEK_LENGTH).map { firstDayOfWeek.plus(it.toLong()) }

    /**
     * A fixed 6×7 grid for [month]: leading and trailing cells outside the month are null, so the
     * calendar height never jumps while navigating between months.
     */
    fun monthGrid(month: YearMonth, firstDayOfWeek: DayOfWeek): List<LocalDate?> {
        val first = month.atDay(1)
        val leading = Math.floorMod(first.dayOfWeek.value - firstDayOfWeek.value, WEEK_LENGTH)
        val cells = ArrayList<LocalDate?>(GRID_ROWS * WEEK_LENGTH)
        repeat(leading) { cells += null }
        for (day in 1..month.lengthOfMonth()) cells += month.atDay(day)
        while (cells.size < GRID_ROWS * WEEK_LENGTH) cells += null
        return cells
    }

    /** New bookings can be made for today or later, judged by the local calendar date. */
    fun isSelectable(date: LocalDate, today: LocalDate = LocalDate.now()): Boolean = !date.isBefore(today)

    fun canGoToPreviousMonth(displayed: YearMonth, today: LocalDate = LocalDate.now()): Boolean =
        displayed.isAfter(YearMonth.from(today))

    /** Keeps month navigation from landing on a month that is entirely in the past. */
    fun clampMonth(month: YearMonth, today: LocalDate = LocalDate.now()): YearMonth =
        maxOf(month, YearMonth.from(today))

    fun selectableYears(today: LocalDate = LocalDate.now(), count: Int = YEAR_CHOICES): List<Int> =
        (0 until count).map { today.year + it }

    /** Upcoming dates for the quick-pick strip, starting today. */
    fun upcomingDates(today: LocalDate = LocalDate.now(), count: Int = 14): List<LocalDate> =
        (0 until count).map { today.plusDays(it.toLong()) }

    fun parseIsoDate(value: String?): LocalDate? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun toIsoDate(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    /** Month to show when the picker opens: the selected date's month, or the current month. */
    fun initialMonth(selectedIsoDate: String?, today: LocalDate = LocalDate.now()): YearMonth =
        clampMonth(parseIsoDate(selectedIsoDate)?.let(YearMonth::from) ?: YearMonth.from(today), today)
}

/** 12-hour presentation and validation for times stored as `HH:mm`. */
object EventTimeRules {
    private val storage = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
    private val display = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    val minuteOptions: List<Int> = (0 until 60 step 5).toList()
    val hourOptions: List<Int> = (1..12).toList()

    fun parse(value: String?): LocalTime? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it, storage) }.getOrNull() }

    fun toStorage(time: LocalTime): String = time.format(storage)

    fun display(value: String?): String? = parse(value)?.format(display)

    fun hour12(time: LocalTime): Int = (time.hour % 12).let { if (it == 0) 12 else it }

    fun isPm(time: LocalTime): Boolean = time.hour >= 12

    fun of12Hour(hour12: Int, minute: Int, pm: Boolean): LocalTime {
        require(hour12 in 1..12) { "hour12 must be 1..12" }
        require(minute in 0..59) { "minute must be 0..59" }
        val hour24 = (hour12 % 12) + if (pm) 12 else 0
        return LocalTime.of(hour24, minute)
    }

    /** End time must be strictly later than the start time on the same day. */
    fun isEndAfterStart(start: String?, end: String?): Boolean {
        val s = parse(start) ?: return false
        val e = parse(end) ?: return false
        return e.isAfter(s)
    }

    /**
     * Mirrors the backend rule: the event must start after "now" in India (events are local to IST),
     * so a booking for earlier today is rejected before it reaches the server.
     */
    fun startsInFuture(
        isoDate: String?,
        start: String?,
        now: java.time.LocalDateTime = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
    ): Boolean {
        val date = EventCalendarRules.parseIsoDate(isoDate) ?: return false
        val time = parse(start) ?: return false
        return date.atTime(time).isAfter(now)
    }
}
