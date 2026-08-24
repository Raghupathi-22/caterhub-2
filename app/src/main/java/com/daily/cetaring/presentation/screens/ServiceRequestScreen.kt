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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Restaurant
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
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalTime

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)

private data class ServiceItem(val name: String, val price: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val workerType: WorkerType? = null, val quoteOnly: Boolean = false)

@Composable
fun ServiceRequestScreen(
    serviceType: String,
    workerRepository: WorkerRepository,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit = {}
) {
    val staffMode = serviceType == "staff"
    val items = if (staffMode) listOf(
        ServiceItem("Chef", 2000, Icons.Filled.Restaurant, WorkerType.CHEF),
        ServiceItem("Catering Boys", 600, Icons.Filled.Groups, WorkerType.SERVING_BOY),
        ServiceItem("Catering Girls", 1000, Icons.Filled.Groups, WorkerType.SERVING_GIRL),
        ServiceItem("Kitchen Helpers", 600, Icons.Filled.Restaurant, WorkerType.KITCHEN_HELPER),
        ServiceItem("Cleaning Staff", 800, Icons.Filled.CleaningServices, WorkerType.CLEANER)
    ) else listOf(
        ServiceItem("Chairs", 50, Icons.Filled.Groups),
        ServiceItem("Tables", 60, Icons.Filled.Restaurant),
        ServiceItem("Stage & Decoration", 0, Icons.Filled.Cake, quoteOnly = true),
        ServiceItem("Lighting", 0, Icons.Filled.Lightbulb, quoteOnly = true)
    )

    val selected = remember { mutableStateMapOf<String, Int>() }
    var step by remember { mutableIntStateOf(0) }
    var eventType by remember { mutableStateOf("Birthday") }
    var eventDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("11:00") }
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fixedTotal = items.sumOf { item -> (selected[item.name] ?: 0) * item.price }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text(if (staffMode) "Book Catering Staff" else "Decorations & Equipment", color = TextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Maroon) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Step ${step + 1} of 3", color = Green, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { i -> Card(Modifier.weight(1f).height(5.dp), shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(if (i <= step) Maroon else Border)) {} }
            }
            Spacer(Modifier.height(14.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        Text(if (staffMode) "Choose the people you need" else "Choose equipment & decorations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        Text(if (staffMode) "Prices are shown per person. Select the quantity you need." else "Chairs and tables have fixed prices. Stage & Decoration and Lighting are quoted by CaterHub.", color = Muted)
                        items.forEach { item ->
                            val count = selected[item.name] ?: 0
                            ServiceItemCard(item, count, onMinus = { selected[item.name] = (count - 1).coerceAtLeast(0) }, onPlus = { selected[item.name] = count + 1 }, onCount = { selected[item.name] = it.coerceIn(0, 5000) })
                        }
                        if (fixedTotal > 0 || selected.values.any { it > 0 }) TotalCard(fixedTotal, if (staffMode) Maroon else Green)
                    }
                    1 -> {
                        Text("Event details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        EventTypeField(eventType) { eventType = it }
                        OutlinedTextField(eventDate, { eventDate = it }, label = { Text("Event date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(startTime, { startTime = it }, label = { Text("Start time") }, singleLine = true, modifier = Modifier.weight(1f), textStyle = LocalTextStyle.current.copy(color = TextDark))
                            if (staffMode) OutlinedTextField(endTime, { endTime = it }, label = { Text("End time") }, singleLine = true, modifier = Modifier.weight(1f), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        }
                        SectionLabel("Event location")
                        OutlinedTextField(location, { location = it }, label = { Text("Event address") }, minLines = 2, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        OutlinedTextField(area, { area = it }, label = { Text("Area / locality") }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                        OutlinedTextField(notes, { notes = it }, label = { Text("Additional instructions (optional)") }, minLines = 3, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
                    }
                    else -> {
                        Text("Review your request", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        ReviewBox(eventType, eventDate, startTime, endTime, location, area)
                        items.filter { (selected[it.name] ?: 0) > 0 }.forEach { item ->
                            val qty = selected[item.name] ?: 0
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(if (item.quoteOnly) "$qty selected • Price: To be quoted" else "$qty × ₹${item.price}", color = Muted)
                                }
                                if (!item.quoteOnly) Text("₹${qty * item.price}", fontWeight = FontWeight.ExtraBold, color = if (staffMode) Maroon else Green)
                            }
                        }
                        Divider()
                        Text("Fixed total: ₹$fixedTotal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        if (items.any { (selected[it.name] ?: 0) > 0 && it.quoteOnly }) Text("Stage/Decoration and Lighting will be quoted separately by CaterHub.", color = Muted)
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { if (step == 0) onBackClick() else step-- }, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(if (step == 0) "Cancel" else "Back", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        if (step < 2) {
                            error = when {
                                selected.values.sum() == 0 -> "Please select at least one service."
                                step == 1 && eventDate.isBlank() -> "Please enter event date."
                                step == 1 && location.isBlank() -> "Please enter event address."
                                step == 1 && area.isBlank() -> "Please enter area/locality."
                                else -> null
                            }
                            if (error == null) step++
                        } else {
                            scope.launch {
                                submitting = true; error = null
                                try {
                                    if (staffMode) {
                                        items.filter { (selected[it.name] ?: 0) > 0 }.forEach { item ->
                                            val qty = selected[item.name] ?: 0
                                            workerRepository.createStaffingRequest(CreateStaffingRequest(eventType, item.workerType!!, eventDate, startTime, endTime, location, area, qty, BigDecimal(item.price), notes.ifBlank { null }))
                                        }
                                    } else {
                                        val details = items.filter { (selected[it.name] ?: 0) > 0 }.joinToString("; ") { item ->
                                            val qty = selected[item.name] ?: 0
                                            if (item.quoteOnly) "${item.name}: selected x$qty (quote)" else "${item.name}: $qty x ₹${item.price}"
                                        }
                                        workerRepository.createServiceRequest(ServiceRequestRequest("EQUIPMENT", eventType, eventDate, startTime, location, area, "$details${if (notes.isBlank()) "" else "; Notes: $notes"}", BigDecimal(fixedTotal)))
                                    }
                                    onSubmitted()
                                } catch (e: Exception) { error = e.message ?: "Unable to submit request." }
                                finally { submitting = false }
                            }
                        }
                    },
                    enabled = !submitting,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (staffMode) Maroon else Green)
                ) { Text(if (step == 2) "Submit Request" else "Continue", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable private fun ServiceItemCard(item: ServiceItem, count: Int, onMinus: () -> Unit, onPlus: () -> Unit, onCount: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(if (item.quoteOnly) Green.copy(.10f) else Maroon.copy(.08f))) { Icon(item.icon, null, tint = if (item.quoteOnly) Green else Maroon, modifier = Modifier.padding(12.dp).size(28.dp)) }
                Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
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
                Spacer(Modifier.height(8.dp)); OutlinedTextField(count.toString(), { onCount(it.filter(Char::isDigit).toIntOrNull() ?: 0) }, label = { Text("Custom quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = TextDark))
            }
            if (count > 0 && !item.quoteOnly) Text("Selected total: ₹${count * item.price}", Modifier.padding(top = 7.dp), color = Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun TotalCard(total: Int, color: Color) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(color.copy(.10f))) { Row(Modifier.padding(17.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Current fixed total", fontWeight = FontWeight.Bold, color = TextDark); Text("₹$total", fontWeight = FontWeight.ExtraBold, color = color) } } }

@Composable private fun EventTypeField(value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }; val options = listOf("Birthday","Wedding","Engagement","Housewarming","Corporate","Baby Shower","Naming Ceremony","Festival","Other")
    Box { OutlinedTextField(value, {}, label={Text("Event type")}, readOnly=true, modifier=Modifier.fillMaxWidth().clickable{expanded=true}, textStyle=LocalTextStyle.current.copy(color=TextDark)); DropdownMenu(expanded, {expanded=false}) { options.forEach { DropdownMenuItem(text={Text(it)}, onClick={onChange(it);expanded=false}) } } }
}

@Composable private fun SectionLabel(text: String) { Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Filled.LocationOn, null, tint=Gold, modifier=Modifier.size(22.dp)); Spacer(Modifier.width(7.dp)); Text(text, fontWeight=FontWeight.ExtraBold, color=TextDark) } }

@Composable private fun ReviewBox(event: String, date: String, start: String, end: String, location: String, area: String) { Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(Color.White), border=BorderStroke(1.dp,Border)) { Column(Modifier.padding(17.dp), verticalArrangement=Arrangement.spacedBy(7.dp)) { Text("$event", style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.ExtraBold); Text("$date • $start${if(end.isBlank()) "" else " - $end"}", color=Muted); Text("$location, $area", color=TextDark) } } }
