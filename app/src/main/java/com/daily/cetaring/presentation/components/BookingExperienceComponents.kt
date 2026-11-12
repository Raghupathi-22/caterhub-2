package com.daily.cetaring.presentation.components

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daily.cetaring.R
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.domain.booking.BookingTimeSlots
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.EventTypeDefinition
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog
import java.time.LocalTime

/**
 * Photography used by the booking journeys. Every image is a bundled, licensed CaterHub asset
 * (see [ServiceImagery] for sources), so the flow works offline and never shows a broken image.
 */
object BookingImagery {
    @DrawableRes
    fun eventPhoto(eventId: String): Int = when (eventId) {
        "wedding" -> R.drawable.public_offer_wedding
        "reception" -> R.drawable.svc_venue_reception_stage
        "engagement", "anniversary" -> R.drawable.svc_decoration_floral_table
        "birthday", "baby_shower", "naming_ceremony", "party" -> R.drawable.svc_decoration_balloon_arch
        "pooja", "housewarming", "festival" -> R.drawable.svc_religious_pooja_thali
        "corporate" -> R.drawable.svc_venue_convention_centre
        "school_college" -> R.drawable.svc_venue_banquet_hall
        else -> R.drawable.public_offer_family
    }

    @DrawableRes
    fun mealPhoto(type: CateringMealServiceType): Int = when (type) {
        CateringMealServiceType.BREAKFAST -> R.drawable.svc_menu_breakfast
        CateringMealServiceType.LUNCH -> R.drawable.public_main_course
        CateringMealServiceType.SNACKS -> R.drawable.public_starters
        CateringMealServiceType.BEVERAGES -> R.drawable.public_beverages
        CateringMealServiceType.DINNER -> R.drawable.public_biryani
    }

    @DrawableRes
    fun foodTypePhoto(foodType: String): Int = when (foodType) {
        BookingOptions.foodTypeVegetarian -> R.drawable.public_veg_specials
        BookingOptions.foodTypeNonVegetarian -> R.drawable.public_biryani
        else -> R.drawable.public_hero_food
    }

    @DrawableRes
    fun packagePhoto(plan: BookingOptions.CateringPlanOption): Int = when (plan.backendValue) {
        BookingOptions.planVegBasic -> R.drawable.public_veg_specials
        BookingOptions.planVegClassic -> R.drawable.public_main_course
        BookingOptions.planVegPremium -> R.drawable.public_desserts
        BookingOptions.planNonVegBasic -> R.drawable.public_biryani
        BookingOptions.planNonVegClassic -> R.drawable.public_starters
        BookingOptions.planNonVegPremium -> R.drawable.public_offer_wedding
        else -> R.drawable.public_hero_food
    }

    fun visual(@DrawableRes photo: Int, icon: ImageVector = Icons.Filled.Restaurant, seed: Int = photo): ServiceVisual =
        ServiceVisual(photoRes = photo, icon = icon, accent = BookingPalette.Emerald, seed = seed)
}

/** True when the user disabled animations (Developer options / accessibility "Remove animations"). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

private val ImageCardShape = RoundedCornerShape(20.dp)

@Composable
private fun animatedSelectionScale(selected: Boolean): Float {
    val reduced = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (selected && !reduced) 1f else if (reduced) 1f else 0.985f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )
    return scale
}

@Composable
private fun SelectedBadge(visible: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (visible) 1f else 0f, tween(200), label = "badge")
    Box(
        modifier
            .size(26.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(BookingPalette.Champagne)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Check, null, tint = BookingPalette.Emerald, modifier = Modifier.size(16.dp))
    }
}

/* ------------------------------------------------------------------------------------------------
 * Event selection
 * ---------------------------------------------------------------------------------------------- */

/** Image-led event card: full-bleed photo with an emerald scrim and the event name. */
@Composable
fun EventImageCard(
    event: EventTypeDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (selected) BookingPalette.Champagne else Color.Transparent, tween(220), label = "eventBorder"
    )
    Box(
        modifier
            .scale(animatedSelectionScale(selected))
            .heightIn(min = 118.dp)
            .clip(ImageCardShape)
            .border(if (selected) 3.dp else 0.dp, borderColor, ImageCardShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = event.displayName
                stateDescription = if (selected) "Selected" else "Not selected"
            }
    ) {
        ServiceImage(
            visual = remember(event.id) { BookingImagery.visual(BookingImagery.eventPhoto(event.id), eventTypeIcon(event)) },
            contentDescription = null,
            iconSize = 28.dp,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.35f to Color.Black.copy(alpha = 0.05f),
                        1f to BookingPalette.Emerald.copy(alpha = if (selected) 0.92f else 0.82f)
                    )
                )
        )
        Text(
            event.displayName,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 64.dp)
        )
        SelectedBadge(selected, Modifier.align(Alignment.TopEnd).padding(8.dp))
    }
}

