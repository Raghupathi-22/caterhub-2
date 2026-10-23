package com.daily.cetaring.presentation.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Colours for the customer catering booking flow; aligned with [ProfilePalette]. */
object BookingPalette {
    val Background = ProfilePalette.Background
    val Surface = Color.White
    val Burgundy = ProfilePalette.Burgundy
    val BurgundySoft = ProfilePalette.BurgundySoft
    val Green = ProfilePalette.Green
    val GreenSoft = ProfilePalette.GreenSoft
    val Amber = ProfilePalette.Amber
    val AmberSoft = ProfilePalette.AmberSoft
    val Ink = ProfilePalette.Ink
    val Muted = ProfilePalette.Muted
    val Placeholder = Color(0xFF7A7068)
    val Border = Color(0xFFE6DCCD)
    val FieldBorder = Color(0xFFCDBFAC)
    val Divider = ProfilePalette.Divider
    val Danger = ProfilePalette.Danger
    val DisabledContainer = Color(0xFFECE4D9)
    val DisabledContent = Color(0xFF6F665E)
}

val BookingCardShape = RoundedCornerShape(16.dp)
private val BookingButtonShape = RoundedCornerShape(14.dp)
private val BookingButtonMinHeight = 52.dp

private val BookingLightColorScheme = lightColorScheme(
    primary = BookingPalette.Burgundy,
    onPrimary = Color.White,
    primaryContainer = BookingPalette.BurgundySoft,
    onPrimaryContainer = BookingPalette.Burgundy,
    secondary = BookingPalette.Green,
    onSecondary = Color.White,
    secondaryContainer = BookingPalette.GreenSoft,
    onSecondaryContainer = BookingPalette.Green,
    tertiary = BookingPalette.Amber,
    background = BookingPalette.Background,
    onBackground = BookingPalette.Ink,
    surface = BookingPalette.Surface,
    onSurface = BookingPalette.Ink,
    surfaceVariant = Color(0xFFF4EEE5),
    onSurfaceVariant = BookingPalette.Muted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFF4EEE5),
    outline = BookingPalette.FieldBorder,
    outlineVariant = BookingPalette.Border,
    error = BookingPalette.Danger,
    onError = Color.White
)

/**
 * Forces the booking flow (including its dialogs) onto a light, brand-coloured scheme so the
 * device's dynamic/dark colours do not produce dark pickers inside a light screen.
 */
@Composable
fun BookingFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookingLightColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

/** Uses dark system-bar icons while the light booking screen is visible, restoring them afterwards. */
@Composable
fun BookingLightSystemBars() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = true
        controller?.isAppearanceLightNavigationBars = true
        onDispose {
            if (controller != null) {
                previousStatus?.let { controller.isAppearanceLightStatusBars = it }
                previousNavigation?.let { controller.isAppearanceLightNavigationBars = it }
            }
        }
    }
}

/** Date conversions between the Material DatePicker (UTC-midnight millis) and the stored ISO date. */
object BookingDateRules {
    fun utcMillisForIsoDate(isoDate: String): Long? =
        isoDate.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        }

    fun isoDateForUtcMillis(utcMillis: Long): String =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()

    /** Today and future dates (in the user's local calendar) are selectable; past dates are not. */
    fun isSelectableDate(utcMillis: Long, today: LocalDate = LocalDate.now()): Boolean =
        !Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)

    fun isSelectableYear(year: Int, today: LocalDate = LocalDate.now()): Boolean = year >= today.year
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
            title = {
                Text(
                    title,
                    color = BookingPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = BookingPalette.Burgundy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BookingPalette.Background,
                scrolledContainerColor = BookingPalette.Background,
                titleContentColor = BookingPalette.Ink,
                navigationIconContentColor = BookingPalette.Burgundy
            )
        )
}

