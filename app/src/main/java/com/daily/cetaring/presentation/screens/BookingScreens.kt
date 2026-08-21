package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import java.util.TimeZone
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ColumnScope

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)
private val SoftGold = Color(0xFFFFF3D6)
private val SoftGreen = Color(0xFFEAF4E7)

@Composable
fun BookingFlowScreen(
    viewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onSubmitted: (Long) -> Unit
) {
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
        containerColor = Cream,
        topBar = { BookingTopBar("Book your event", onBackClick) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressHeader(step)

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    0 -> EventStep(draft, viewModel)
                    1 -> CateringPlanAndLocationStep(draft, viewModel)
                    2 -> DateTimeStep(draft, viewModel)
                    else -> ReviewStep(draft) { step = it }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CaterHubSecondaryButton(
                    text = if (step == 0) "Cancel" else "Back",
                    onClick = { if (step == 0) onBackClick() else step-- },
                    modifier = Modifier.weight(1f)
                )

                CaterHubPrimaryButton(
                    text = if (step == 3) "Submit Booking" else "Continue",
                    onClick = {
                        val validation = if (step == 3) {
                            BookingValidationResult.Valid
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
                    modifier = Modifier.weight(1.25f)
                )
            }
        }
    }
}

@Composable
private fun ProgressHeader(step: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Step ${step + 1} of 4",
            color = Green,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(4) { index ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index <= step) Maroon else Border
                    )
                ) {}
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EventStep(draft: BookingDraft, viewModel: BookingViewModel) {
    Text(
        "Tell us about your event",
        color = Maroon,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold
    )
    Text(
        "Choose the occasion and expected guest count.",
        color = Muted
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        BookingOptions.eventTypes.forEach { event ->
            EventTypeCard(
                event = event,
                selected = draft.eventType == event,
                onClick = { viewModel.updateDraft { it.copy(eventType = event) } }
            )
        }
    }

    Text(
        "Guest count",
        color = TextDark,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BookingOptions.guestQuickOptions.forEach { guests ->
            CaterHubCategoryChip(
                text = "$guests guests",
                selected = draft.guestCount == guests,
                onClick = { viewModel.updateDraft { it.copy(guestCount = guests) } },
                icon = Icons.Filled.Groups
            )
        }
    }

    OutlinedTextField(
        value = if (draft.guestCount in BookingOptions.guestQuickOptions) ""
        else draft.guestCount?.toString().orEmpty(),
        onValueChange = { value ->
            viewModel.updateDraft {
                it.copy(guestCount = value.filter(Char::isDigit).toIntOrNull())
            }
        },
        label = { Text("Custom guest count") },
        supportingText = { Text("Enter a positive whole number.") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EventTypeCard(
    event: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (event) {
        "Birthday" -> Icons.Filled.Cake
        "Wedding" -> Icons.Filled.Favorite
        "Engagement" -> Icons.Filled.Favorite
        "Housewarming" -> Icons.Filled.Home
        "Corporate" -> Icons.Filled.Groups
        "Baby Shower" -> Icons.Filled.Favorite
        "Naming Ceremony" -> Icons.Filled.Cake
        "Festival" -> Icons.Filled.Star
        else -> Icons.Filled.Event
    }

    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Maroon else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (selected) Maroon else Border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color.White else Maroon,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(7.dp))
            Text(
                event,
                color = if (selected) Color.White else TextDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CateringPlanAndLocationStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    Text(
        "Choose your catering plan",
        color = Maroon,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold
    )
    Text(
        "CaterHub provides full catering. Choose a package or customize your menu.",
        color = Muted
    )

    PlanCard(
        "BASIC",
        "₹499 / person",
        "Biryani • 2 Curries • Rice • Raita • Sweet",
        Green,
        draft.cateringPlan == "Basic"
    ) {
        viewModel.updateDraft { it.copy(cateringPlan = "Basic") }
    }

    PlanCard(
        "CLASSIC",
        "₹699 / person",
        "2 Starters • Biryani • 3 Curries • Dal • Raita • Sweet",
        Maroon,
        draft.cateringPlan == "Classic"
    ) {
        viewModel.updateDraft { it.copy(cateringPlan = "Classic") }
    }

    PlanCard(
        "PREMIUM",
        "₹999 / person",
        "2 Starters • Biryani • 4 Curries • Dal • Raita • 2 Desserts • Beverages",
        Gold,
        draft.cateringPlan == "Premium"
    ) {
        viewModel.updateDraft { it.copy(cateringPlan = "Premium") }
    }

    PlanCard(
        "CUSTOMIZED",
        "Build your own menu",
        "Choose starters, biryani, curries, rice, sweets, beverages and more.",
        Green,
        draft.cateringPlan == "Customized"
    ) {
        viewModel.updateDraft { it.copy(cateringPlan = "Customized") }
    }

    if (draft.cateringPlan == "Customized") {
        OutlinedTextField(
            value = draft.foodRequirements,
            onValueChange = {
                viewModel.updateDraft { current -> current.copy(foodRequirements = it) }
            },
            label = { Text("Your menu requirements") },
            placeholder = { Text("Example: Chicken Biryani, Paneer Curry, Gulab Jamun...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }

    SectionLabel("Event location", Icons.Filled.LocationOn)

    OutlinedTextField(
        value = draft.address,
        onValueChange = { value -> viewModel.updateDraft { it.copy(address = value) } },
        label = { Text("Event address", color = TextDark) },
        placeholder = { Text("Enter full event address", color = Muted) },
        minLines = 2,
        singleLine = false,
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark)
    )
    OutlinedTextField(
        value = draft.area,
        onValueChange = { value -> viewModel.updateDraft { it.copy(area = value) } },
        label = { Text("Area / locality", color = TextDark) },
        placeholder = { Text("Enter area / locality", color = Muted) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark)
    )
    OutlinedTextField(
        value = draft.city,
        onValueChange = { value -> viewModel.updateDraft { it.copy(city = value) } },
        label = { Text("City", color = TextDark) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark)
    )
    OutlinedTextField(
        value = draft.landmark,
        onValueChange = { value -> viewModel.updateDraft { it.copy(landmark = value) } },
        label = { Text("Landmark (optional)", color = TextDark) },
        placeholder = { Text("Optional landmark", color = Muted) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark)
    )
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    description: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accent.copy(alpha = 0.10f) else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else Border
        )
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = accent, fontWeight = FontWeight.ExtraBold)
                if (selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = accent
                    )
                }
            }
            Text(
                subtitle,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(description, color = Muted, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(7.dp))
        Text(text, color = TextDark, fontWeight = FontWeight.ExtraBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

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
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= today

            override fun isSelectableYear(year: Int): Boolean =
                year >= Calendar.getInstance().get(Calendar.YEAR)
        }
    )
    val timeState = rememberTimePickerState(is24Hour = false)

    Text(
        "When is your event?",
        color = Maroon,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold
    )
    Text(
        "Choose the event date and start time.",
        color = Muted
    )

    PickerField("Date", draft.eventDate.ifBlank { "Select date" }) {
        showDate = true
    }

    PickerField("Time", draft.eventTime.ifBlank { "Select time" }) {
        showTime = true
    }

    OutlinedTextField(
        value = draft.foodRequirements,
        onValueChange = { value ->
            viewModel.updateDraft { it.copy(foodRequirements = value) }
        },
        label = { Text("Food requirements (optional)") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = draft.specialInstructions,
        onValueChange = { value ->
            viewModel.updateDraft { it.copy(specialInstructions = value) }
        },
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
                        val formatter = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        viewModel.updateDraft {
                            it.copy(eventDate = formatter.format(millis))
                        }
                    }
                    showDate = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Select event time") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDraft {
                        it.copy(
                            eventTime = "%02d:%02d".format(
                                timeState.hour,
                                timeState.minute
                            )
                        )
                    }
                    showTime = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = Green, fontWeight = FontWeight.Bold)
            Text(
                value,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReviewStep(draft: BookingDraft, onEdit: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Review your booking",
            color = Maroon,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(onClick = { onEdit(0) }) {
            Icon(Icons.Filled.Edit, "Edit booking", tint = Maroon)
        }
    }

    ReviewCard {
        SummaryRow("Event", draft.eventType)
        SummaryRow("Guests", "${draft.guestCount ?: 0} guests")
        SummaryRow("Catering", "Full Catering • ${draft.cateringPlan}")
        SummaryRow("Date", draft.eventDate)
        SummaryRow("Time", draft.eventTime)
        SummaryRow("Location", draft.deliveryAddress())
        SummaryRow(
            "Food requirements",
            draft.foodRequirements.ifBlank { "Standard plan menu" }
        )
    }
}

@Composable
private fun ReviewCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content
        )
    }
}

