package com.daily.cetaring.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Locale

/** Shared palette for the customer and worker profile screens. */
object ProfilePalette {
    val Background = Color(0xFFFBF7F0)
    val Card = Color.White
    val Burgundy = Color(0xFF8E1C2B)
    val BurgundySoft = Color(0xFFF7E9EA)
    val Green = Color(0xFF0B5D2A)
    val GreenSoft = Color(0xFFE8F2EA)
    val Amber = Color(0xFF8A5A00)
    val AmberSoft = Color(0xFFFFF3D6)
    val Ink = Color(0xFF26221F)
    val Muted = Color(0xFF6A635C)
    val Border = Color(0xFFEAE2D6)
    val Divider = Color(0xFFF1ECE4)
    val Danger = Color(0xFFB3261E)
    val DangerSoft = Color(0xFFFCECEA)
}

private val CardShape = RoundedCornerShape(20.dp)

fun profileInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val initials = when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0].take(1)
        else -> parts.first().take(1) + parts.last().take(1)
    }
    return initials.uppercase(Locale.getDefault()).ifBlank { "?" }
}

@Composable
fun ProfileAvatar(name: String, imageUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(ProfilePalette.BurgundySoft),
        contentAlignment = Alignment.Center
    ) {
        Text(
            profileInitials(name),
            color = ProfilePalette.Burgundy,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        // Initials stay visible underneath if the image is missing or fails to load.
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(CircleShape)
            )
        }
    }
}

@Composable
fun ProfileBadge(text: String, container: Color = ProfilePalette.BurgundySoft, content: Color = ProfilePalette.Burgundy) {
    Surface(shape = CircleShape, color = container) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHeaderCard(
    name: String,
    mobileNumber: String?,
    mobileVerified: Boolean,
    accountType: String,
    imageUrl: String? = null,
    onEditClick: (() -> Unit)? = null,
    extraBadges: @Composable () -> Unit = {},
    footer: (@Composable () -> Unit)? = null
) {
    ProfileCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(name = name, imageUrl = imageUrl)
                Column(
                    Modifier.padding(start = 14.dp).weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfilePalette.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!mobileNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Phone, contentDescription = null, tint = ProfilePalette.Muted, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(mobileNumber, color = ProfilePalette.Muted, style = MaterialTheme.typography.bodyMedium)
                            if (mobileVerified) {
                                Spacer(Modifier.width(5.dp))
                                Icon(Icons.Filled.Verified, contentDescription = "Verified mobile number", tint = ProfilePalette.Green, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        ProfileBadge(accountType)
                        extraBadges()
                    }
                }
            }
            if (onEditClick != null) {
                Surface(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp),
                    color = ProfilePalette.BurgundySoft,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 11.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = ProfilePalette.Burgundy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit profile", color = ProfilePalette.Burgundy, fontWeight = FontWeight.Bold)
                    }
                }
            }
            footer?.invoke()
        }
    }
}

@Composable
fun ProfileCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = ProfilePalette.Card),
        border = BorderStroke(1.dp, ProfilePalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { content() }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    ProfileCard(modifier) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(ProfilePalette.BurgundySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = ProfilePalette.Burgundy, modifier = Modifier.size(19.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfilePalette.Ink,
                    modifier = Modifier.padding(start = 12.dp).weight(1f)
                )
                if (actionLabel != null && onActionClick != null) {
                    TextButton(onClick = onActionClick) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = ProfilePalette.Burgundy, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(actionLabel, color = ProfilePalette.Burgundy, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.size(6.dp))
            content()
        }
    }
}

/** Label/value pair. Blank values render as a subtle "Not provided". */
@Composable
fun ProfileInfoRow(
    label: String,
    value: String?,
    showDivider: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = ProfilePalette.Muted)
                if (value.isNullOrBlank()) {
                    Text("Not provided", style = MaterialTheme.typography.bodyLarge, color = ProfilePalette.Muted.copy(alpha = 0.8f))
                } else {
                    Text(value, style = MaterialTheme.typography.bodyLarge, color = ProfilePalette.Ink, fontWeight = FontWeight.SemiBold)
                }
            }
            trailing?.invoke()
        }
        if (showDivider) HorizontalDivider(color = ProfilePalette.Divider)
    }
}

/** Comma-separated values rendered as chips; falls back to "Not provided". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileChipsRow(label: String, rawValue: String?, showDivider: Boolean = true) {
    val items = rawValue.orEmpty().split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    if (items.isEmpty()) {
        ProfileInfoRow(label, null, showDivider)
        return
    }
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ProfilePalette.Muted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    Surface(shape = RoundedCornerShape(10.dp), color = ProfilePalette.Background, border = BorderStroke(1.dp, ProfilePalette.Border)) {
                        Text(
                            item,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ProfilePalette.Ink,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        if (showDivider) HorizontalDivider(color = ProfilePalette.Divider)
    }
}

@Composable
fun ProfileActionGroup(title: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = ProfilePalette.Muted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        ProfileCard { Column { content() } }
    }
}

@Composable
fun ProfileActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null,
    destructive: Boolean = false,
    showChevron: Boolean = true,
    showDivider: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val tint = if (destructive) ProfilePalette.Danger else ProfilePalette.Burgundy
    val iconBg = if (destructive) ProfilePalette.DangerSoft else ProfilePalette.BurgundySoft
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    title,
                    color = if (destructive) ProfilePalette.Danger else ProfilePalette.Ink,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(subtitle, color = ProfilePalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            when {
                loading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = tint)
                showChevron -> Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ProfilePalette.Muted
                )
            }
        }
        if (showDivider) HorizontalDivider(color = ProfilePalette.Divider, modifier = Modifier.padding(start = 66.dp))
    }
}

@Composable
fun ProfileConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProfilePalette.Card,
        icon = icon?.let { { Icon(it, contentDescription = null, tint = if (destructive) ProfilePalette.Danger else ProfilePalette.Burgundy) } },
        title = { Text(title, color = ProfilePalette.Ink, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = ProfilePalette.Muted) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) ProfilePalette.Danger else ProfilePalette.Burgundy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ProfilePalette.Ink) }
        }
    )
}

@Composable
fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ProfilePalette.Burgundy,
    focusedLabelColor = ProfilePalette.Burgundy,
    cursorColor = ProfilePalette.Burgundy,
    unfocusedBorderColor = ProfilePalette.Border,
    unfocusedLabelColor = ProfilePalette.Muted,
    focusedTextColor = ProfilePalette.Ink,
    unfocusedTextColor = ProfilePalette.Ink,
    disabledTextColor = ProfilePalette.Muted,
    disabledBorderColor = ProfilePalette.Divider,
    disabledLabelColor = ProfilePalette.Muted,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = ProfilePalette.Background
)

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supporting: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { { Text(it) } },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(14.dp),
        colors = profileTextFieldColors(),
        modifier = modifier.fillMaxWidth()
    )
}