/** Responsive image grid of every catalogue event (2 columns; 1 on very narrow/large-font, 3 on wide). */
@Composable
fun EventImageGrid(
    selectedBackendValue: String?,
    onSelect: (EventTypeDefinition) -> Unit,
    modifier: Modifier = Modifier,
    eventTypes: List<EventTypeDefinition> = EventTypeCatalog.eventTypes
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = EventTypeGridRules.columnsFor(maxWidth.value, LocalDensity.current.fontScale)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            eventTypes.chunked(columns).forEach { rowItems ->
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { event ->
                        EventImageCard(
                            event = event,
                            selected = selectedBackendValue == event.backendValue,
                            onClick = { onSelect(event) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------------
 * Guest count & quantity
 * ---------------------------------------------------------------------------------------------- */

@Composable
fun QuantityStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    canDecrement: Boolean = value > 0,
    canIncrement: Boolean = true,
    large: Boolean = false
) {
    val buttonSize = if (large) 48.dp else 40.dp
    Row(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(BookingPalette.EmeraldSoft)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(Icons.Filled.Remove, "Decrease $label", canDecrement, onDecrement, buttonSize)
        Text(
            value.toString(),
            color = BookingPalette.Ink,
            style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = if (large) 72.dp else 36.dp)
                .semantics { contentDescription = "$label: $value" }
        )
        StepperButton(Icons.Filled.Add, "Increase $label", canIncrement, onIncrement, buttonSize)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit, size: Dp) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(size)) {
        Box(
            Modifier
                .size(size - 4.dp)
                .clip(CircleShape)
                .background(if (enabled) BookingPalette.Emerald else BookingPalette.DisabledContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, description, tint = if (enabled) Color.White else BookingPalette.DisabledContent)
        }
    }
}

/** Big stepper with quick presets and an optional typed value; values outside 1..max are rejected. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuestCountSelector(
    guestCount: Int?,
    onGuestCountChange: (Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    presets: List<Int> = BookingOptions.guestQuickOptions,
    maxGuests: Int = BookingOptions.maxGuestCount
) {
    var typing by rememberSaveable { mutableStateOf(false) }
    var typed by rememberSaveable { mutableStateOf("") }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Guests", color = BookingPalette.Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (guestCount == null) "How many people are you expecting?" else "Up to ${"%,d".format(maxGuests)} guests",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            QuantityStepper(
                value = guestCount ?: 0,
                onDecrement = onDecrement,
                onIncrement = onIncrement,
                label = "guests",
                canDecrement = (guestCount ?: 0) > 1,
                canIncrement = (guestCount ?: 0) < maxGuests,
                large = true
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.selectableGroup()
        ) {
            presets.forEach { preset ->
                BookingChip(
                    text = "$preset",
                    selected = guestCount == preset && !typing,
                    onClick = {
                        typing = false
                        onGuestCountChange(preset)
                    }
                )
            }
            BookingChip(
                text = "Type a number",
                selected = typing,
                onClick = {
                    typing = true
                    typed = guestCount?.toString().orEmpty()
                }
            )
        }
        AnimatedVisibility(typing, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            val parsed = typed.toIntOrNull()
            val invalid = typed.isNotBlank() && (parsed == null || parsed !in 1..maxGuests)
            BookingTextField(
                value = typed,
                onValueChange = { value ->
                    if (value.length > maxGuests.toString().length || !value.all(Char::isDigit)) return@BookingTextField
                    typed = value
                    value.toIntOrNull()?.takeIf { it in 1..maxGuests }?.let(onGuestCountChange)
                },
                label = "Number of guests",
                supportingText = if (invalid) "Enter a guest count between 1 and ${"%,d".format(maxGuests)}." else null,
                isError = invalid,
                leadingIcon = Icons.Filled.Groups,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
        }
    }
}

/** Rounded pill chip with an emerald selected state. */
@Composable
fun BookingChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supporting: String? = null
) {
    val background by animateColorAsState(
        if (selected) BookingPalette.Emerald else Color.White, tween(180), label = "chipBg"
    )
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (enabled) background else BookingPalette.DisabledContainer)
            .border(1.dp, if (selected) BookingPalette.Emerald else BookingPalette.Border, shape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val contentColor = when {
            !enabled -> BookingPalette.DisabledContent
            selected -> Color.White
            else -> BookingPalette.Ink
        }
        if (selected) {
            Icon(Icons.Filled.Check, null, tint = BookingPalette.Champagne, modifier = Modifier.size(16.dp))
        } else if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = BookingPalette.Emerald, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(text, color = contentColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            if (supporting != null) {
                Text(
                    supporting,
                    color = if (selected) Color.White.copy(alpha = 0.8f) else BookingPalette.Muted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------------
 * Image selection cards
 * ---------------------------------------------------------------------------------------------- */

/** Vertical photo tile for grids (e.g. meal services). Supports single or multiple selection. */
@Composable
fun ImageTileCard(
    title: String,
    description: String?,
    visual: ServiceVisual,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true,
    imageHeight: Dp = 112.dp
) {
    val borderColor by animateColorAsState(
        if (selected) BookingPalette.Emerald else BookingPalette.Border, tween(220), label = "tileBorder"
    )
    val interaction = if (multiSelect) {
        Modifier.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else {
        Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
    Surface(
        modifier = modifier
            .scale(animatedSelectionScale(selected))
            .clip(ImageCardShape)
            .then(interaction)
            .semantics(mergeDescendants = true) {
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        shape = ImageCardShape,
        color = if (selected) BookingPalette.ChampagneSoft else Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(imageHeight)) {
                ServiceImage(visual = visual, contentDescription = null, modifier = Modifier.matchParentSize())
                SelectedBadge(selected, Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = BookingPalette.Ink, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (!description.isNullOrBlank()) {
                    Text(description, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * Horizontal photo card for lists of bookable services. [trailing] holds the selection control
 * (quantity stepper or select toggle); [onClick] toggles the card when it is not quantity based.
 */
@Composable
fun ImageRowCard(
    title: String,
    description: String?,
    priceText: String?,
    visual: ServiceVisual,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val borderColor by animateColorAsState(
        if (selected) BookingPalette.Emerald else BookingPalette.Border, tween(220), label = "rowBorder"
    )
    val clickModifier = if (onClick != null) {
        Modifier.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else {
        Modifier
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(ImageCardShape)
            .then(clickModifier),
        shape = ImageCardShape,
        color = if (selected) BookingPalette.ChampagneSoft else Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServiceImage(
                    visual = visual,
                    contentDescription = null,
                    shape = RoundedCornerShape(16.dp),
                    iconSize = 26.dp,
                    modifier = Modifier.size(88.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, color = BookingPalette.Ink, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (!description.isNullOrBlank()) {
                        Text(description, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (priceText != null) {
                        Text(priceText, color = BookingPalette.ChampagneDeep, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { footer?.invoke() }
                trailing()
            }
        }
    }
}

/** Pill toggle used for quote-only services (no quantity). */
@Composable
fun SelectToggleButton(selected: Boolean, onClick: () -> Unit, label: String) {
    BookingChip(
        text = if (selected) "Selected" else "Select",
        selected = selected,
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = if (selected) "Remove $label" else "Select $label" }
    )
}

/** Package card with a photo header, price per unit and menu highlights. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackageCard(
    title: String,
    priceText: String,
    priceUnit: String?,
    highlights: List<String>,
    visual: ServiceVisual,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    footnote: String? = null
) {
    val borderColor by animateColorAsState(
        if (selected) BookingPalette.Emerald else BookingPalette.Border, tween(220), label = "pkgBorder"
    )
    Surface(
        modifier = modifier
            .scale(animatedSelectionScale(selected))
            .clip(ImageCardShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) { stateDescription = if (selected) "Selected" else "Not selected" },
        shape = ImageCardShape,
        color = Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(120.dp)) {
                ServiceImage(visual = visual, contentDescription = null, modifier = Modifier.matchParentSize())
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))
                    )
                )
                if (badge != null) {
                    Text(
                        badge,
                        color = BookingPalette.Emerald,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(BookingPalette.Champagne)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                SelectedBadge(selected, Modifier.align(Alignment.TopEnd).padding(8.dp))
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                )
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(priceText, color = BookingPalette.Emerald, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    if (priceUnit != null) {
                        Text(" / $priceUnit", color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 2.dp))
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    highlights.forEach { item ->
                        Text(
                            item,
                            color = BookingPalette.Ink,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(BookingPalette.Ivory)
                                .border(1.dp, BookingPalette.Border, RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                if (footnote != null) {
                    Text(footnote, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Small inline status: "3 selected · Lunch • Dinner" or a gentle hint when nothing is selected. */
@Composable
fun SelectionSummaryText(selectedLabels: List<String>, emptyHint: String, modifier: Modifier = Modifier) {
    val count = selectedLabels.size
    Text(
        if (count == 0) emptyHint else "$count selected · ${selectedLabels.joinToString(" • ")}",
        color = if (count == 0) BookingPalette.Muted else BookingPalette.Emerald,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (count == 0) FontWeight.Normal else FontWeight.SemiBold,
        modifier = modifier
    )
}

/* ------------------------------------------------------------------------------------------------
 * Date, time and location
 * ---------------------------------------------------------------------------------------------- */

/** Quick start-time slots plus a "Custom time" chip that opens the clock picker. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartTimeSlotPicker(
    time: String?,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Start time",
    isError: Boolean = false
) {
    val slotTimes = BookingTimeSlots.startSlots.map { it.time }
    val isCustom = !time.isNullOrBlank() && time !in slotTimes
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.selectableGroup()
        ) {
            BookingTimeSlots.startSlots.forEach { slot ->
                BookingChip(
                    text = EventTimeRules.display(slot.time).orEmpty(),
                    supporting = slot.label,
                    selected = time == slot.time,
                    onClick = { onTimeSelected(slot.time) }
                )
            }
        }
        EventTimeSelector(
            label = if (isCustom) label else "$label · custom",
            time = time?.takeIf { isCustom },
            onTimeSelected = onTimeSelected,
            defaultTime = EventTimeRules.parse(time) ?: LocalTime.of(18, 0),
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** End-time selection as event duration chips (same-day only) plus a custom end-time picker. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EndTimeDurationPicker(
    startTime: String?,
    endTime: String?,
    onEndSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val currentDuration = BookingTimeSlots.durationOf(startTime, endTime)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (EventTimeRules.parse(startTime) != null) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.selectableGroup()
            ) {
                BookingTimeSlots.durationHours.forEach { hours ->
                    val end = BookingTimeSlots.endAfter(startTime, hours)
                    BookingChip(
                        text = "$hours hrs",
                        supporting = end?.let { "till ${EventTimeRules.display(it)}" } ?: "past midnight",
                        selected = currentDuration == hours,
                        enabled = end != null,
                        onClick = { end?.let(onEndSelected) }
                    )
                }
            }
        }
        EventTimeSelector(
            label = "End time",
            time = endTime?.takeIf { it.isNotBlank() },
            onTimeSelected = onEndSelected,
            defaultTime = EventTimeRules.parse(startTime)?.plusHours(3) ?: LocalTime.of(21, 0),
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Address entry with searchable area suggestions from CaterHub's service-area list (no paid maps
 * dependency). [landmark]/[city] are shown only when the booking type supports them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingLocationSection(
    address: String,
    onAddressChange: (String) -> Unit,
    area: String,
    onAreaChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    addressMaxLength: Int? = null,
    areaMaxLength: Int? = null,
    city: String? = null,
    onCityChange: ((String) -> Unit)? = null,
    landmark: String? = null,
    onLandmarkChange: ((String) -> Unit)? = null,
    showErrors: Boolean = false
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BookingTextField(
            value = area,
            onValueChange = { value -> onAreaChange(areaMaxLength?.let { value.take(it) } ?: value) },
            label = "Area / locality",
            placeholder = "Search or type your area",
            leadingIcon = Icons.Filled.LocationOn,
            isError = showErrors && area.isBlank(),
            supportingText = if (showErrors && area.isBlank()) "Area is required" else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )
        val query = area.trim()
        val suggestions = remember(query) {
            val all = WorkerProfileCatalog.serviceAreas
            if (query.isEmpty()) {
                all.take(8)
            } else {
                all.filter { it.contains(query, ignoreCase = true) && !it.equals(query, ignoreCase = true) }.take(8)
            }
        }
        if (suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    BookingChip(text = suggestion, selected = false, onClick = { onAreaChange(suggestion) })
                }
            }
        }
        BookingTextField(
            value = address,
            onValueChange = { value -> onAddressChange(addressMaxLength?.let { value.take(it) } ?: value) },
            label = "Venue / full address",
            placeholder = "House or venue name, street",
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            isError = showErrors && address.isBlank(),
            supportingText = when {
                showErrors && address.isBlank() -> "Address is required"
                addressMaxLength != null -> "${address.length}/$addressMaxLength"
                else -> null
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        if (landmark != null && onLandmarkChange != null) {
            BookingTextField(
                value = landmark,
                onValueChange = onLandmarkChange,
                label = "Landmark (optional)",
                placeholder = "Nearby landmark",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
        }
        if (city != null && onCityChange != null) {
            BookingTextField(
                value = city,
                onValueChange = onCityChange,
                label = "City",
                isError = showErrors && city.isBlank(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done)
            )
        }
    }
}

/* ------------------------------------------------------------------------------------------------
 * Review & pricing
 * ---------------------------------------------------------------------------------------------- */

/** A review section with an Edit action that returns to the step that owns these details. */
@Composable
fun BookingReviewSection(
    title: String,
    icon: ImageVector,
    onEdit: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BookingCard(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(BookingPalette.EmeraldSoft),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = BookingPalette.Emerald, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                color = BookingPalette.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            TextButton(
                onClick = onEdit,
                enabled = enabled,
                colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Emerald),
                modifier = Modifier.semantics { contentDescription = "Edit $title" }
            ) {
                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit", fontWeight = FontWeight.SemiBold)
            }
        }
        HorizontalDivider(color = BookingPalette.Border)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
fun BookingReviewRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f))
        Text(
            value,
            color = BookingPalette.Ink,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

data class PriceLine(val label: String, val detail: String?, val amount: String)

/**
 * Pricing breakdown. Only published prices are itemised; quote-only items say "Price on quote".
 * No taxes or delivery charges are assumed. [note] explains who confirms the final amount.
 */
@Composable
fun PriceBreakdownCard(
    lines: List<PriceLine>,
    totalLabel: String,
    totalValue: String,
    note: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BookingCardShape,
        color = BookingPalette.Emerald
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Price summary",
                color = BookingPalette.Champagne,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            lines.forEach { line ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(line.label, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        line.detail?.let {
                            Text(it, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(line.amount, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(totalLabel, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(totalValue, color = BookingPalette.Champagne, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            if (note != null) {
                Text(note, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Review hero: event photo with a bottom gradient, "Review & confirm" eyebrow, event name, the
 * selected occasion/service and compact fact pills (date, guests, time).
 */
@Composable
fun BookingReviewHeader(
    visual: ServiceVisual,
    title: String,
    subtitle: String?,
    facts: List<Pair<ImageVector, String>>,
    modifier: Modifier = Modifier,
    eyebrow: String = "Review & confirm"
) {
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 196.dp)
            .clip(RoundedCornerShape(26.dp))
    ) {
        ServiceImage(visual = visual, contentDescription = null, modifier = Modifier.matchParentSize())
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.35f to BookingPalette.Emerald.copy(alpha = 0.35f),
                    1f to BookingPalette.Emerald.copy(alpha = 0.96f)
                )
            )
        )
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(eyebrow.uppercase(), color = BookingPalette.Champagne, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.semantics { heading() }
            )
            subtitle?.let { Text(it, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium) }
            if (facts.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    facts.take(3).forEach { (icon, text) ->
                        Row(
                            Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = BookingPalette.Champagne, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text, color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/** Short photo banner at the top of a booking step. */
@Composable
fun BookingHeroBanner(
    visual: ServiceVisual,
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    height: Dp = 132.dp
) {
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(RoundedCornerShape(22.dp))
    ) {
        ServiceImage(visual = visual, contentDescription = null, modifier = Modifier.matchParentSize())
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(
                    0f to BookingPalette.Emerald.copy(alpha = 0.92f),
                    0.65f to BookingPalette.Emerald.copy(alpha = 0.55f),
                    1f to Color.Transparent
                )
            )
        )
        Column(
            Modifier.align(Alignment.CenterStart).padding(18.dp).fillMaxWidth(0.72f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(eyebrow.uppercase(), color = BookingPalette.Champagne, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** Success confirmation mark: a gentle scale-in of the champagne check (static with reduced motion). */
@Composable
fun BookingSuccessMark(modifier: Modifier = Modifier) {
    val reduced = rememberReducedMotion()
    var shown by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        if (shown) 1f else 0.4f,
        if (reduced) tween(0) else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "successScale"
    )
    Box(
        modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(BookingPalette.EmeraldSoft),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(68.dp).clip(CircleShape).background(BookingPalette.Emerald),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, "Success", tint = BookingPalette.Champagne, modifier = Modifier.size(38.dp))
        }
    }
}

/** Section heading inside a step with an optional trailing value. */
@Composable
fun BookingSubheading(title: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Event, trailing: String? = null) {
    Row(modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = BookingPalette.ChampagneDeep, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = BookingPalette.Ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (trailing != null) {
            Text(trailing, color = BookingPalette.Emerald, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
