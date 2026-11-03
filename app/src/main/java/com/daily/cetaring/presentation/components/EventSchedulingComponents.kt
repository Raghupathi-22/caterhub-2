package com.daily.cetaring.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val PanelShape = RoundedCornerShape(28.dp)
private const val MaxGridFontScale = 1.3f

/** Caps font scaling inside dense grids so labels never overflow their fixed-ratio cells. */
@Composable
private fun ClampedFontScale(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(MaxGridFontScale)),
        content = content
    )
}

/**
 * Custom month calendar. Each of the seven weekday columns gets an equal `weight(1f)` share of
 * the available width, so the grid always fits (no fixed 48dp cells that overflow narrow dialogs).
 */
@Composable
fun EventCalendarPanel(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    val locale = Locale.getDefault()
    val firstDayOfWeek = remember(locale) { EventCalendarRules.firstDayOfWeek(locale) }
    var monthKey by rememberSaveable {
        mutableStateOf(EventCalendarRules.initialMonth(selectedDate?.toString(), today).toString())
    }
    val month = YearMonth.parse(monthKey)
    var showYears by rememberSaveable { mutableStateOf(false) }
    val canGoBack = EventCalendarRules.canGoToPreviousMonth(month, today)
    fun goTo(target: YearMonth) {
        monthKey = EventCalendarRules.clampMonth(target, today).toString()
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val monthTitle = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .selectable(selected = showYears, role = Role.Button, onClick = { showYears = !showYears })
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 6.dp)
                    .semantics {
                        heading()
                        contentDescription = "$monthTitle. ${if (showYears) "Hide" else "Choose"} year"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    monthTitle,
                    color = BookingPalette.Ink,
                    // Narrow phones: a smaller title keeps "September 2026" on one line beside the nav buttons.
                    style = if (LocalConfiguration.current.screenWidthDp < 360) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    if (showYears) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    null,
                    tint = BookingPalette.ChampagneDeep
                )
            }
            if (!showYears) {
                CalendarNavButton(
                    icon = Icons.Filled.ChevronLeft,
                    description = "Previous month",
                    enabled = canGoBack,
                    onClick = { goTo(month.minusMonths(1)) }
                )
                CalendarNavButton(
                    icon = Icons.Filled.ChevronRight,
                    description = "Next month",
                    enabled = true,
                    onClick = { goTo(month.plusMonths(1)) }
                )
            }
        }

        if (showYears) {
            YearGrid(
                years = EventCalendarRules.selectableYears(today),
                selectedYear = month.year,
                onYearSelected = { year ->
                    goTo(YearMonth.of(year, month.month))
                    showYears = false
                }
            )
        } else {
            ClampedFontScale {
                WeekdayHeader(firstDayOfWeek, locale)
                var dragTotal by remember { mutableFloatStateOf(0f) }
                val swipeThreshold = with(LocalDensity.current) { 56.dp.toPx() }
                AnimatedContent(
                    targetState = month,
                    transitionSpec = {
                        val forward = targetState > initialState
                        (slideInHorizontally(tween(260)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(260)))
                            .togetherWith(
                                slideOutHorizontally(tween(200)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(160))
                            )
                    },
                    label = "calendarMonth",
                    modifier = Modifier.pointerInput(monthKey) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragTotal = 0f },
                            onDragEnd = {
                                when {
                                    dragTotal < -swipeThreshold -> goTo(month.plusMonths(1))
                                    dragTotal > swipeThreshold && canGoBack -> goTo(month.minusMonths(1))
                                }
                            },
                            onHorizontalDrag = { _, amount -> dragTotal += amount }
                        )
                    }
                ) { shownMonth ->
                    MonthGrid(
                        month = shownMonth,
                        firstDayOfWeek = firstDayOfWeek,
                        selectedDate = selectedDate,
                        today = today,
                        locale = locale,
                        onDateSelected = onDateSelected
                    )
                }
            }
            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = BookingPalette.Emerald,
            disabledContentColor = BookingPalette.Ink.copy(alpha = 0.24f)
        )
    ) { Icon(icon, contentDescription = description) }
}

