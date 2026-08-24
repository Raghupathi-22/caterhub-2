package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.EventDashboardDto
import com.daily.cetaring.data.repository.EventRepository
import kotlinx.coroutines.launch

@Composable
fun EventDashboardScreen(
    eventId: Long,
    repository: EventRepository,
    onBackClick: () -> Unit
) {
    var dashboard by remember { mutableStateOf<EventDashboardDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        runCatching { repository.dashboard(eventId) }
            .onSuccess { dashboard = it }
            .onFailure { error = it.message ?: "Unable to load event" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Event", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("Back", color = Color(0xFF971B1E)) } }
            )
        }
    ) { padding ->
        when {
            error != null -> Text(error!!, modifier = Modifier.padding(padding).padding(20.dp), color = MaterialTheme.colorScheme.error)
            dashboard == null -> Box(Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(Modifier.padding(20.dp))
            }
            else -> {
                val d = dashboard!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(d.event.eventName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                                Text("${d.event.eventType} • ${d.event.eventDate}")
                                Text("${d.event.location} • ${d.event.guestCount} guests")
                                Text("Event ID: ${d.event.eventCode}", color = Color.Gray)
                                Spacer(Modifier.height(5.dp))
                                Text("Budget: ₹${"%,.0f".format(d.event.estimatedBudget)}", fontWeight = FontWeight.Bold)
                                Text("Planned: ₹${"%,.0f".format(d.event.totalEstimatedCost)}")
                                Text("Booked: ₹${"%,.0f".format(d.event.totalBookedAmount)}")
                                Text("Remaining: ₹${"%,.0f".format(d.event.remainingBudget)}")
                                Text("Progress: ${d.event.bookedRequired}/${d.event.requiredCount} required services booked")
                            }
                        }
                    }
                    d.budgetWarning?.let { warning ->
                        item { Text(warning, color = Color(0xFF9A5B00), fontWeight = FontWeight.Bold) }
                    }
                    item {
                        Text("Your Event Checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }
                    items(d.requirements, key = { it.id }) { r ->
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text(r.serviceName, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                    Text(if (r.required) "Required" else "Optional", color = if (r.required) Color(0xFF971B1E) else Color.Gray)
                                }
                                Text("${r.category} • ${r.quantity} ${r.unit.lowercase()}")
                                Text("Status: ${r.status.replace('_', ' ')}")
                                Text("Estimated: ₹${"%,.0f".format(r.customerBudget)}")
                            }
                        }
                    }
                    item {
                        Text("Timeline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }
                    items(d.timeline, key = { it.id }) { t ->
                        Column(Modifier.padding(vertical = 3.dp)) {
                            Text(t.title, fontWeight = FontWeight.Bold)
                            if (!t.detail.isNullOrBlank()) Text(t.detail!!, color = Color.Gray)
                            Text(t.occurredAt, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
