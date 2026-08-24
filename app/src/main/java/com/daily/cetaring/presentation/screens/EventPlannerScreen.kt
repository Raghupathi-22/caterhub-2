package com.daily.cetaring.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import com.daily.cetaring.data.remote.dto.CreateEventRequestDto
import com.daily.cetaring.data.remote.dto.SelectedRequirementDto
import com.daily.cetaring.presentation.viewmodel.EventPlannerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Cream = Color(0xFFFFFCF5)

@Composable
fun EventPlannerScreen(
    viewModel: EventPlannerViewModel,
    onBackClick: () -> Unit,
    onEventCreated: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var eventName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<String?>(null) }
    var endTime by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Hyderabad") }
    var guests by remember { mutableStateOf("100") }
    var budget by remember { mutableStateOf("") }
    var foodPreference by remember { mutableStateOf("") }
    var specialRequirements by remember { mutableStateOf("") }

    LaunchedEffect(state.created) {
        state.created?.event?.id?.let {
            viewModel.clearCreated()
            onEventCreated(it)
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text("Plan My Event", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("Back", color = Maroon) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!state.error.isNullOrBlank()) {
                item { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            }

            if (step == 0) {
                item {
                    Text("What are you planning?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("Choose an event and CaterHub will prepare the service checklist.", color = Color.Gray)
                }
                if (state.loading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                state.groups.forEach { group ->
                    item {
                        Text(group.group.replace('_', ' '), color = Gold, fontWeight = FontWeight.Bold)
                    }
                    items(group.types, key = { it.code }) { type ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Color(0xFFE4D9C6)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            onClick = {
                                viewModel.selectType(type.code, type.displayName, guests.toIntOrNull() ?: 100, budget.toDoubleOrNull() ?: 0.0)
                                step = 1
                            }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(type.displayName, fontWeight = FontWeight.Bold, color = Maroon)
                                Text("Plan and book services for this event.", color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(state.selectedDisplayName ?: "Event Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("Step ${if (step == 1) 1 else 2} of 2", color = Gold, fontWeight = FontWeight.Bold)
                }

                item {
                    OutlinedTextField(eventName, { eventName = it }, Modifier.fillMaxWidth(), label = { Text("Event name (optional)") })
                }
                item {
                    Button(
                        onClick = {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                    ) { Text(date?.format(DateTimeFormatter.ISO_DATE) ?: "Select event date") }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m -> startTime = "%02d:%02d".format(h, m) },
                                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                        }) { Text(startTime ?: "Start time") }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m -> endTime = "%02d:%02d".format(h, m) },
                                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                        }) { Text(endTime ?: "End time") }
                    }
                }
                item { OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Event location") }) }
                item { OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("City") }) }
                item {
                    OutlinedTextField(
                        guests, { guests = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(),
                        label = { Text("Number of guests") }, singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        budget, { budget = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.fillMaxWidth(),
                        label = { Text("Total budget (₹)") }, singleLine = true
                    )
                }
                item {
                    OutlinedTextField(foodPreference, { foodPreference = it }, Modifier.fillMaxWidth(),
                        label = { Text("Food preference (optional)") })
                }
                item {
                    OutlinedTextField(specialRequirements, { specialRequirements = it }, Modifier.fillMaxWidth(),
                        label = { Text("Special requirements (optional)") }, minLines = 2)
                }

                item {
                    Text("Recommended services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Required services are preselected. You can add or remove optional services.", color = Color.Gray)
                }

                items(state.checklist, key = { it.serviceKey }) { service ->
                    val checked = state.selectedServices.contains(service.serviceKey)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp)) {
                            Checkbox(checked = checked, onCheckedChange = {
                                if (!service.required) viewModel.toggleService(service.serviceKey, it)
                            }, enabled = !service.required)
                            Column(Modifier.weight(1f).padding(top = 7.dp)) {
                                Text(service.serviceName, fontWeight = FontWeight.Bold)
                                Text("${service.category} • ${service.unit}${if (service.required) " • Required" else " • Optional"}", color = Color.Gray)
                            }
                        }
                    }
                }

                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        enabled = !state.loading && date != null && location.isNotBlank() && (guests.toIntOrNull() ?: 0) > 0,
                        onClick = {
                            val selected = state.selectedServices.map { key ->
                                val item = state.checklist.firstOrNull { it.serviceKey == key }
                                SelectedRequirementDto(key, true, null, null, null)
                            }
                            viewModel.create(CreateEventRequestDto(
                                eventType = state.selectedType ?: "OTHER",
                                eventName = eventName.ifBlank { null },
                                eventDate = date!!.toString(),
                                startTime = startTime,
                                endTime = endTime,
                                location = location,
                                city = city,
                                guestCount = guests.toIntOrNull() ?: 1,
                                estimatedBudget = budget.toDoubleOrNull() ?: 0.0,
                                foodPreference = foodPreference.ifBlank { null },
                                specialRequirements = specialRequirements.ifBlank { null },
                                selectedServices = selected
                            ))
                        }
                    ) {
                        if (state.loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Create My Event")
                    }
                }
            }
        }
    }
}