@Composable
private fun WeekdayHeader(firstDayOfWeek: java.time.DayOfWeek, locale: Locale) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val narrow = maxWidth / 7 < 40.dp
        Row(Modifier.fillMaxWidth()) {
            EventCalendarRules.weekdays(firstDayOfWeek).forEach { day ->
                Text(
                    day.getDisplayName(if (narrow) TextStyle.NARROW else TextStyle.SHORT, locale),
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .semantics { contentDescription = day.getDisplayName(TextStyle.FULL, locale) }
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    firstDayOfWeek: java.time.DayOfWeek,
    selectedDate: LocalDate?,
    today: LocalDate,
    locale: Locale,
    onDateSelected: (LocalDate) -> Unit
) {
    val cells = remember(month, firstDayOfWeek) { EventCalendarRules.monthGrid(month, firstDayOfWeek) }
    val longFormat = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale) }
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        cells.chunked(EventCalendarRules.WEEK_LENGTH).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                selected = date == selectedDate,
                                isToday = date == today,
                                enabled = EventCalendarRules.isSelectable(date, today),
                                description = date.format(longFormat),
                                onClick = { onDateSelected(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    description: String,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        if (selected) BookingPalette.Emerald else Color.Transparent, tween(200), label = "dayBg"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(background)
            .then(
                if (isToday && !selected) Modifier.border(1.5.dp, BookingPalette.Champagne, CircleShape) else Modifier
            )
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = buildString {
                    append(description)
                    if (isToday) append(", today")
                }
                stateDescription = when {
                    selected -> "Selected"
                    !enabled -> "Unavailable, past date"
                    else -> "Available"
                }
            }
            .alpha(if (enabled) 1f else 0.32f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            date.dayOfMonth.toString(),
            color = when {
                selected -> Color.White
                isToday -> BookingPalette.ChampagneDeep
                else -> BookingPalette.Ink
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot("Today") { Modifier.border(1.5.dp, BookingPalette.Champagne, CircleShape) }
        LegendDot("Selected") { Modifier.background(BookingPalette.Emerald) }
    }
}

@Composable
private fun LegendDot(label: String, decoration: () -> Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).then(decoration()))
        Spacer(Modifier.width(6.dp))
        Text(label, color = BookingPalette.Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun YearGrid(years: List<Int>, selectedYear: Int, onYearSelected: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        years.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { year ->
                    ChoiceChip(
                        text = year.toString(),
                        selected = year == selectedYear,
                        onClick = { onYearSelected(year) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val background by animateColorAsState(
        if (selected) BookingPalette.Emerald else Color.White, tween(180), label = "chipBg"
    )
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(1.dp, if (selected) BookingPalette.Emerald else BookingPalette.Border, RoundedCornerShape(14.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else BookingPalette.Ink,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}

/** Shared dialog chrome: edge-safe width (16dp margins, max 420dp), scrollable body, action footer. */
@Composable
private fun SchedulingDialog(
    onDismiss: () -> Unit,
    title: String,
    summary: String?,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = PanelShape,
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Column {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(BookingPalette.Ivory)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        title.uppercase(),
                        color = BookingPalette.ChampagneDeep,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        summary ?: "Nothing selected yet",
                        color = if (summary != null) BookingPalette.Ink else BookingPalette.Placeholder,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) { content() }
                HorizontalDivider(color = BookingPalette.Border)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Ink)
                    ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = BookingPalette.Emerald,
                            disabledContentColor = BookingPalette.DisabledContent
                        )
                    ) { Text("Confirm", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun EventDatePickerDialog(
    initialIsoDate: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    today: LocalDate = LocalDate.now()
) {
    var pendingIso by rememberSaveable {
        mutableStateOf(EventCalendarRules.parseIsoDate(initialIsoDate)?.takeIf { EventCalendarRules.isSelectable(it, today) }?.toString())
    }
    val pending = EventCalendarRules.parseIsoDate(pendingIso)
    SchedulingDialog(
        onDismiss = onDismiss,
        title = "Event date",
        summary = pending?.let(::formatLongDate),
        confirmEnabled = pending != null,
        onConfirm = { pending?.let { onConfirm(EventCalendarRules.toIsoDate(it)) } }
    ) {
        EventCalendarPanel(
            selectedDate = pending,
            onDateSelected = { pendingIso = it.toString() },
            today = today
        )
    }
}

/**
 * Compact 12-hour time picker: hours and 5-minute steps in equal-width grids plus an AM/PM toggle.
 * Unlike the fixed 256dp Material clock dial it scales to any dialog width.
 */
@Composable
fun EventTimePickerDialog(
    title: String,
    initialTime: String?,
    defaultTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val start = EventTimeRules.parse(initialTime) ?: defaultTime
    var hour12 by rememberSaveable { mutableIntStateOf(EventTimeRules.hour12(start)) }
    var minute by rememberSaveable { mutableIntStateOf(start.minute) }
    var pm by rememberSaveable { mutableStateOf(EventTimeRules.isPm(start)) }
    val chosen = EventTimeRules.of12Hour(hour12, minute, pm)
    SchedulingDialog(
        onDismiss = onDismiss,
        title = title,
        summary = EventTimeRules.display(EventTimeRules.toStorage(chosen)),
        confirmEnabled = true,
        onConfirm = { onConfirm(EventTimeRules.toStorage(chosen)) }
    ) {
        ClampedFontScale {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip("AM", selected = !pm, onClick = { pm = false }, modifier = Modifier.weight(1f))
                    ChoiceChip("PM", selected = pm, onClick = { pm = true }, modifier = Modifier.weight(1f))
                }
                PickerLabel("Hour")
                NumberGrid(EventTimeRules.hourOptions, selected = hour12, format = { it.toString() }) { hour12 = it }
                PickerLabel("Minutes")
                NumberGrid(EventTimeRules.minuteOptions, selected = minute, format = { ":%02d".format(it) }) { minute = it }
            }
        }
    }
}

@Composable
private fun PickerLabel(text: String) {
    Text(
        text,
        color = BookingPalette.Muted,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun NumberGrid(options: List<Int>, selected: Int, format: (Int) -> String, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { value ->
                    ChoiceChip(format(value), selected = value == selected, onClick = { onSelect(value) }, modifier = Modifier.weight(1f))
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

fun formatLongDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale))

/**
 * Date control: a prominent summary tile plus a quick-pick strip of the next 14 days, with a
 * "Full calendar" action that opens [EventDatePickerDialog]. Values are ISO `yyyy-MM-dd`.
 */
@Composable
fun EventDateSelector(
    isoDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    today: LocalDate = LocalDate.now()
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val selected = EventCalendarRules.parseIsoDate(isoDate)
    val locale = Locale.getDefault()

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScheduleTile(
            label = "Event date",
            value = selected?.let { formatLongDate(it, locale) },
            placeholder = "Choose a date",
            icon = Icons.Filled.CalendarMonth,
            isError = isError,
            onClick = { showCalendar = true },
            trailingAction = "Full calendar"
        )
        val upcoming = remember(today) { EventCalendarRules.upcomingDates(today) }
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = selected?.let { upcoming.indexOf(it) }?.takeIf { it > 1 }?.minus(1) ?: 0
        )
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.selectableGroup()
        ) {
            items(upcoming, key = { it.toEpochDay() }) { date ->
                QuickDateChip(
                    date = date,
                    selected = date == selected,
                    isToday = date == today,
                    locale = locale,
                    onClick = { onDateSelected(EventCalendarRules.toIsoDate(date)) }
                )
            }
        }
    }

    if (showCalendar) {
        EventDatePickerDialog(
            initialIsoDate = isoDate,
            onDismiss = { showCalendar = false },
            onConfirm = {
                onDateSelected(it)
                showCalendar = false
            },
            today = today
        )
    }
}

@Composable
private fun QuickDateChip(date: LocalDate, selected: Boolean, isToday: Boolean, locale: Locale, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) BookingPalette.Emerald else Color.White, tween(200), label = "quickBg"
    )
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .widthIn(min = 56.dp)
            .clip(shape)
            .background(background)
            .border(
                1.dp,
                when {
                    selected -> BookingPalette.Emerald
                    isToday -> BookingPalette.Champagne
                    else -> BookingPalette.Border
                },
                shape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = formatLongDate(date, locale) + if (isToday) ", today" else "" }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            if (isToday) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
            color = if (selected) BookingPalette.Champagne else BookingPalette.Muted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            date.dayOfMonth.toString(),
            color = if (selected) Color.White else BookingPalette.Ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            date.month.getDisplayName(TextStyle.SHORT, locale),
            color = if (selected) Color.White.copy(alpha = 0.8f) else BookingPalette.Muted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

/** A labelled time control showing the chosen time in large type with a champagne AM/PM suffix. */
@Composable
fun EventTimeSelector(
    label: String,
    time: String?,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultTime: LocalTime = LocalTime.of(18, 0),
    isError: Boolean = false
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    ScheduleTile(
        label = label,
        value = EventTimeRules.display(time),
        placeholder = "Set time",
        icon = Icons.Filled.Schedule,
        isError = isError,
        onClick = { showPicker = true },
        modifier = modifier
    )
    if (showPicker) {
        EventTimePickerDialog(
            title = label,
            initialTime = time,
            defaultTime = defaultTime,
            onDismiss = { showPicker = false },
            onConfirm = {
                onTimeSelected(it)
                showPicker = false
            }
        )
    }
}

/** Places two time selectors side by side, stacking them on narrow screens or large font scales. */
@Composable
fun EventTimeRangeRow(
    modifier: Modifier = Modifier,
    start: @Composable (Modifier) -> Unit,
    end: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val stack = maxWidth / LocalDensity.current.fontScale.coerceAtLeast(1f) < 300.dp
        if (stack) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                start(Modifier.fillMaxWidth())
                end(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                start(Modifier.weight(1f))
                end(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScheduleTile(
    label: String,
    value: String?,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: String? = null
) {
    val hasValue = value != null
    val borderColor by animateColorAsState(
        when {
            isError -> BookingPalette.Danger
            hasValue -> BookingPalette.Emerald
            else -> BookingPalette.FieldBorder
        },
        tween(200),
        label = "scheduleBorder"
    )
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .selectable(selected = false, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "$label, ${value ?: placeholder}" },
        shape = shape,
        color = if (hasValue) BookingPalette.Ivory else Color.White,
        border = BorderStroke(if (hasValue) 1.5.dp else 1.dp, borderColor)
    ) {
        BoxWithConstraints {
            // Side-by-side time tiles on phones are ~160dp wide: stack icon+label above the value there.
            val compact = maxWidth < 240.dp
            if (compact) {
                Column(
                    Modifier
                        .heightIn(min = 72.dp)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ScheduleIconBadge(icon, hasValue, size = 28.dp, iconSize = 16.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            label,
                            color = if (isError) BookingPalette.Danger else BookingPalette.Muted,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (value != null) {
                        ScheduleValue(value)
                    } else {
                        Text(placeholder, color = BookingPalette.Placeholder, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    }
                }
            } else {
                Row(
                    Modifier
                        .heightIn(min = 72.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScheduleIconBadge(icon, hasValue, size = 40.dp, iconSize = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            label,
                            color = if (isError) BookingPalette.Danger else BookingPalette.Muted,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (value != null) {
                            ScheduleValue(value)
                        } else {
                            Text(placeholder, color = BookingPalette.Placeholder, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (trailingAction != null) {
                        Text(
                            trailingAction,
                            color = BookingPalette.Emerald,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(start = 8.dp).widthIn(max = 96.dp)
                        )
                    } else if (isError) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = BookingPalette.Danger, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasValue: Boolean,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(if (hasValue) BookingPalette.Emerald else BookingPalette.EmeraldSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            null,
            tint = if (hasValue) BookingPalette.Champagne else BookingPalette.Emerald,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun ScheduleValue(value: String) {
    val parts = value.split(" ")
    val isTime = parts.size == 2 && (parts[1] == "AM" || parts[1] == "PM")
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            if (isTime) parts[0] else value,
            color = BookingPalette.Ink,
            style = if (isTime) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (isTime) {
            Spacer(Modifier.width(4.dp))
            Text(
                parts[1],
                color = BookingPalette.ChampagneDeep,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

/** Inline validation / hint banner for the scheduling section. */
@Composable
fun ScheduleMessage(text: String, isError: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isError) BookingPalette.DangerSoft else BookingPalette.EmeraldSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isError) Icons.Filled.ErrorOutline else Icons.Filled.Schedule,
            null,
            tint = if (isError) BookingPalette.Danger else BookingPalette.Emerald,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = if (isError) BookingPalette.Danger else BookingPalette.Emerald,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

