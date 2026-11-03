package com.daily.cetaring.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium palette for the customer booking flows (catering and service requests) only:
 * deep emerald, warm ivory and restrained champagne gold. Other screens keep their own colours.
 */
object BookingPalette {
    val Emerald = Color(0xFF173F32)
    val EmeraldSoft = Color(0xFFE8EFEA)
    val Champagne = Color(0xFFD9B979)
    val ChampagneDeep = Color(0xFF8C6A2F)
    val ChampagneSoft = Color(0xFFF7EFDD)
    val Ivory = Color(0xFFFBF8F1)

    val Background = Ivory
    val Surface = Color.White
    val Accent = Emerald
    val AccentSoft = ChampagneSoft
    val Green = Emerald
    val GreenSoft = EmeraldSoft
    val Amber = ProfilePalette.Amber
    val AmberSoft = ProfilePalette.AmberSoft
    val Ink = Color(0xFF242824)
    val Muted = Color(0xFF686C66)
    val Placeholder = Color(0xFF7A7D76)
    val Border = Color(0xFFE8E2D7)
    val FieldBorder = Color(0xFFCFC6B6)
    val Divider = ProfilePalette.Divider
    val Danger = ProfilePalette.Danger
    val DangerSoft = ProfilePalette.DangerSoft
    val DisabledContainer = Color(0xFFECE7DD)
    val DisabledContent = Color(0xFF6F716B)
}

val BookingCardShape = RoundedCornerShape(16.dp)
private val BookingButtonShape = RoundedCornerShape(14.dp)
private val BookingButtonMinHeight = 52.dp

private val BookingLightColorScheme = lightColorScheme(
    primary = BookingPalette.Accent,
    onPrimary = Color.White,
    primaryContainer = BookingPalette.AccentSoft,
    onPrimaryContainer = BookingPalette.Accent,
    secondary = BookingPalette.Green,
    onSecondary = Color.White,
    secondaryContainer = BookingPalette.GreenSoft,
    onSecondaryContainer = BookingPalette.Green,
    tertiary = BookingPalette.Champagne,
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = BookingPalette.Accent)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BookingPalette.Background,
                scrolledContainerColor = BookingPalette.Background,
                titleContentColor = BookingPalette.Ink,
                navigationIconContentColor = BookingPalette.Accent
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
                color = BookingPalette.Accent,
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
                val color by animateColorAsState(
                    targetValue = when {
                        index < stepIndex -> BookingPalette.Emerald
                        index == stepIndex -> BookingPalette.Champagne
                        else -> BookingPalette.Border
                    },
                    animationSpec = tween(320),
                    label = "stepColor"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(color)
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
            .background(if (selected) BookingPalette.Accent else Color.White)
            .border(
                width = if (selected) 0.dp else 1.5.dp,
                color = if (selected) BookingPalette.Accent else BookingPalette.FieldBorder,
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
        color = if (selected) BookingPalette.AccentSoft else BookingPalette.Surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) BookingPalette.Accent else BookingPalette.Border
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
            .background(if (selected) BookingPalette.Accent else BookingPalette.GreenSoft),
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
    cursorColor = BookingPalette.Accent,
    errorCursorColor = BookingPalette.Danger,
    focusedBorderColor = BookingPalette.Accent,
    unfocusedBorderColor = BookingPalette.FieldBorder,
    errorBorderColor = BookingPalette.Danger,
    focusedLabelColor = BookingPalette.Accent,
    unfocusedLabelColor = BookingPalette.Muted,
    errorLabelColor = BookingPalette.Danger,
    focusedPlaceholderColor = BookingPalette.Placeholder,
    unfocusedPlaceholderColor = BookingPalette.Placeholder,
    focusedSupportingTextColor = BookingPalette.Muted,
    unfocusedSupportingTextColor = BookingPalette.Muted,
    errorSupportingTextColor = BookingPalette.Danger,
    focusedLeadingIconColor = BookingPalette.Accent,
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
            containerColor = BookingPalette.Accent,
            contentColor = Color.White,
            disabledContainerColor = if (loading) BookingPalette.Accent.copy(alpha = 0.85f) else BookingPalette.DisabledContainer,
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