@Composable
fun BookingSuccessScreen(
    viewModel: BookingViewModel,
    bookingId: Long,
    onViewBooking: (Long) -> Unit,
    onHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) {
        if (uiState !is BookingUiState.Submitted) {
            viewModel.loadBooking(bookingId)
        }
    }

    val submitted = uiState as? BookingUiState.Submitted
    val booking = submitted?.booking
        ?: (uiState as? BookingUiState.DetailsLoaded)?.booking

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(52.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Booking request submitted",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Maroon
        )
        Text(
            "Your request has been sent to CaterHub.",
            color = Muted
        )

        Spacer(Modifier.height(20.dp))

        if (booking != null) {
            BookingSummaryCard(booking)
        }

        Spacer(Modifier.height(20.dp))

        CaterHubPrimaryButton(
            "View Booking",
            { onViewBooking(bookingId) },
            Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        CaterHubSecondaryButton(
            "Back to Home",
            onHome,
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BookingHistoryScreen(
    viewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onBookCateringClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadBookings() }

    Scaffold(
        containerColor = Cream,
        topBar = { BookingTopBar("My Bookings", onBackClick) }
    ) { padding ->
        when (val state = uiState) {
            BookingUiState.Loading, BookingUiState.Idle ->
                CaterHubLoadingState("Loading your bookings...")

            is BookingUiState.Error ->
                Column(Modifier.padding(padding).padding(20.dp)) {
                    CaterHubErrorState(
                        state.message,
                        { viewModel.loadBookings() }
                    )
                }

            is BookingUiState.ListLoaded ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (state.bookings.isEmpty()) {
                        CaterHubEmptyState(
                            "No bookings yet",
                            "Book catering for your next event.",
                            actionText = "Book Catering",
                            onActionClick = onBookCateringClick
                        )
                    } else {
                        state.bookings.forEach {
                            CaterHubBookingCard(
                                it,
                                { onBookingClick(it.id) }
                            )
                        }
                    }
                }

            else -> Unit
        }
    }
}

