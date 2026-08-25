@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.daily.cetaring.domain.catalog.ServiceCatalog
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
private val DateReviewFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
private val TimeDisplayFormatter = DateTimeFormatter.ofPattern("h:mm a")

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

private fun categoryFor(type: String): ServiceCategory {
    val category = ServiceCatalog.category(type) ?: ServiceCatalog.category("event-support")!!
    val color = when (category.id) {
        "decoration", "beauty", "event-support", "other-event-services" -> Green
        "religious-ceremony" -> Gold
        else -> Maroon
    }
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

private fun categoryIcon(categoryId: String) = when (categoryId) {
    "catering-food" -> Icons.Filled.Restaurant
    "decoration" -> Icons.Filled.Celebration
    "entertainment" -> Icons.Filled.Star
    "beauty" -> Icons.Filled.Star
    "photography-video" -> Icons.Filled.Cake
    "religious-ceremony" -> Icons.Filled.Cake
    "event-support" -> Icons.Filled.Groups
    "rentals" -> Icons.Filled.Celebration
    "transport-logistics" -> Icons.Filled.LocationOn
    else -> Icons.Filled.Star
}

@Composable
fun ServiceRequestScreen(
    serviceType: String,
    workerRepository: WorkerRepository,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit = {}
) {
    val category = remember(serviceType) { categoryFor(serviceType) }
    val items = category.items
    val selected = remember(serviceType) { mutableStateMapOf<String, Int>() }
    var step by remember { mutableIntStateOf(0) }
    var eventType by remember { mutableStateOf("Birthday") }
    var eventDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fixedTotal = items.sumOf { item -> (selected[item.id] ?: 0) * item.price }
    val hasSelection = selected.values.any { it > 0 }
    val hasQuoteServices = items.any { (selected[it.id] ?: 0) > 0 && it.quoteOnly }
    val detailsComplete = hasSelection &&
        eventDate.isNotBlank() &&
        startTime.isNotBlank() &&
        endTime.isNotBlank() &&
        location.isNotBlank() &&
        area.isNotBlank() &&
        isValidTimeRange(startTime, endTime) &&
        isFutureDateTime(eventDate, startTime)

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
                        EventTypeField(eventType) { eventType = it }

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

                        SectionLabel("Event location")
                        OutlinedTextField(location, { location = it }, label = { Text("Event address") }, minLines = 2, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        OutlinedTextField(area, { area = it }, label = { Text("Area / locality") }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        OutlinedTextField(notes, { notes = it }, label = { Text("Additional instructions (optional)") }, minLines = 3, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                    }
                    else -> {
                        val selectedItems = items.filter { (selected[it.id] ?: 0) > 0 }
                        ReviewRequestCard(
                            eventType = eventType,
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
                            Text("Quote-based services will be confirmed after request review.", color = Muted)
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { if (step == 0) onBackClick() else step-- }, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    Text(if (step == 0) "Cancel" else "Back", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (step < 2) {
                            error = when {
                                !hasSelection -> "Please select at least one service."
                                step == 1 && eventDate.isBlank() -> "Please choose the event date."
                                step == 1 && startTime.isBlank() -> "Please choose the start time."
                                step == 1 && endTime.isBlank() -> "Please choose the end time."
                                step == 1 && location.isBlank() -> "Please enter event address."
                                step == 1 && area.isBlank() -> "Please enter area/locality."
                                step == 1 && !isValidTimeRange(startTime, endTime) -> "End time must be after start time."
                                step == 1 && !isFutureDateTime(eventDate, startTime) -> "Please select a future event date and time."
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
                                    val serviceItems = selectedItems.filter { it.workerType == null }

                                    staffingItems.forEach { item ->
                                        val qty = selected[item.id] ?: 0
                                        workerRepository.createStaffingRequest(
                                            CreateStaffingRequest(
                                                eventType, item.workerType!!, eventDate, startTime, endTime,
                                                location, area, qty, BigDecimal(item.price.coerceAtLeast(1)), notes.ifBlank { null }
                                            )
                                        )
                                    }

                                    if (serviceItems.isNotEmpty() || staffingItems.isEmpty()) {
                                        val details = selectedItems.joinToString("; ") { item ->
                                            val qty = selected[item.id] ?: 0
                                            if (item.quoteOnly) "${item.name}: selected x$qty (quote)" else "${item.name}: $qty x ₹${item.price}"
                                        }
                                        val fullDetails = "End time: $endTime; $details${if (notes.isBlank()) "" else "; Notes: $notes"}"
                                        workerRepository.createServiceRequest(
                                            ServiceRequestRequest(
                                                category.serviceType, eventType, eventDate, startTime,
                                                location, area, fullDetails, BigDecimal(fixedTotal)
                                            )
                                        )
                                    }
                                    onSubmitted()
                                } catch (e: Exception) {
                                    error = e.message ?: "Unable to submit request. Please try again."
                                } finally {
                                    submitting = false
                                }
                            }
                        }
                    },
                    enabled = !submitting && (step != 0 || hasSelection) && (step != 1 || detailsComplete),
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
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
                            timeZone = TimeZone.getDefault()
                        }
                        eventDate = formatter.format(millis)
                    }
                    showDatePicker = false
                }) { Text("Confirm", color = category.color) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initial = startTime.toLocalTimeOrNull() ?: LocalTime.now().withSecond(0).withNano(0),
            onDismiss = { showStartPicker = false },
            onConfirm = { startTime = it.format(TimeFormatter); showStartPicker = false },
            accent = category.color
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            initial = endTime.toLocalTimeOrNull() ?: LocalTime.now().plusHours(1).withSecond(0).withNano(0),
            onDismiss = { showEndPicker = false },
            onConfirm = { endTime = it.format(TimeFormatter); showEndPicker = false },
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
private fun TimePickerDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit, accent: Color) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose time") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK", color = accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun isValidTimeRange(start: String, end: String): Boolean {
    val s = start.toLocalTimeOrNull() ?: return false
    val e = end.toLocalTimeOrNull() ?: return false
    return e.isAfter(s)
}

private fun isFutureDateTime(date: String, time: String): Boolean {
    val selectedDate = date.toLocalDateOrNull() ?: return false
    val selectedTime = time.toLocalTimeOrNull() ?: return false
    return LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())
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
                Icon(categoryIcon(category.id), null, tint = category.color, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(category.title.removePrefix("Book "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text("Choose the services you need", color = Muted)
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

@Composable
private fun EventTypeField(value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "Wedding", "Reception", "Engagement", "Birthday", "Anniversary", "Housewarming",
        "Baby Shower", "Naming Ceremony", "Corporate Event", "School / College Event",
        "Festival", "Religious Ceremony", "Farewell", "Get Together", "Other"
    )
    Box {
        OutlinedTextField(value, {}, label = { Text("Event type") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { expanded = true }, textStyle = LocalTextStyle.current.copy(color = TextDark))
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onChange(it); expanded = false }) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocationOn, null, tint = Gold, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, fontWeight = FontWeight.ExtraBold, color = TextDark)
    }
}
