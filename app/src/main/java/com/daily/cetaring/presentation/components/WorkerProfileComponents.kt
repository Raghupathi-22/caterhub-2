package com.daily.cetaring.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.worker.WorkerRegistrationRules

private val RegEmerald get() = WorkerPalette.Emerald
private val RegTint get() = WorkerPalette.EmeraldTint

/** Segmented progress bar with the current step highlighted in champagne gold. */
@Composable
fun RegistrationProgressIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = "Step ${current + 1} of $total" },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { index ->
            val fill by animateFloatAsState(if (index <= current) 1f else 0f, tween(320), label = "step-fill-$index")
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(WorkerPalette.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .background(if (index == current) WorkerPalette.Gold else RegEmerald, CircleShape)
                )
            }
        }
    }
}

/** Round selection indicator shared by category and role cards. */
@Composable
fun RegistrationSelectionBadge(selected: Boolean, modifier: Modifier = Modifier, onPhoto: Boolean = false) {
    val color by animateColorAsState(
        if (selected) RegEmerald else if (onPhoto) Color.White.copy(alpha = 0.85f) else Color.Transparent,
        tween(200),
        label = "badge"
    )
    Box(
        modifier
            .size(26.dp)
            .background(color, CircleShape)
            .border(1.5.dp, if (selected) Color.White else WorkerPalette.FieldBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

/**
 * Category tile: consistent photo header (branded artwork fallback), title and short description.
 * Selected = light emerald surface, emerald border and a check badge; no overlays on the photo.
 */
@Composable
fun PremiumServiceCategoryCard(
    title: String,
    subtitle: String,
    visual: ServiceVisual,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 96.dp
) {
    val background by animateColorAsState(if (selected) RegTint else Color.White, tween(200), label = "category-bg")
    val borderColor by animateColorAsState(if (selected) RegEmerald else WorkerPalette.Border, tween(200), label = "category-border")
    val scale by animateFloatAsState(if (selected) 1f else 0.985f, tween(200), label = "category-scale")
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .clip(shape)
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = "$title. $subtitle"
                stateDescription = if (selected) "Selected" else "Not selected"
            }
    ) {
        Box(Modifier.fillMaxWidth().height(imageHeight).padding(6.dp)) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(imageHeight * scale),
                shape = RoundedCornerShape(17.dp),
                iconSize = 28.dp
            )
            RegistrationSelectionBadge(selected, Modifier.align(Alignment.TopEnd).padding(8.dp), onPhoto = true)
        }
        Column(
            Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                title,
                color = if (selected) RegEmerald else WorkerPalette.Ink,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                color = WorkerPalette.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Role row with a role photo (or a distinct role icon tile), name, short description and radio badge. */
@Composable
fun PremiumProfessionalRoleCard(
    title: String,
    description: String?,
    visual: ServiceVisual,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(if (selected) RegTint else Color.White, tween(200), label = "role-bg")
    val borderColor by animateColorAsState(if (selected) RegEmerald else WorkerPalette.Border, tween(200), label = "role-border")
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(shape)
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { stateDescription = if (selected) "Selected" else "Not selected" }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (visual.photoRes != null) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                iconSize = 22.dp
            )
        } else {
            Box(
                Modifier
                    .size(60.dp)
                    .background(if (selected) RegEmerald else visual.accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (selected) Color.White else visual.accent, modifier = Modifier.size(28.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                color = if (selected) RegEmerald else WorkerPalette.Ink,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            if (description != null) {
                Text(description, color = WorkerPalette.Muted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        RegistrationSelectionBadge(selected)
    }
}

/** Circular completion ring with the percentage in the middle. */
@Composable
fun ProfileCompletionIndicator(percent: Int, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val progress by animateFloatAsState(percent.coerceIn(0, 100) / 100f, tween(600), label = "completion")
    val complete = percent >= 100
    Box(
        modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = "Profile $percent percent complete" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size)) {
            val stroke = 6.dp.toPx()
            drawArc(WorkerPalette.Border, -90f, 360f, false, style = Stroke(stroke))
            drawArc(
                if (complete) WorkerPalette.Emerald else WorkerPalette.Gold,
                -90f,
                360f * progress,
                false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text("$percent%", color = WorkerPalette.Ink, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
    }
}

/**
 * Identity area of the profile preview: role photo, role, service, experience and the completion
 * ring with a short checklist of what is missing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilePreviewHeader(
    roleTitle: String,
    categoryTitle: String?,
    experienceLabel: String?,
    visual: ServiceVisual,
    strength: Int,
    checklist: List<WorkerRegistrationRules.ChecklistItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    val missingRequired = checklist.filter { it.required && !it.done }
    val bioMissing = checklist.any { !it.required && !it.done }
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, WorkerPalette.Gold.copy(alpha = 0.55f), shape)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(WorkerPalette.Emerald, WorkerPalette.Gold, WorkerPalette.Emerald)))
        )
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ServiceImage(
                    visual = visual,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .border(2.dp, WorkerPalette.Gold, CircleShape)
                        .padding(3.dp),
                    shape = CircleShape,
                    iconSize = 26.dp
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "YOUR PARTNER PROFILE",
                        color = Color(0xFF8A6A2A),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        roleTitle,
                        color = WorkerPalette.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        listOfNotNull(categoryTitle, experienceLabel).joinToString(" · "),
                        color = WorkerPalette.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WorkerPalette.Ivory)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileCompletionIndicator(strength)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        when {
                            missingRequired.isNotEmpty() -> "${missingRequired.size} required item${if (missingRequired.size == 1) "" else "s"} missing"
                            bioMissing -> "Ready to submit"
                            else -> "Profile complete"
                        },
                        color = if (missingRequired.isEmpty()) WorkerPalette.Emerald else WorkerPalette.Maroon,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when {
                            missingRequired.isNotEmpty() -> "Tap an item below to complete it."
                            bioMissing -> "Optional: add a short bio to reach 100% and stand out to customers."
                            else -> "Customers will see a complete, professional profile."
                        },
                        color = WorkerPalette.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (missingRequired.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    checklist.forEach { item ->
                        ChecklistPill(item, onClick = { onItemClick(item.step) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistPill(item: WorkerRegistrationRules.ChecklistItem, onClick: () -> Unit) {
    val done = item.done
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (done) RegTint else WorkerPalette.Maroon.copy(alpha = 0.07f))
            .selectable(selected = false, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics { stateDescription = if (done) "Done" else if (item.required) "Missing" else "Optional" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) WorkerPalette.Emerald else WorkerPalette.Maroon,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            item.label + if (item.required) "" else " (optional)",
            color = if (done) WorkerPalette.Emerald else WorkerPalette.Maroon,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/** Review section with an icon, heading and a compact Edit action. */
@Composable
fun ProfileReviewSection(
    title: String,
    icon: ImageVector,
    onEdit: (() -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    missing: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, if (missing) WorkerPalette.Maroon.copy(alpha = 0.35f) else WorkerPalette.Border, shape)
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).background(WorkerPalette.GoldTint, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = WorkerPalette.Emerald, modifier = Modifier.size(18.dp))
            }
            Text(
                title,
                color = WorkerPalette.Ink,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f).padding(start = 10.dp).semantics { heading() }
            )
            if (onEdit != null) {
                TextButton(onClick = onEdit, enabled = enabled, modifier = Modifier.semantics { contentDescription = "Edit $title" }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = WorkerPalette.Maroon, modifier = Modifier.size(15.dp))
                    Text(" Edit", color = WorkerPalette.Maroon, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Column(Modifier.padding(top = 8.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}
