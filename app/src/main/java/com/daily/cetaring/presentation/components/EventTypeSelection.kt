package com.daily.cetaring.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.EventTypeDefinition
import kotlinx.coroutines.launch

/** Icon for each event type; unknown ids fall back to a generic event icon. */
fun eventTypeIcon(event: EventTypeDefinition): ImageVector = when (event.id) {
    "birthday" -> Icons.Filled.Cake
    "wedding" -> Icons.Filled.Favorite
    "engagement" -> Icons.Filled.Diamond
    "reception" -> Icons.Filled.LocalBar
    "anniversary" -> Icons.Filled.Celebration
    "baby_shower" -> Icons.Filled.ChildFriendly
    "naming_ceremony" -> Icons.Filled.ChildCare
    "housewarming" -> Icons.Filled.Home
    "pooja" -> Icons.Filled.TempleHindu
    "corporate" -> Icons.Filled.BusinessCenter
    "school_college" -> Icons.Filled.School
    "festival" -> Icons.Filled.AutoAwesome
    "party" -> Icons.Filled.Celebration
    "other" -> Icons.Filled.MoreHoriz
    else -> Icons.Filled.Event
}

/** Pure column rule for the event-type grid, based on the font-scale-adjusted available width. */
object EventTypeGridRules {
    fun columnsFor(availableWidthDp: Float, fontScale: Float): Int {
        val effective = availableWidthDp / fontScale.coerceAtLeast(1f)
        return when {
            effective >= 560f -> 3
            effective >= 260f -> 2
            else -> 1
        }
    }
}

@Composable
fun EventTypeTile(
    event: EventTypeDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        if (selected) BookingPalette.ChampagneSoft else Color.White, tween(220), label = "tileBg"
    )
    val borderColor by animateColorAsState(
        if (selected) BookingPalette.Emerald else BookingPalette.Border, tween(220), label = "tileBorder"
    )
    val checkScale by animateFloatAsState(if (selected) 1f else 0f, tween(220), label = "check")
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = event.displayName
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        shape = shape,
        color = container,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) BookingPalette.Emerald else BookingPalette.EmeraldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        eventTypeIcon(event),
                        contentDescription = null,
                        tint = if (selected) BookingPalette.Champagne else BookingPalette.Emerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    event.displayName,
                    color = BookingPalette.Ink,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 3
                )
            }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(22.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(BookingPalette.Emerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Responsive 2-column (1 on very narrow / large-font, 3 on wide screens) grid of event-type tiles. */
@Composable
fun EventTypeGrid(
    selectedBackendValue: String?,
    onSelect: (EventTypeDefinition) -> Unit,
    modifier: Modifier = Modifier,
    eventTypes: List<EventTypeDefinition> = EventTypeCatalog.eventTypes,
    spacing: Dp = 10.dp
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val columns = EventTypeGridRules.columnsFor(maxWidth.value, fontScale)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(spacing)) {
            eventTypes.chunked(columns).forEach { rowItems ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    rowItems.forEach { event ->
                        EventTypeTile(
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

/** Field that summarises the chosen occasion and opens [EventTypeSelectorSheet]. */
@Composable
fun EventTypeSelectorField(
    selectedBackendValue: String?,
    onSelected: (EventTypeDefinition) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val selected = selectedBackendValue?.let { value ->
        EventTypeCatalog.eventTypes.firstOrNull { it.backendValue == value }
    }
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(selected = false, role = Role.Button, onClick = { showSheet = true })
            .semantics {
                contentDescription = "Event type, ${selected?.displayName ?: selectedBackendValue ?: "not selected"}"
            },
        shape = shape,
        color = Color.White,
        border = BorderStroke(
            1.dp,
            when {
                isError -> BookingPalette.Danger
                selectedBackendValue != null -> BookingPalette.Emerald
                else -> BookingPalette.FieldBorder
            }
        )
    ) {
        Row(
            Modifier
                .heightIn(min = 68.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected != null) BookingPalette.Emerald else BookingPalette.EmeraldSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    selected?.let(::eventTypeIcon) ?: Icons.Filled.Event,
                    null,
                    tint = if (selected != null) BookingPalette.Champagne else BookingPalette.Emerald,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Event type", color = BookingPalette.Muted, style = MaterialTheme.typography.labelMedium)
                Text(
                    selected?.displayName ?: selectedBackendValue ?: "Choose the occasion",
                    color = if (selectedBackendValue != null) BookingPalette.Ink else BookingPalette.Placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selectedBackendValue != null) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            Icon(Icons.Filled.ExpandMore, null, tint = BookingPalette.Muted)
        }
    }

    if (showSheet) {
        EventTypeSelectorSheet(
            selectedBackendValue = selectedBackendValue,
            onDismiss = { showSheet = false },
            onConfirm = { event ->
                onSelected(event)
                showSheet = false
            }
        )
    }
}

/**
 * Premium bottom sheet with a heading, subtitle and two-column tile grid. The choice is applied
 * only when the user confirms, so dismissing keeps the previous value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTypeSelectorSheet(
    selectedBackendValue: String?,
    onDismiss: () -> Unit,
    onConfirm: (EventTypeDefinition) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var pending by rememberSaveable { mutableStateOf(selectedBackendValue) }
    val pendingEvent = remember(pending) { EventTypeCatalog.eventTypes.firstOrNull { it.backendValue == pending } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BookingPalette.Ivory,
        contentColor = BookingPalette.Ink
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Column(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "What's the occasion?",
                    color = BookingPalette.Ink,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Pick the event type so vendors can tailor their service.",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                EventTypeGrid(selectedBackendValue = pending, onSelect = { pending = it.backendValue })
            }
            HorizontalDivider(color = BookingPalette.Border)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookingSecondaryButton(
                    text = "Cancel",
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
                    modifier = Modifier.weight(1f)
                )
                BookingPrimaryButton(
                    text = "Confirm",
                    enabled = pendingEvent != null,
                    onClick = {
                        val chosen = pendingEvent ?: return@BookingPrimaryButton
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onConfirm(chosen) }
                    },
                    modifier = Modifier.weight(1.6f)
                )
            }
        }
    }
}