@Composable
fun BookingStepProgress(
    stepIndex: Int,
    stepTitles: List<String>,
    modifier: Modifier = Modifier
) {
    val total = stepTitles.size
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Step ${stepIndex + 1} of $total",
                color = BookingPalette.Burgundy,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stepTitles.getOrElse(stepIndex) { "" },
                color = BookingPalette.Muted,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Step ${stepIndex + 1} of $total" },
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(total) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < stepIndex -> BookingPalette.Green
                                index == stepIndex -> BookingPalette.Burgundy
                                else -> BookingPalette.Border
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun BookingStepHeading(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            color = BookingPalette.Ink,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(subtitle, color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BookingSectionTitle(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BookingPalette.GreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = BookingPalette.Green, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
        }
        Text(
            title,
            color = BookingPalette.Ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
fun BookingCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BookingCardShape,
        color = BookingPalette.Surface,
        border = BorderStroke(1.dp, BookingPalette.Border),
        shadowElevation = 1.dp
    ) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) { content() }
    }
}

/** Visual check indicator: an empty ring when unselected, a filled burgundy check when selected. */
@Composable
fun BookingSelectionIndicator(selected: Boolean, radio: Boolean = false, modifier: Modifier = Modifier) {
    val shape = CircleShape
    Box(
        modifier
            .size(24.dp)
            .clip(shape)
            .background(if (selected) BookingPalette.Burgundy else Color.White)
            .border(
                width = if (selected) 0.dp else 1.5.dp,
                color = if (selected) BookingPalette.Burgundy else BookingPalette.FieldBorder,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            if (radio) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(Color.White))
            } else {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

enum class BookingSelectionMode { Single, Multiple }

/** A whole-card selectable surface with a clear selected state (burgundy border + soft tint). */
@Composable
fun BookingSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: BookingSelectionMode = BookingSelectionMode.Single,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    val interaction = when (mode) {
        BookingSelectionMode.Single -> Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
        BookingSelectionMode.Multiple -> Modifier.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    }
    Surface(
        modifier = modifier
            .clip(BookingCardShape)
            .then(interaction),
        shape = BookingCardShape,
        color = if (selected) BookingPalette.BurgundySoft else BookingPalette.Surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) BookingPalette.Burgundy else BookingPalette.Border
        ),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

@Composable
fun BookingIconTile(icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) BookingPalette.Burgundy else BookingPalette.GreenSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color.White else BookingPalette.Green,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun bookingTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BookingPalette.Ink,
    unfocusedTextColor = BookingPalette.Ink,
    disabledTextColor = BookingPalette.Muted,
    errorTextColor = BookingPalette.Ink,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color(0xFFF7F3ED),
    errorContainerColor = Color.White,
    cursorColor = BookingPalette.Burgundy,
    errorCursorColor = BookingPalette.Danger,
    focusedBorderColor = BookingPalette.Burgundy,
    unfocusedBorderColor = BookingPalette.FieldBorder,
    errorBorderColor = BookingPalette.Danger,
    focusedLabelColor = BookingPalette.Burgundy,
    unfocusedLabelColor = BookingPalette.Muted,
    errorLabelColor = BookingPalette.Danger,
    focusedPlaceholderColor = BookingPalette.Placeholder,
    unfocusedPlaceholderColor = BookingPalette.Placeholder,
    focusedSupportingTextColor = BookingPalette.Muted,
    unfocusedSupportingTextColor = BookingPalette.Muted,
    errorSupportingTextColor = BookingPalette.Danger,
    focusedLeadingIconColor = BookingPalette.Burgundy,
    unfocusedLeadingIconColor = BookingPalette.Muted
)

@Composable
fun BookingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = BookingPalette.Ink),
        shape = RoundedCornerShape(12.dp),
        colors = bookingTextFieldColors(),
        modifier = modifier.fillMaxWidth()
    )
}

/** A read-only field that opens a picker. */
@Composable
fun BookingPickerField(
    label: String,
    value: String?,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasValue = !value.isNullOrBlank()
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = false, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "$label, ${value ?: placeholder}" },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (hasValue) BookingPalette.Green else BookingPalette.FieldBorder)
    ) {
        Row(
            Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (hasValue) BookingPalette.Green else BookingPalette.Muted, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, color = BookingPalette.Muted, style = MaterialTheme.typography.labelMedium)
                Text(
                    if (hasValue) value.orEmpty() else placeholder,
                    color = if (hasValue) BookingPalette.Ink else BookingPalette.Placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            Icon(Icons.Filled.ChevronRight, null, tint = BookingPalette.Muted)
        }
    }
}