@Composable
fun BookingDetailsScreen(
    viewModel: BookingViewModel,
    bookingId: Long,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(bookingId) { viewModel.loadBooking(bookingId) }

    Scaffold(
        containerColor = Cream,
        topBar = { BookingTopBar("Booking Details", onBackClick) }
    ) { padding ->
        when (val state = uiState) {
            BookingUiState.Loading, BookingUiState.Idle ->
                CaterHubLoadingState("Loading booking details...")

            is BookingUiState.Error ->
                Column(Modifier.padding(padding).padding(20.dp)) {
                    CaterHubErrorState(
                        state.message,
                        { viewModel.loadBooking(bookingId) }
                    )
                }

            is BookingUiState.DetailsLoaded ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    BookingSummaryCard(state.booking, detailed = true)
                }

            else -> Unit
        }
    }
}

@Composable
private fun BookingSummaryCard(
    booking: BookingResponse,
    detailed: Boolean = false
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    booking.bookingReference ?: "Booking #${booking.id}",
                    color = Maroon,
                    fontWeight = FontWeight.ExtraBold
                )
                CaterHubStatusChip(booking.status)
            }

            SummaryRow("Event", booking.eventType)
            SummaryRow("Services", booking.mealType)
            SummaryRow("Guests", "${booking.guestCount} guests")
            SummaryRow("Date & time", formatDateTime(booking.eventDateTime))
            SummaryRow("Location", booking.deliveryAddress)

            if (detailed) {
                SummaryRow(
                    "Instructions",
                    booking.specialInstructions.orEmpty()
                )
                SummaryRow(
                    "Created",
                    formatDateTime(booking.createdAt)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                title,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = Maroon
                )
            }
        }
    )
}
