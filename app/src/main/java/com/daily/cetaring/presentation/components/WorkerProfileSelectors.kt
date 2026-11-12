package com.daily.cetaring.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Premium onboarding palette shared by worker registration and worker profile editing. */
object WorkerPalette {
    val Ivory = Color(0xFFFBF8F1)
    val Emerald = Color(0xFF173F32)
    val EmeraldTint = Color(0xFFE7F0EB)
    val Maroon = Color(0xFF941B27)
    val Gold = Color(0xFFD9B979)
    val GoldTint = Color(0xFFF6EEDC)
    val Ink = Color(0xFF1F2420)
    val Muted = Color(0xFF6B6F69)
    val Border = Color(0xFFE6E0D3)
    val FieldBorder = Color(0xFFCFC7B6)
    val Surface = Color.White
}

@Composable
fun workerSearchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = WorkerPalette.Ink,
    unfocusedTextColor = WorkerPalette.Ink,
    focusedContainerColor = WorkerPalette.Ivory,
    unfocusedContainerColor = WorkerPalette.Ivory,
    cursorColor = WorkerPalette.Emerald,
    focusedBorderColor = WorkerPalette.Emerald,
    unfocusedBorderColor = WorkerPalette.Border,
    focusedLeadingIconColor = WorkerPalette.Emerald,
    unfocusedLeadingIconColor = WorkerPalette.Muted,
    focusedTrailingIconColor = WorkerPalette.Muted,
    unfocusedTrailingIconColor = WorkerPalette.Muted,
    focusedPlaceholderColor = WorkerPalette.Muted,
    unfocusedPlaceholderColor = WorkerPalette.Muted,
    focusedSupportingTextColor = WorkerPalette.Muted,
    unfocusedSupportingTextColor = WorkerPalette.Muted
)

/** Pill-shaped selectable chip with an animated emerald selected state and a 40dp+ touch target. */
@Composable
fun WorkerSelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val container by animateColorAsState(
        if (selected) WorkerPalette.Emerald else WorkerPalette.Surface, tween(180), label = "chip-bg"
    )
    val content by animateColorAsState(
        if (selected) Color.White else WorkerPalette.Ink, tween(180), label = "chip-fg"
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .background(container)
            .border(1.dp, if (selected) WorkerPalette.Emerald else WorkerPalette.FieldBorder, shape)
            .clickable(enabled = enabled, role = Role.Checkbox, onClick = onClick)
            .semantics {
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        Text(label, color = content, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Removable chip used in the "selected" summary row. */
@Composable
fun WorkerRemovableChip(label: String, onRemove: () -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(shape)
            .background(WorkerPalette.EmeraldTint)
            .border(1.dp, WorkerPalette.Emerald.copy(alpha = 0.35f), shape)
            .padding(start = 12.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = WorkerPalette.Emerald,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onRemove, enabled = enabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remove $label", tint = WorkerPalette.Emerald, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Searchable multi-select section: selected items as removable chips, a search box, and the
 * catalogue options as toggle chips. Values already saved on a profile but not in [options] are
 * kept and shown, so existing worker data is never silently dropped.
 *
 * @param onAddCustom when non-null, the worker can add a value that is not in the catalogue.
 *        It returns an error message, or null when the value was accepted.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerMultiSelectSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<String>,
    selected: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search",
    collapsedCount: Int = 12,
    maxSelected: Int = Int.MAX_VALUE,
    error: String? = null,
    enabled: Boolean = true,
    onAddCustom: ((String) -> String?)? = null
) {
    var query by rememberSaveable(title) { mutableStateOf("") }
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    var customError by remember(title) { mutableStateOf<String?>(null) }

    val allOptions = remember(options, selected) {
        (options + selected.filter { s -> options.none { it.equals(s, true) } }).distinctBy { it.lowercase() }
    }
    val trimmedQuery = query.trim()
    val filtered = remember(allOptions, trimmedQuery) {
        if (trimmedQuery.isEmpty()) allOptions else allOptions.filter { it.contains(trimmedQuery, ignoreCase = true) }
    }
    val canCollapse = trimmedQuery.isEmpty() && filtered.size > collapsedCount
    val visible = if (canCollapse && !expanded) filtered.take(collapsedCount) else filtered
    val atLimit = selected.size >= maxSelected
    val exactMatch = allOptions.any { it.equals(trimmedQuery, ignoreCase = true) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = WorkerPalette.Surface,
        border = BorderStroke(1.dp, if (error != null) MaterialTheme.colorScheme.error else WorkerPalette.Border)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp).background(WorkerPalette.GoldTint, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, contentDescription = null, tint = WorkerPalette.Emerald, modifier = Modifier.size(22.dp)) }
                Column(Modifier.weight(1f)) {
                    Text(title, color = WorkerPalette.Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = WorkerPalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
                if (selected.isNotEmpty()) {
                    Box(
                        Modifier.background(WorkerPalette.Emerald, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)
                            .semantics { contentDescription = "${selected.size} selected" }
                    ) {
                        Text("${selected.size}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(selected.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selected.forEach { item -> WorkerRemovableChip(item, onRemove = { onToggle(item) }, enabled = enabled) }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(40); customError = null },
                placeholder = { Text(searchPlaceholder) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "Clear search") } }
                } else null,
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = workerSearchFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                modifier = Modifier.fillMaxWidth()
            )

            if (visible.isEmpty()) {
                Text(
                    if (trimmedQuery.isEmpty()) "No options available yet." else "No matches for \"$trimmedQuery\".",
                    color = WorkerPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visible.forEach { option ->
                        val isSelected = selected.any { it.equals(option, ignoreCase = true) }
                        WorkerSelectChip(
                            label = option,
                            selected = isSelected,
                            enabled = enabled && (isSelected || !atLimit),
                            onClick = { onToggle(option) }
                        )
                    }
                }
            }

            if (canCollapse) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) "Show fewer" else "Show all ${filtered.size}",
                        color = WorkerPalette.Emerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (onAddCustom != null && trimmedQuery.isNotEmpty() && !exactMatch && !atLimit) {
                TextButton(
                    enabled = enabled,
                    onClick = {
                        val problem = onAddCustom(trimmedQuery)
                        customError = problem
                        if (problem == null) query = ""
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = WorkerPalette.Maroon, modifier = Modifier.size(18.dp))
                    Text(" Add \"$trimmedQuery\"", color = WorkerPalette.Maroon, fontWeight = FontWeight.Bold)
                }
            }

            val helper = customError ?: error ?: if (atLimit) "You've reached the limit of $maxSelected." else null
            if (helper != null) {
                Text(
                    helper,
                    color = if (customError != null || error != null) MaterialTheme.colorScheme.error else WorkerPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Optional bio input with a live character counter. */
@Composable
fun WorkerBioField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Short bio (optional)",
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(maxLength)) },
        label = { Text(label) },
        placeholder = { Text("Tell customers about your experience and the services you offer.") },
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(error.orEmpty(), modifier = Modifier.weight(1f))
                Text("${value.length}/$maxLength")
            }
        },
        isError = error != null,
        enabled = enabled,
        minLines = 3,
        maxLines = 6,
        shape = RoundedCornerShape(16.dp),
        colors = workerSearchFieldColors(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.fillMaxWidth()
    )
}