@Composable
fun BookingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = text
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = BookingButtonMinHeight),
        shape = BookingButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = BookingPalette.Burgundy,
            contentColor = Color.White,
            disabledContainerColor = if (loading) BookingPalette.Burgundy.copy(alpha = 0.85f) else BookingPalette.DisabledContainer,
            disabledContentColor = if (loading) Color.White else BookingPalette.DisabledContent
        )
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(loadingText, fontWeight = FontWeight.SemiBold)
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BookingSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = BookingButtonMinHeight),
        shape = BookingButtonShape,
        border = BorderStroke(1.dp, if (enabled) BookingPalette.FieldBorder else BookingPalette.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = BookingPalette.Ink,
            disabledContainerColor = Color.White,
            disabledContentColor = BookingPalette.DisabledContent
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** Sticky bottom action area that stays above the navigation bar and the keyboard. */
@Composable
fun BookingBottomActionBar(content: @Composable RowScope.() -> Unit) {
    Surface(color = BookingPalette.Surface, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            HorizontalDivider(color = BookingPalette.Border)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun bookingDatePickerColors() = DatePickerDefaults.colors(
    containerColor = Color.White,
    titleContentColor = BookingPalette.Muted,
    headlineContentColor = BookingPalette.Ink,
    weekdayContentColor = BookingPalette.Muted,
    subheadContentColor = BookingPalette.Muted,
    navigationContentColor = BookingPalette.Ink,
    yearContentColor = BookingPalette.Ink,
    disabledYearContentColor = BookingPalette.Ink.copy(alpha = 0.32f),
    currentYearContentColor = BookingPalette.Burgundy,
    selectedYearContentColor = Color.White,
    selectedYearContainerColor = BookingPalette.Burgundy,
    dayContentColor = BookingPalette.Ink,
    disabledDayContentColor = BookingPalette.Ink.copy(alpha = 0.32f),
    selectedDayContentColor = Color.White,
    selectedDayContainerColor = BookingPalette.Burgundy,
    todayContentColor = BookingPalette.Burgundy,
    todayDateBorderColor = BookingPalette.Burgundy,
    dividerColor = BookingPalette.Border
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val colors = bookingDatePickerColors()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let(onConfirm) },
                enabled = state.selectedDateMillis != null,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BookingPalette.Burgundy,
                    disabledContentColor = BookingPalette.DisabledContent
                )
            ) { Text("Confirm", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Ink)
            ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
        },
        colors = colors
    ) {
        DatePicker(
            state = state,
            colors = colors,
            title = {
                Text(
                    "Select event date",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var useKeyboardInput by rememberSaveable { mutableStateOf(false) }
    val colors = TimePickerDefaults.colors(
        clockDialColor = Color(0xFFF4EEE5),
        clockDialSelectedContentColor = Color.White,
        clockDialUnselectedContentColor = BookingPalette.Ink,
        selectorColor = BookingPalette.Burgundy,
        containerColor = Color.White,
        periodSelectorBorderColor = BookingPalette.FieldBorder,
        periodSelectorSelectedContainerColor = BookingPalette.BurgundySoft,
        periodSelectorUnselectedContainerColor = Color.White,
        periodSelectorSelectedContentColor = BookingPalette.Burgundy,
        periodSelectorUnselectedContentColor = BookingPalette.Ink,
        timeSelectorSelectedContainerColor = BookingPalette.BurgundySoft,
        timeSelectorUnselectedContainerColor = Color(0xFFF4EEE5),
        timeSelectorSelectedContentColor = BookingPalette.Burgundy,
        timeSelectorUnselectedContentColor = BookingPalette.Ink
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 380.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Select event time",
                        color = BookingPalette.Ink,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { useKeyboardInput = !useKeyboardInput }) {
                        Icon(
                            if (useKeyboardInput) Icons.Filled.Schedule else Icons.Filled.Keyboard,
                            contentDescription = if (useKeyboardInput) "Switch to clock" else "Switch to keyboard input",
                            tint = BookingPalette.Muted
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (useKeyboardInput) {
                    TimeInput(state = state, colors = colors)
                } else {
                    TimePicker(state = state, colors = colors)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Ink)
                    ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Burgundy)
                    ) { Text("Confirm", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
