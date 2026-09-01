@file:OptIn(ExperimentalMaterial3Api::class)

package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.CreateStaffingRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestRequest
import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.categoryUiMeta
import com.daily.cetaring.presentation.components.ReviewLineItem
import com.daily.cetaring.presentation.components.ReviewRequestCard
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.time.format.DateTimeFormatter

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)
private val DateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateDisplayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val DateReviewFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val TimeDisplayFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val TimePickerDisplayFormatter = DateTimeFormatter.ofPattern("hh : mm a")

private data class ServiceItem(
    val id: String,
    val name: String,
    val price: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val workerType: WorkerType? = null,
    val quoteOnly: Boolean = false
)

private data class ServiceCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val serviceType: String,
    val items: List<ServiceItem>,
    val color: Color
)

private data class EventDetailsState(
    val eventType: String?,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val eventAddress: String,
    val area: String
)

private data class SubmittedServiceRequestSummary(
    val categoryTitle: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String
)

private fun categoryFor(type: String): ServiceCategory {
    val category = ServiceCatalog.category(type) ?: ServiceCatalog.category("event-support")!!
    val color = categoryUiMeta(category).accent
    val items = ServiceCatalog.rolesForCategory(category.id).map { role ->
        ServiceItem(
            id = role.id,
            name = role.title,
            price = role.defaultUnitPrice ?: 0,
            icon = roleIcon(category.id, role.id),
            workerType = role.workerType,
            quoteOnly = role.quoteOnly
        )
    }
    return ServiceCategory(
        id = category.id,
        title = "Book ${category.title}",
        subtitle = category.subtitle,
        serviceType = category.serviceType,
        items = items,
        color = color
    )
}

private fun roleIcon(categoryId: String, roleId: String) = when {
    categoryId == "catering-food" -> Icons.Filled.Restaurant
    categoryId == "decoration" && roleId.contains("lighting") -> Icons.Filled.Lightbulb
    categoryId == "religious-ceremony" -> Icons.Filled.Cake
    categoryId == "transport-logistics" -> Icons.Filled.LocationOn
    roleId.contains("clean") -> Icons.Filled.CleaningServices
    roleId.contains("staff") || roleId.contains("worker") || roleId.contains("helper") -> Icons.Filled.Groups
    else -> Icons.Filled.Star
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = Border)
}

