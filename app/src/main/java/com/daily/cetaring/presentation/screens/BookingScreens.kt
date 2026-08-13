package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
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

    Scaffold(topBar = { BookingTopBar("Book Catering", onBackClick) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Step ${step + 1} of 8", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Card(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (step) {
                        0 -> OptionStep("What are you planning?", BookingOptions.eventTypes, draft.eventType) { selected -> viewModel.updateDraft { it.copy(eventType = selected) } }
                        1 -> OptionStep("What do you need?", BookingOptions.serviceTypes, draft.serviceType) { selected -> viewModel.updateDraft { it.copy(serviceType = selected) } }
                        2 -> GuestStep(draft) { viewModel.updateDraft { it } }
                        3 -> FoodStep(draft, viewModel)
                        4 -> DateTimeStep(draft, viewModel)
                        5 -> LocationStep(draft, viewModel)
                        6 -> RequirementsStep(draft, viewModel)
                        else -> ReviewStep(draft) { editStep -> step = editStep }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CaterHubSecondaryButton(if (step == 0) "Cancel" else "Back", onClick = { if (step == 0) onBackClick() else step-- }, modifier = Modifier.weight(1f))
                CaterHubPrimaryButton(
                    text = if (step < 7) "Continue" else "Submit Booking Request",
                    onClick = {
                        val validation = if (step < 7) viewModel.validateStep(step) else com.daily.cetaring.data.remote.dto.BookingValidator.validateForSubmit(draft)
                        if (validation is BookingValidationResult.Invalid) {
                            coroutineScope.launch { snackbar.showSnackbar(validation.message) }
                        } else if (step < 7) step++ else viewModel.submitBooking()
                    },
                    loading = uiState is BookingUiState.Loading,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

@Composable
private fun OptionStep(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { options.forEach { CaterHubCategoryChip(it, selected == it, { onSelected(it) }, Modifier.fillMaxWidth()) } }
}

@Composable
private fun GuestStep(draft: BookingDraft, update: ((BookingDraft) -> BookingDraft) -> Unit) {
    Text("How many guests?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BookingOptions.guestQuickOptions.take(3).forEach { guests -> CaterHubCategoryChip("$guests", draft.guestCount == guests, { update { it.copy(guestCount = guests) } }) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BookingOptions.guestQuickOptions.drop(3).forEach { guests -> CaterHubCategoryChip("$guests", draft.guestCount == guests, { update { it.copy(guestCount = guests) } }) }
    }
    OutlinedTextField(value = draft.guestCount?.toString().orEmpty(), onValueChange = { value -> update { it.copy(guestCount = value.filter(Char::isDigit).toIntOrNull()) } }, label = { Text("Custom guest count") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun FoodStep(draft: BookingDraft, viewModel: BookingViewModel) {
    OptionStep("What type of food service?", BookingOptions.foodTypes, draft.foodType) { selected -> viewModel.updateDraft { it.copy(foodType = selected) } }
    OutlinedTextField(value = draft.foodRequirements, onValueChange = { value -> viewModel.updateDraft { it.copy(foodRequirements = value) } }, label = { Text("Any special food requirements?") }, minLines = 3, modifier = Modifier.fillMaxWidth())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(draft: BookingDraft, viewModel: BookingViewModel) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = false)
    Text("When is your event?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    OutlinedTextField(value = draft.eventDate, onValueChange = {}, readOnly = true, label = { Text("Date") }, modifier = Modifier.fillMaxWidth().clickable { showDate = true })
    OutlinedTextField(value = draft.eventTime, onValueChange = {}, readOnly = true, label = { Text("Time") }, modifier = Modifier.fillMaxWidth().clickable { showTime = true })
    if (showDate) DatePickerDialog(
        onDismissRequest = { showDate = false },
        confirmButton = {
            TextButton(onClick = {
                dateState.selectedDateMillis?.let { millis ->
                    val selectedCalendar = Calendar.getInstance().apply { timeInMillis = millis }
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (!selectedCalendar.before(today)) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        viewModel.updateDraft { it.copy(eventDate = formatter.format(selectedCalendar.time)) }
                    }
                }
                showDate = false
            }) { Text("Done") }
        },
        dismissButton = { TextButton({ showDate = false }) { Text("Cancel") } }
    ) { DatePicker(dateState) }
    if (showTime) AlertDialog(onDismissRequest = { showTime = false }, title = { Text("Select time") }, text = { TimeInput(timeState) }, confirmButton = { TextButton(onClick = { viewModel.updateDraft { it.copy(eventTime = "%02d:%02d".format(timeState.hour, timeState.minute)) }; showTime = false }) { Text("Done") } }, dismissButton = { TextButton({ showTime = false }) { Text("Cancel") } })
}

@Composable
private fun LocationStep(draft: BookingDraft, viewModel: BookingViewModel) {
    Text("Where should we serve?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    OutlinedTextField(draft.address, { v -> viewModel.updateDraft { it.copy(address = v) } }, label = { Text("Address") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.area, { v -> viewModel.updateDraft { it.copy(area = v) } }, label = { Text("Area") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.city, { v -> viewModel.updateDraft { it.copy(city = v) } }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.pincode, { v -> viewModel.updateDraft { it.copy(pincode = v.filter(Char::isDigit).take(6)) } }, label = { Text("Pincode") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun RequirementsStep(draft: BookingDraft, viewModel: BookingViewModel) {
    Text("Additional requirements", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    OutlinedTextField(draft.specialInstructions, { v -> viewModel.updateDraft { it.copy(specialInstructions = v) } }, label = { Text("Special instructions") }, placeholder = { Text("Need vegetarian menu, buffet setup, 10 serving staff...") }, minLines = 5, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(draft.workerCount?.toString().orEmpty(), { v -> viewModel.updateDraft { it.copy(workerCount = v.filter(Char::isDigit).toIntOrNull()) } }, label = { Text("Worker count if needed") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ReviewStep(draft: BookingDraft, onEdit: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Review your request", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        IconButton(onClick = { onEdit(0) }) { Icon(Icons.Filled.Edit, "Edit") }
    }
    SummaryRow("Event", draft.eventType)
    SummaryRow("Service", draft.serviceType)
    SummaryRow("Guests", draft.guestCount?.toString().orEmpty())
    SummaryRow("Food", draft.foodType)
    SummaryRow("Date", draft.eventDate)
    SummaryRow("Time", draft.eventTime)
    SummaryRow("Location", draft.deliveryAddress())
    SummaryRow("Special requirements", listOf(draft.foodRequirements, draft.specialInstructions).filter { it.isNotBlank() }.joinToString("\n"))
}

@Composable
fun BookingSuccessScreen(viewModel: BookingViewModel, bookingId: Long, onViewBooking: (Long) -> Unit, onHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(bookingId) { if (uiState !is BookingUiState.Submitted) viewModel.loadBooking(bookingId) }
    val booking = when (val state = uiState) { is BookingUiState.Submitted -> state.booking; is BookingUiState.DetailsLoaded -> state.booking; else -> null }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(64.dp))
        Text("Booking Request Submitted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Your catering request has been sent to CaterHub.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        if (booking != null) BookingSummaryCard(booking)
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
            is BookingUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadBookings() }) }
            is BookingUiState.ListLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.bookings.isEmpty()) CaterHubEmptyState("No bookings yet", "Book catering for your next event.", actionText = "Book Catering", onActionClick = onBookCateringClick) else state.bookings.forEach { CaterHubBookingCard(it, { onBookingClick(it.id) }) }
            }
            else -> Unit
        }
    }
}

@Composable
fun BookingDetailsScreen(viewModel: BookingViewModel, bookingId: Long, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmCancel by remember { mutableStateOf(false) }
    LaunchedEffect(bookingId) { viewModel.loadBooking(bookingId) }
    Scaffold(topBar = { BookingTopBar("Booking Details", onBackClick) }) { padding ->
        when (val state = uiState) {
            BookingUiState.Loading, BookingUiState.Idle -> CaterHubLoadingState("Loading booking details...")
            is BookingUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadBooking(bookingId) }) }
            is BookingUiState.DetailsLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                BookingSummaryCard(state.booking, detailed = true)
                if (state.booking.status.uppercase() !in setOf("CANCELLED", "COMPLETED", "DELIVERED")) CaterHubSecondaryButton("Cancel Booking", { confirmCancel = true }, Modifier.fillMaxWidth())
            }
            else -> Unit
        }
    }
    if (confirmCancel) AlertDialog(onDismissRequest = { confirmCancel = false }, title = { Text("Cancel booking?") }, text = { Text("This will send a cancellation request for your booking.") }, confirmButton = { Button(onClick = { confirmCancel = false; viewModel.cancelBooking(bookingId) }) { Icon(Icons.Filled.Delete, null); Text("Cancel Booking") } }, dismissButton = { TextButton({ confirmCancel = false }) { Text("Keep Booking") } })
}

@Composable
private fun BookingSummaryCard(booking: BookingResponse, detailed: Boolean = false) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(booking.bookingReference ?: "Booking #${booking.id}", fontWeight = FontWeight.ExtraBold); CaterHubStatusChip(booking.status) }
            SummaryRow("Event", booking.eventType)
            SummaryRow("Service / Food", booking.mealType)
            SummaryRow("Guests", booking.guestCount.toString())
            SummaryRow("Date & Time", formatDateTime(booking.eventDateTime))
            SummaryRow("Location", booking.deliveryAddress)
            if (detailed) {
                SummaryRow("Special requirements", booking.specialInstructions.orEmpty())
                SummaryRow("Created", formatDateTime(booking.createdAt))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } })
}
