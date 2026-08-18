package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.StaffingRequirement
import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.presentation.components.CaterHubBookingCard
import com.daily.cetaring.presentation.components.CaterHubCategoryChip
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.SummaryRow
import com.daily.cetaring.presentation.components.formatDateTime
import com.daily.cetaring.presentation.viewmodel.BookingUiState
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun BookingFlowScreen(viewModel: BookingViewModel, onBackClick: () -> Unit, onSubmitted: (Long) -> Unit) {
    val draft by viewModel.draft.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BookingUiState.Error -> snackbar.showSnackbar(state.message)
            is BookingUiState.Submitted -> onSubmitted(state.booking.id)
            else -> Unit
        }
    }

    Scaffold(
        topBar = { BookingTopBar("Book your event", onBackClick) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Step ${step + 1} of 4", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    0 -> EventStep(draft, viewModel)
                    1 -> ServicesAndLocationStep(draft, viewModel)
                    2 -> DateTimeStep(draft, viewModel)
                    else -> ReviewStep(draft) { step = it }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CaterHubSecondaryButton(
                    text = if (step == 0) "Cancel" else "Back",
                    onClick = { if (step == 0) onBackClick() else step-- },
                    modifier = Modifier.weight(1f)
                )
                CaterHubPrimaryButton(
                    text = if (step == 3) "Submit Booking" else "Continue",
                    onClick = {
                        val validation = if (step == 3) {
                            com.daily.cetaring.data.remote.dto.BookingValidator.validateForSubmit(draft)
                        } else {
                            viewModel.validateStep(step)
                        }
                        if (validation is BookingValidationResult.Invalid) {
                            coroutineScope.launch { snackbar.showSnackbar(validation.message) }
                        } else if (step < 3) {
                            step++
                        } else {
                            viewModel.submitBooking()
                        }
                    },
                    loading = uiState is BookingUiState.Loading,
                    modifier = Modifier.weight(1.35f)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EventStep(draft: BookingDraft, viewModel: BookingViewModel) {
    Text("Tell us about your event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    Text("Choose an event type and expected guest count.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.eventTypes.forEach { event ->
            CaterHubCategoryChip(event, draft.eventType == event, onClick = { viewModel.updateDraft { it.copy(eventType = event) } })
        }
    }
    Text("Guest count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.guestQuickOptions.forEach { guests ->
            CaterHubCategoryChip("$guests", draft.guestCount == guests, onClick = {
                viewModel.updateDraft { it.copy(guestCount = guests) }
            })
        }
    }
    OutlinedTextField(
        value = if (draft.guestCount in BookingOptions.guestQuickOptions) "" else draft.guestCount?.toString().orEmpty(),
        onValueChange = { value ->
            viewModel.updateDraft { it.copy(guestCount = value.filter(Char::isDigit).toIntOrNull()) }
        },
        label = { Text("Custom guest count") },
        supportingText = { Text("Enter a positive whole number.") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ServicesAndLocationStep(draft: BookingDraft, viewModel: BookingViewModel) {
    Text("Choose your catering services", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    Text("Food", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.foodServices.forEach { service ->
            CaterHubCategoryChip(service, draft.foodService == service, onClick = {
                viewModel.updateDraft { it.copy(foodService = service) }
            })
        }
    }
    Text("Staff", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("Set a worker count and per-worker offer for each role you need.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    BookingOptions.staffingServices.forEach { workerType ->
        StaffingSelector(
            workerType = workerType,
            requirement = draft.staffingRequirements[workerType] ?: StaffingRequirement(),
            onUpdate = { requirement ->
                viewModel.updateDraft {
                    it.copy(staffingRequirements = it.staffingRequirements.toMutableMap().apply {
                        if (requirement.quantity == 0) remove(workerType) else put(workerType, requirement)
                    })
                }
            }
        )
    }
    Text("Event location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    OutlinedTextField(draft.address, { value -> viewModel.updateDraft { it.copy(address = value) } }, label = { Text("Event address") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.area, { value -> viewModel.updateDraft { it.copy(area = value) } }, label = { Text("Area / locality") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.city, { value -> viewModel.updateDraft { it.copy(city = value) } }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.landmark, { value -> viewModel.updateDraft { it.copy(landmark = value) } }, label = { Text("Landmark (optional)") }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun StaffingSelector(workerType: WorkerType, requirement: StaffingRequirement, onUpdate: (StaffingRequirement) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(workerType.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (requirement.quantity > 0) "Request ${requirement.quantity} ${workerType.label}" else "Not requested",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onUpdate(requirement.copy(quantity = (requirement.quantity - 1).coerceAtLeast(0))) }, enabled = requirement.quantity > 0) {
                        Icon(Icons.Filled.Remove, "Remove worker")
                    }
                    Text(requirement.quantity.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onUpdate(requirement.copy(quantity = requirement.quantity + 1)) }) {
                        Icon(Icons.Filled.Add, "Add worker")
                    }
                }
            }
            if (requirement.quantity > 0) {
                OutlinedTextField(
                    value = requirement.paymentPerWorker?.toPlainString().orEmpty(),
                    onValueChange = { value ->
                        onUpdate(requirement.copy(paymentPerWorker = value.toBigDecimalOrNull()))
                    },
                    label = { Text("Offer per worker (INR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(draft: BookingDraft, viewModel: BookingViewModel) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showEndTime by remember { mutableStateOf(false) }
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val dateState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= today
            override fun isSelectableYear(year: Int): Boolean = year >= Calendar.getInstance().get(Calendar.YEAR)
        }
    )
    val timeState = rememberTimePickerState(is24Hour = false)
    val endTimeState = rememberTimePickerState(is24Hour = false)

    Text("When is your event?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    PickerField("Date", draft.eventDate.ifBlank { "Select date" }) { showDate = true }
    PickerField("Time", draft.eventTime.ifBlank { "Select time" }) { showTime = true }
    if (draft.staffingRequirements.values.any { it.quantity > 0 }) {
        PickerField("Staffing shift ends", draft.staffingEndTime.ifBlank { "Select end time" }) { showEndTime = true }
    }
    OutlinedTextField(
        value = draft.foodRequirements,
        onValueChange = { value -> viewModel.updateDraft { it.copy(foodRequirements = value) } },
        label = { Text("Food requirements (optional)") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = draft.specialInstructions,
        onValueChange = { value -> viewModel.updateDraft { it.copy(specialInstructions = value) } },
        label = { Text("Additional instructions (optional)") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
    if (showDate) {
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        viewModel.updateDraft {
                            it.copy(eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(millis))
                        }
                    }
                    showDate = false
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
    if (showTime) {
        TimePickerDialog(
            state = timeState,
            onConfirm = {
                viewModel.updateDraft { it.copy(eventTime = "%02d:%02d".format(timeState.hour, timeState.minute)) }
                showTime = false
            },
            onDismiss = { showTime = false }
        )
    }
    if (showEndTime) {
        TimePickerDialog(
            state = endTimeState,
            onConfirm = {
                viewModel.updateDraft { it.copy(staffingEndTime = "%02d:%02d".format(endTimeState.hour, endTimeState.minute)) }
                showEndTime = false
            },
            onDismiss = { showEndTime = false }
        )
    }
}

@Composable
private fun PickerField(label: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    state: androidx.compose.material3.TimePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ReviewStep(draft: BookingDraft, onEdit: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Review your booking", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        IconButton(onClick = { onEdit(0) }) { Icon(Icons.Filled.Edit, "Edit booking") }
    }
    SummaryRow("Event", draft.eventType)
    SummaryRow("Guests", "${draft.guestCount ?: 0} guests")
    SummaryRow("Services", draft.mealTypeForBackend())
    draft.staffingRequirements.filterValues { it.quantity > 0 }.forEach { (type, requirement) ->
        SummaryRow(type.label, "${requirement.quantity} requested · INR ${requirement.paymentPerWorker} each")
    }
    SummaryRow("Date", draft.eventDate)
    SummaryRow("Time", draft.eventTime)
    if (draft.staffingEndTime.isNotBlank()) SummaryRow("Staffing shift ends", draft.staffingEndTime)
    SummaryRow("Location", draft.deliveryAddress())
}

@Composable
fun BookingSuccessScreen(viewModel: BookingViewModel, bookingId: Long, onViewBooking: (Long) -> Unit, onHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(bookingId) { if (uiState !is BookingUiState.Submitted) viewModel.loadBooking(bookingId) }
    val submitted = uiState as? BookingUiState.Submitted
    val booking = submitted?.booking ?: (uiState as? BookingUiState.DetailsLoaded)?.booking
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
        Text("Booking request submitted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Your request has been sent to CaterHub.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        if (booking != null) BookingSummaryCard(booking)
        if (submitted?.staffingError != null) {
            Spacer(Modifier.height(12.dp))
            Text(submitted.staffingError, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        CaterHubPrimaryButton("View Booking", { onViewBooking(bookingId) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        CaterHubSecondaryButton("Back to Home", onHome, Modifier.fillMaxWidth())
    }
}

@Composable
fun BookingHistoryScreen(viewModel: BookingViewModel, onBackClick: () -> Unit, onBookingClick: (Long) -> Unit, onBookCateringClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadBookings() }
    Scaffold(topBar = { BookingTopBar("My Bookings", onBackClick) }) { padding ->
        when (val state = uiState) {
            BookingUiState.Loading, BookingUiState.Idle -> CaterHubLoadingState("Loading your bookings...")
            is BookingUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(state.message, { viewModel.loadBookings() }) }
            is BookingUiState.ListLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.bookings.isEmpty()) CaterHubEmptyState("No bookings yet", "Book catering for your next event.", actionText = "Book Catering", onActionClick = onBookCateringClick)
                else state.bookings.forEach { CaterHubBookingCard(it, { onBookingClick(it.id) }) }
            }
            else -> Unit
        }
    }
}

@Composable
fun BookingDetailsScreen(viewModel: BookingViewModel, bookingId: Long, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(bookingId) { viewModel.loadBooking(bookingId) }
    Scaffold(topBar = { BookingTopBar("Booking Details", onBackClick) }) { padding ->
        when (val state = uiState) {
            BookingUiState.Loading, BookingUiState.Idle -> CaterHubLoadingState("Loading booking details...")
            is BookingUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(state.message, { viewModel.loadBooking(bookingId) }) }
            is BookingUiState.DetailsLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                BookingSummaryCard(state.booking, detailed = true)
            }
            else -> Unit
        }
    }
}

@Composable
private fun BookingSummaryCard(booking: BookingResponse, detailed: Boolean = false) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.bookingReference ?: "Booking #${booking.id}", fontWeight = FontWeight.ExtraBold)
                CaterHubStatusChip(booking.status)
            }
            SummaryRow("Event", booking.eventType)
            SummaryRow("Services", booking.mealType)
            SummaryRow("Guests", "${booking.guestCount} guests")
            SummaryRow("Date & time", formatDateTime(booking.eventDateTime))
            SummaryRow("Location", booking.deliveryAddress)
            if (detailed) {
                SummaryRow("Instructions", booking.specialInstructions.orEmpty())
                SummaryRow("Created", formatDateTime(booking.createdAt))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(title = { Text(title) }, navigationIcon = {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    })
}