@Composable
private fun CaterHubActionButtons(
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(secondaryText, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(primaryText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ServiceRequestScreen(
    serviceType: String,
    workerRepository: WorkerRepository,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit = {},
    onViewMyBookings: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val category = remember(serviceType) { categoryFor(serviceType) }
    val items = category.items
    val selected = remember(serviceType) { mutableStateMapOf<String, Int>() }
    var step by rememberSaveable(serviceType) { mutableIntStateOf(0) }
    var eventType by rememberSaveable(serviceType) { mutableStateOf<String?>(null) }
    var eventDate by rememberSaveable(serviceType) { mutableStateOf("") }
    var startTime by rememberSaveable(serviceType) { mutableStateOf("") }
    var endTime by rememberSaveable(serviceType) { mutableStateOf("") }
    var location by rememberSaveable(serviceType) { mutableStateOf("") }
    var area by rememberSaveable(serviceType) { mutableStateOf("") }
    var notes by rememberSaveable(serviceType) { mutableStateOf("") }
    var submitting by rememberSaveable(serviceType) { mutableStateOf(false) }
    var error by rememberSaveable(serviceType) { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable(serviceType) { mutableStateOf(false) }
    var showStartPicker by rememberSaveable(serviceType) { mutableStateOf(false) }
    var showEndPicker by rememberSaveable(serviceType) { mutableStateOf(false) }
    var submittedSummary by rememberSaveable(serviceType) { mutableStateOf<SubmittedServiceRequestSummary?>(null) }
    val scope = rememberCoroutineScope()

    val fixedTotal = items.sumOf { item -> (selected[item.id] ?: 0) * item.price }
    val hasSelection = selected.values.any { it > 0 }
    val hasQuoteServices = items.any { (selected[it.id] ?: 0) > 0 && it.quoteOnly }
    val eventDetails = EventDetailsState(
        eventType = eventType,
        eventDate = eventDate,
        startTime = startTime,
        endTime = endTime,
        eventAddress = location,
        area = area
    )
    val eventDetailsValid = isEventDetailsValid(eventDetails)

    if (submittedSummary != null) {
        val summary = checkNotNull(submittedSummary)
        Scaffold(
            containerColor = Cream,
            topBar = {
                TopAppBar(
                    title = { Text("Request submitted", color = TextDark, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackToHome) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Home", tint = category.color)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Green)
                            Text("Request submitted", fontWeight = FontWeight.ExtraBold, color = TextDark, style = MaterialTheme.typography.titleLarge)
                        }
                        Text("Your event request has been received by CaterHub.", color = Muted)
                        DividerLine()
                        Text(summary.eventType, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        Text(summary.categoryTitle, color = category.color, fontWeight = FontWeight.Bold)
                        Text("${displayDateForReview(summary.eventDate)}\n${displayTime(summary.startTime)} – ${displayTime(summary.endTime)}", color = TextDark)
                        Text(summary.location, color = Muted)
                        DividerLine()
                        Text("Status", color = Muted, style = MaterialTheme.typography.labelMedium)
                        Text("Pending confirmation", fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
                CaterHubActionButtons(
                    primaryText = "View My Bookings",
                    onPrimary = onViewMyBookings,
                    secondaryText = "Back to Home",
                    onSecondary = onBackToHome,
                    accent = category.color
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text(category.title, color = TextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = category.color) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Step ${step + 1} of 3", color = category.color, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { i ->
                    Card(Modifier.weight(1f).height(5.dp), shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(containerColor = if (i <= step) category.color else Border)) {}
                }
            }
            Spacer(Modifier.height(14.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        CategoryHeader(category = category)
                        items.forEach { item ->
                            val count = selected[item.id] ?: 0
                            ServiceItemCard(
                                item = item,
                                count = count,
                                onMinus = { selected[item.id] = (count - 1).coerceAtLeast(0) },
                                onPlus = { selected[item.id] = (count + 1).coerceAtMost(5000) },
                                onCount = { selected[item.id] = it.coerceIn(0, 5000) }
                            )
                        }
                        if (hasSelection) TotalCard(fixedTotal, category.color, hasQuoteOnly = items.any { (selected[it.id] ?: 0) > 0 && it.quoteOnly })
                    }
                    1 -> {
                        Text("Event details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EventTypeField(eventType, category.color) {
                                    eventType = it
                                    error = null
                                }

                                PickerField(
                                    label = "Date",
                                    value = eventDate.takeIf { it.isNotBlank() }?.let(::displayDate) ?: "Select date",
                                    onClick = { showDatePicker = true }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PickerField(
                                        label = "Start time",
                                        value = startTime.takeIf { it.isNotBlank() }?.let(::displayTime) ?: "Select time",
                                        onClick = { showStartPicker = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PickerField(
                                        label = "End time",
                                        value = endTime.takeIf { it.isNotBlank() }?.let(::displayTime) ?: "Select time",
                                        onClick = { showEndPicker = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (startTime.isNotBlank() && endTime.isNotBlank() && !isValidTimeRange(startTime, endTime)) {
                                    Text("End time must be after start time", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }

                                SectionLabel()
                                OutlinedTextField(
                                    location,
                                    { location = it; error = null },
                                    label = { Text("Event address") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextDark)
                                )
                                OutlinedTextField(
                                    area,
                                    { area = it; error = null },
                                    label = { Text("Area / locality") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextDark)
                                )
                                OutlinedTextField(
                                    notes,
                                    { notes = it },
                                    label = { Text("Additional instructions (optional)") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextDark)
                                )
                            }
                        }
                    }
                    else -> {
                        val selectedItems = items.filter { (selected[it.id] ?: 0) > 0 }
                        ReviewRequestCard(
                            eventType = EventTypeCatalog.displayNameForBackendValue(eventType),
                            eventDate = eventDate.takeIf { it.isNotBlank() }?.let(::displayDateForReview).orEmpty(),
                            timeRange = "${startTime.takeIf { it.isNotBlank() }?.let(::displayTime).orEmpty()} – ${endTime.takeIf { it.isNotBlank() }?.let(::displayTime).orEmpty()}",
                            location = "$area, $location",
                            services = selectedItems.map { item ->
                                val qty = selected[item.id] ?: 0
                                ReviewLineItem(
                                    title = "${qty} × ${item.name}",
                                    subtitle = if (item.quoteOnly) "To be quoted" else "₹${item.price} per ${if (item.workerType == null) "item" else "person"}",
                                    amountText = if (item.quoteOnly) null else "₹${qty * item.price}"
                                )
                            },
                            totalLabel = if (hasQuoteServices) {
                                if (fixedTotal > 0) "₹$fixedTotal + To be quoted" else "To be quoted"
                            } else {
                                "₹$fixedTotal"
                            }
                        )
                        if (hasQuoteServices) {
                            Text("Price will be confirmed by CaterHub.", color = Muted)
                        }
                    }
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { if (step == 0) onBackClick() else step-- }, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    Text(if (step == 0) "Cancel" else "Back", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (step < 2) {
                            error = when {
                                step == 0 && !hasSelection -> "Please select at least one service."
                                step == 1 -> eventDetailsValidationMessage(eventDetails)
                                else -> null
                            }
                            if (error == null) step++
                        } else {
                            scope.launch {
                                submitting = true
                                error = null
                                try {
                                    val selectedItems = items.filter { (selected[it.id] ?: 0) > 0 }
                                    val staffingItems = selectedItems.filter { it.workerType != null }
                                    val selectedNames = selectedItems.map { it.name }

                                    staffingItems.forEach { item ->
                                        val qty = selected[item.id] ?: 0
                                        workerRepository.createStaffingRequest(
                                            CreateStaffingRequest(
                                                requireNotNull(eventType), item.workerType!!, eventDate, startTime, endTime,
                                                location, area, qty, BigDecimal(item.price.coerceAtLeast(1)), notes.ifBlank { null }
                                            )
                                        )
                                    }

                                    val details = selectedItems.joinToString("; ") { item ->
                                        val qty = selected[item.id] ?: 0
                                        if (item.quoteOnly) "${item.name}: selected x$qty (quote)" else "${item.name}: $qty x ₹${item.price}"
                                    }
                                    workerRepository.createServiceRequest(
                                        ServiceRequestRequest(
                                            serviceType = category.serviceType,
                                            eventType = requireNotNull(eventType),
                                            eventDate = eventDate,
                                            startTime = startTime,
                                            endTime = endTime,
                                            location = location,
                                            area = area,
                                            selectedServices = selectedNames,
                                            instructions = notes.ifBlank { null },
                                            details = "Services: $details",
                                            quoteBased = hasQuoteServices,
                                            totalAmount = BigDecimal(fixedTotal)
                                        )
                                    )
                                    submittedSummary = SubmittedServiceRequestSummary(
                                        categoryTitle = category.title.removePrefix("Book "),
                                        eventType = requireNotNull(eventType),
                                        eventDate = eventDate,
                                        startTime = startTime,
                                        endTime = endTime,
                                        location = "$area, $location"
                                    )
                                    onSubmitted()
                                } catch (e: Exception) {
                                    error = e.message ?: "Unable to submit request. Please try again."
                                } finally {
                                    submitting = false
                                }
                            }
                        }
                    },
                    enabled = !submitting && (step != 0 || hasSelection) && (step != 1 || eventDetailsValid),
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = category.color)
                ) {
                    Text(if (submitting) "Submitting…" else if (step == 2) "Submit Request" else "Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        val today = remember {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = eventDate.toLocalDateOrNull()
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= today
                override fun isSelectableYear(year: Int): Boolean = year >= Calendar.getInstance().get(Calendar.YEAR)
            }
        )
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            containerColor = Color.White,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Select date", color = TextDark, fontWeight = FontWeight.ExtraBold)
                    Text("Choose your event date", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            },
            text = {
                DatePicker(state = state)
            },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
                            timeZone = TimeZone.getDefault()
                        }
                        eventDate = formatter.format(millis)
                        error = null
                    }
                    showDatePicker = false
                }) { Text("Confirm", color = category.color) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        )
    }

    if (showStartPicker) {
        CaterHubTimePickerDialog(
            title = "Start time",
            subtitle = "Select start time",
            initial = startTime.toLocalTimeOrNull() ?: LocalTime.now().withSecond(0).withNano(0),
            onDismiss = { showStartPicker = false },
            onConfirm = { startTime = it.format(TimeFormatter); showStartPicker = false; error = null },
            accent = category.color
        )
    }

    if (showEndPicker) {
        CaterHubTimePickerDialog(
            title = "End time",
            subtitle = "Select end time",
            initial = endTime.toLocalTimeOrNull() ?: LocalTime.now().plusHours(1).withSecond(0).withNano(0),
            onDismiss = { showEndPicker = false },
            onConfirm = { endTime = it.format(TimeFormatter); showEndPicker = false; error = null },
            accent = category.color
        )
    }
}


@Composable
private fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Green, fontWeight = FontWeight.Bold)
            Text(value, color = TextDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CaterHubTimePickerDialog(
    title: String,
    subtitle: String,
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    accent: Color
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    val selectedTime = LocalTime.of(state.hour, state.minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    selectedTime.format(TimePickerDisplayFormatter),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                TimeInput(state = state)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedTime) }) { Text("Confirm", color = accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun isValidTimeRange(start: String, end: String): Boolean {
    val s = start.toLocalTimeOrNull() ?: return false
    val e = end.toLocalTimeOrNull() ?: return false
    return e.isAfter(s)
}

private fun isEventDetailsValid(details: EventDetailsState): Boolean =
    eventDetailsValidationMessage(details) == null

private fun eventDetailsValidationMessage(details: EventDetailsState): String? = when {
    details.eventType.isNullOrBlank() -> "Please select an event type."
    details.eventDate.isBlank() -> "Please select a date."
    details.startTime.isBlank() -> "Please select a start time."
    details.endTime.isBlank() -> "Please select an end time."
    !isValidTimeRange(details.startTime, details.endTime) -> "End time must be after start time"
    details.eventAddress.trim().isEmpty() -> "Please enter the event address."
    details.area.trim().isEmpty() -> "Please enter the area/locality."
    else -> null
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this, DateFormatter) }.getOrNull()
private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this, TimeFormatter) }.getOrNull()
private fun displayDate(value: String): String =
    value.toLocalDateOrNull()?.format(DateDisplayFormatter) ?: value
private fun displayDateForReview(value: String): String =
    value.toLocalDateOrNull()?.format(DateReviewFormatter) ?: value
private fun displayTime(value: String): String =
    value.toLocalTimeOrNull()?.format(TimeDisplayFormatter) ?: value

@Composable
private fun CategoryHeader(category: ServiceCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = category.color.copy(alpha = 0.12f))
            ) {
                val visual = categoryUiMeta(ServiceCatalog.category(category.id) ?: ServiceCatalog.category("other-event-services")!!)
                Icon(visual.icon, null, tint = visual.accent, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(category.title.removePrefix("Book "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text(category.subtitle, color = Muted)
            }
        }
    }
}

@Composable
private fun ServiceItemCard(item: ServiceItem, count: Int, onMinus: () -> Unit, onPlus: () -> Unit, onCount: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = if (item.quoteOnly) Green.copy(.10f) else Maroon.copy(.08f))) {
                    Icon(item.icon, null, tint = if (item.quoteOnly) Green else Maroon, modifier = Modifier.padding(12.dp).size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Text(if (item.quoteOnly) "Price: To be quoted" else "₹${item.price} / ${if (item.workerType == null) "item" else "person"}", color = if (item.quoteOnly) Green else Maroon, fontWeight = FontWeight.Bold)
                }
                if (item.quoteOnly) {
                    OutlinedButton(onClick = { if (count == 0) onPlus() else onMinus() }) {
                        if (count > 0) Icon(Icons.Filled.Check, "Selected") else Text("Select")
                    }
                } else {
                    OutlinedButton(onClick = onMinus, enabled = count > 0) { Text("−") }
                    Text("$count", Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.ExtraBold, color = TextDark)
                    OutlinedButton(onClick = onPlus) { Icon(Icons.Filled.Add, "Add") }
                }
            }
            if (!item.quoteOnly && (item.name == "Chairs" || item.name == "Tables")) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    count.toString(),
                    { onCount(it.filter(Char::isDigit).toIntOrNull() ?: 0) },
                    label = { Text("Custom quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextDark)
                )
            }
            if (count > 0 && !item.quoteOnly) Text("Selected total: ₹${count * item.price}", Modifier.padding(top = 7.dp), color = Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TotalCard(total: Int, color: Color, hasQuoteOnly: Boolean) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(.10f))) {
        Row(Modifier.padding(17.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (hasQuoteOnly) "Current fixed total" else "Current total", fontWeight = FontWeight.Bold, color = TextDark)
            Text(if (hasQuoteOnly) "₹$total + quote" else "₹$total", fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTypeField(
    value: String?,
    accent: Color,
    onChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    PickerField(
        label = "Event type",
        value = value?.let { EventTypeCatalog.displayNameForBackendValue(it) } ?: "Select event type",
        onClick = { showDialog = true }
    )
    if (showDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose event type", style = MaterialTheme.typography.titleLarge, color = TextDark, fontWeight = FontWeight.ExtraBold)
                Text("Select the occasion for your booking", color = Muted)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    EventTypeCatalog.eventTypes.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onChange(option.backendValue)
                                    showDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == option.backendValue,
                                onClick = {
                                    onChange(option.backendValue)
                                    showDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = accent)
                            )
                            Text(option.displayName, color = TextDark)
                        }
                    }
                }
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocationOn, null, tint = Gold, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(7.dp))
        Text("Event location", fontWeight = FontWeight.ExtraBold, color = TextDark)
    }
}
