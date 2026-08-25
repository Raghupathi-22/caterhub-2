package com.daily.cetaring.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.BookingResponse
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color
@Composable
fun CaterHubPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CaterHubSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.height(54.dp), shape = RoundedCornerShape(18.dp)) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CaterHubSectionHeader(title: String, action: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        if (action != null && onActionClick != null) {
            Text(
                text = action,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun CaterHubStatusChip(status: String, modifier: Modifier = Modifier) {
    val normalized = status.uppercase(Locale.ROOT)
    val color = when (normalized) {
        "CONFIRMED", "ACTIVE", "ACCEPTED" -> MaterialTheme.colorScheme.primaryContainer
        "ASSIGNED", "IN_PROGRESS", "OFFERED" -> MaterialTheme.colorScheme.tertiaryContainer
        "COMPLETED", "DELIVERED" -> MaterialTheme.colorScheme.secondaryContainer
        "CANCELLED", "REJECTED", "DECLINED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (normalized) {
        "CANCELLED", "REJECTED", "DECLINED" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = CircleShape, color = color, modifier = modifier) {
        Text(
            text = normalized.replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CaterHubLoadingState(message: String = "Loading CaterHub...") {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CaterHubErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val normalized = message.lowercase(Locale.ROOT)
    val title = when {
        normalized.contains("session") || normalized.contains("sign in again") -> "Session expired"
        normalized.contains("permission") -> "Permission required"
        normalized.contains("connect") || normalized.contains("internet") || normalized.contains("timeout") -> "Connection issue"
        else -> "Something went wrong"
    }
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(34.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Retry") }
        }
    }
}

@Composable
fun CaterHubEmptyState(title: String, message: String, modifier: Modifier = Modifier, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (actionText != null && onActionClick != null) {
                Spacer(Modifier.height(2.dp))
                CaterHubPrimaryButton(text = actionText, onClick = onActionClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun CaterHubCategoryChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
        modifier = modifier
    )
}

@Composable
fun CaterHubBookingCard(booking: BookingResponse, onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val eventIcon = when (booking.eventType) {
        "Birthday", "Naming Ceremony" -> Icons.Filled.Cake
        "Wedding", "Engagement", "Baby Shower" -> Icons.Filled.Favorite
        "Housewarming" -> Icons.Filled.Home
        "Festival" -> Icons.Filled.Star
        else -> Icons.Filled.EventBusy
    }
    val accent = when (booking.eventType) {
        "Birthday", "Naming Ceremony" -> Color(0xFF971B1E)
        "Wedding", "Engagement" -> Color(0xFFC58A16)
        "Festival", "Housewarming" -> Color(0xFF0A672A)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, accent.copy(alpha=.20f)), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha=.10f)) { Icon(eventIcon, null, tint=accent, modifier=Modifier.padding(11.dp).size(27.dp)) }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.eventType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(booking.bookingReference ?: "Booking #${booking.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CaterHubStatusChip(booking.status)
            }
            Text("${booking.guestCount} Guests · ${booking.mealType}", fontWeight = FontWeight.Bold)
            Text(formatDateTime(booking.eventDateTime), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!compact) Text(booking.deliveryAddress, maxLines=2, overflow=TextOverflow.Ellipsis, color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(value.ifBlank { "Not provided" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

data class ReviewLineItem(
    val title: String,
    val subtitle: String,
    val amountText: String? = null
)

@Composable
fun ReviewRequestCard(
    eventType: String,
    eventDate: String,
    timeRange: String,
    location: String,
    services: List<ReviewLineItem>,
    totalLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4D9C6))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Review your request", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            SummaryRow("Event", eventType)
            SummaryRow("Date", eventDate)
            SummaryRow("Time", timeRange)
            SummaryRow("Location", location)
            Text("SERVICES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (services.isEmpty()) {
                Text("No services selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                services.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.amountText != null) {
                            Text(item.amountText, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE4D9C6))
            SummaryRow("Total", totalLabel)
        }
    }
}

fun formatDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "Date to be confirmed"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val formatter = SimpleDateFormat("dd MMM · h:mm a", Locale.ENGLISH)
        formatter.format(parser.parse(raw)!!)
    } catch (_: Exception) {
        raw.replace('T', ' ')
    }
}
