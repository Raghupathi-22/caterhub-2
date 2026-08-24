package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daily.cetaring.presentation.viewmodel.EventUiState
import com.daily.cetaring.presentation.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val eventTypes = listOf(
    "MARRIAGE" to "Marriage",
    "ENGAGEMENT" to "Engagement",
    "BIRTHDAY" to "Birthday",
    "POOJA" to "Pooja / Religious Function",
    "BABY_FUNCTION" to "Baby Function",
    "HOUSEWARMING" to "Housewarming",
    "ANNIVERSARY" to "Anniversary",
    "CORPORATE" to "Corporate Event",
    "SCHOOL_COLLEGE" to "School / College",
    "OTHER" to "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPlannerScreen(
    viewModel: EventViewModel,
    initialEventType: String? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember {
        mutableStateOf(
            eventTypes.firstOrNull { it.first.equals(initialEventType, true) }?.first ?: "MARRIAGE"
        )
    }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var location by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTime by remember { mutableStateOf(false) }
    var showEndTime by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is EventUiState.Created) {
            // Keep the workspace visible; user can go back after reviewing it.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Plan My Event")
            Spacer(Modifier.width(60.dp))
        }

        Text("Everything for your event in one place.")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(100) },
            label = { Text("Event name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Column {
            OutlinedTextField(
                value = eventTypes.first { it.first == type }.second,
                onValueChange = {},
                label = { Text("Event type") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            DropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                eventTypes.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            type = code
                            typeExpanded = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(1.dp))
            Button(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Choose Event Type")
            }
        }

        OutlinedTextField(
            value = date?.toString() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Event date") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Date")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = startTime?.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start time") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { showStartTime = true }) { Text("Select") }
            }
            Column(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = endTime?.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End time") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { showEndTime = true }) { Text("Select") }
            }
        }

        OutlinedTextField(
            value = location,
            onValueChange = { location = it.take(255) },
            label = { Text("Event location") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = guests,
            onValueChange = { guests = it.filter(Char::isDigit).take(5) },
            label = { Text("Number of guests") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
            label = { Text("Total budget (₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (state is EventUiState.Created) {
            val workspace = (state as EventUiState.Created).workspace
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Event created: ${workspace.eventCode}")
                    Text("Budget: ₹${workspace.budget ?: 0}")
                    Text("Checklist")
                    workspace.checklist.forEach { item ->
                        Text("• ${item.category}")
                    }
                }
            }
        }

        if (state is EventUiState.Error) {
            Text((state as EventUiState.Error).message)
        }

        Button(
            onClick = {
                val selectedDate = date ?: return@Button
                if (name.isBlank() || location.isBlank()) return@Button
                viewModel.createEvent(
                    name = name,
                    eventType = type,
                    date = selectedDate.toString(),
                    startTime = startTime?.toString(),
                    endTime = endTime?.toString(),
                    location = location,
                    guests = guests.toIntOrNull(),
                    budget = budget
                )
            },
            enabled = state !is EventUiState.Loading && date != null && name.isNotBlank() && location.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is EventUiState.Loading) CircularProgressIndicator(modifier = Modifier.height(20.dp))
            else Text("Create Event & Generate Plan")
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showStartTime) {
        val timeState = rememberTimePickerState(is24Hour = false)
        AlertDialog(
            onDismissRequest = { showStartTime = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timeState.hour, timeState.minute)
                    showStartTime = false
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { showStartTime = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showEndTime) {
        val timeState = rememberTimePickerState(is24Hour = false)
        AlertDialog(
            onDismissRequest = { showEndTime = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timeState.hour, timeState.minute)
                    showEndTime = false
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { showEndTime = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }
}
