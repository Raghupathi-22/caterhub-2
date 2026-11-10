package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.RiceOrderResponse
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.data.remote.dto.RiceSampleResponse
import com.daily.cetaring.domain.rice.RiceOrderRules
import com.daily.cetaring.domain.rice.RiceQuote
import com.daily.cetaring.presentation.components.BookingCard
import com.daily.cetaring.presentation.components.BookingFlowTheme
import com.daily.cetaring.presentation.components.BookingFlowTopBar
import com.daily.cetaring.presentation.components.BookingPalette
import com.daily.cetaring.presentation.components.BookingPrimaryButton
import com.daily.cetaring.presentation.components.BookingSecondaryButton
import com.daily.cetaring.presentation.components.BookingSectionTitle
import com.daily.cetaring.presentation.components.BookingTextField
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.RiceHeroImage
import com.daily.cetaring.presentation.components.RicePalette
import com.daily.cetaring.presentation.components.RiceSampleOfferCard
import com.daily.cetaring.presentation.viewmodel.RiceOrderForm
import com.daily.cetaring.presentation.viewmodel.RiceProductState
import com.daily.cetaring.presentation.viewmodel.RiceUiState
import com.daily.cetaring.presentation.viewmodel.RiceViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object RiceOrderTabs {
    const val ORDER = 0
    const val SAMPLE = 1
    const val HISTORY = 2
}

@Composable
fun RiceOrderScreen(
    viewModel: RiceViewModel,
    initialTab: Int,
    onBackClick: () -> Unit,
    onAuthRequired: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 2)) }

    LaunchedEffect(Unit) { viewModel.loadProduct() }
    LaunchedEffect(state.authRequired) {
        if (state.authRequired) {
            viewModel.onAuthRequiredHandled()
            onAuthRequired()
        }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == RiceOrderTabs.HISTORY) viewModel.loadHistory()
    }

    BookingFlowTheme {
        Scaffold(
            containerColor = BookingPalette.Background,
            topBar = { BookingFlowTopBar(title = "Village Farm Rice", onBackClick = onBackClick) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BookingPalette.Background,
                    contentColor = RicePalette.Green,
                    indicator = { positions ->
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTab]), color = RicePalette.Green)
                    }
                ) {
                    listOf("Order", "Free sample", "My orders").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) },
                            selectedContentColor = RicePalette.Green,
                            unselectedContentColor = BookingPalette.Muted
                        )
                    }
                }
                when (selectedTab) {
                    RiceOrderTabs.ORDER -> OrderTab(state, viewModel, onViewOrders = { selectedTab = RiceOrderTabs.HISTORY })
                    RiceOrderTabs.SAMPLE -> SampleTab(state, viewModel, onViewOrders = { selectedTab = RiceOrderTabs.HISTORY })
                    else -> HistoryTab(state, viewModel, onOrderRice = { selectedTab = RiceOrderTabs.ORDER })
                }
            }
        }

        if (state.showOrderConfirmation) {
            val quote = state.quote
            val product = state.product
            if (quote != null && product != null) {
                OrderConfirmationDialog(
                    form = state.orderForm,
                    product = product,
                    quote = quote,
                    onConfirm = viewModel::confirmOrder,
                    onDismiss = viewModel::dismissOrderConfirmation
                )
            }
        }
    }
}

@Composable
private fun ProductStateGate(state: RiceUiState, viewModel: RiceViewModel, content: @Composable (RiceProductResponse) -> Unit) {
    when (val productState = state.productState) {
        RiceProductState.Loading -> CaterHubLoadingState("Loading Village Farm Rice…")
        is RiceProductState.Error -> Box(Modifier.fillMaxSize().padding(20.dp)) {
            CaterHubErrorState(productState.message, onRetry = { viewModel.loadProduct(force = true) })
        }
        is RiceProductState.Loaded -> content(productState.product)
    }
}

@Composable
private fun ScrollColumn(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OrderTab(state: RiceUiState, viewModel: RiceViewModel, onViewOrders: () -> Unit) {
    ProductStateGate(state, viewModel) { product ->
        val placed = state.placedOrder
        if (placed != null) {
            ScrollColumn { OrderSuccess(placed, product, onViewOrders = onViewOrders, onNewOrder = viewModel::startNewOrder) }
            return@ProductStateGate
        }
        if (!RiceOrderRules.isOrderable(product)) {
            ScrollColumn {
                CaterHubEmptyState(
                    title = "Ordering paused",
                    message = "Village Farm Rice ordering is not available right now. Please check back soon."
                )
            }
            return@ProductStateGate
        }
        val form = state.orderForm
        val errors = state.orderErrors
        ScrollColumn {
            Surface(shape = RoundedCornerShape(20.dp), color = RicePalette.PaleGreen, border = BorderStroke(1.dp, RicePalette.Border)) {
                Column {
                    RiceHeroImage(product.imageUrl, height = 150.dp)
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name ?: "Village Farm Rice", color = RicePalette.GreenDeep, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                            Text("Minimum order ${product.minOrderKg} kg", color = RicePalette.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(RiceOrderRules.formatInr(product.basePackPrice), color = RicePalette.GreenDeep, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                            Text("per ${product.basePackKg} kg", color = RicePalette.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            BookingCard {
                BookingSectionTitle("Quantity", icon = Icons.Filled.Scale)
                QuantityStepper(
                    product = product,
                    quantityText = form.quantityText,
                    error = errors.quantity,
                    onQuantityChange = { text -> viewModel.updateOrderForm { it.copy(quantityText = text.filter(Char::isDigit).take(6)) } },
                    onIncrement = viewModel::incrementQuantity,
                    onDecrement = viewModel::decrementQuantity
                )
            }

            BookingCard {
                BookingSectionTitle("Your details", icon = Icons.Filled.Person)
                BookingTextField(
                    value = form.customerName,
                    onValueChange = { v -> viewModel.updateOrderForm { it.copy(customerName = v.take(120)) } },
                    label = "Full name",
                    isError = errors.customerName != null,
                    supportingText = errors.customerName
                )
                BookingTextField(
                    value = form.mobileNumber,
                    onValueChange = { v -> viewModel.updateOrderForm { it.copy(mobileNumber = v.take(16)) } },
                    label = "Mobile number",
                    leadingIcon = Icons.Filled.Phone,
                    isError = errors.mobileNumber != null,
                    supportingText = errors.mobileNumber,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            BookingCard {
                BookingSectionTitle("Delivery", icon = Icons.Filled.LocalShipping)
                BookingTextField(
                    value = form.deliveryAddress,
                    onValueChange = { v -> viewModel.updateOrderForm { it.copy(deliveryAddress = v.take(1000)) } },
                    label = "Delivery address",
                    singleLine = false,
                    minLines = 2,
                    isError = errors.deliveryAddress != null,
                    supportingText = errors.deliveryAddress
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BookingTextField(
                        value = form.area,
                        onValueChange = { v -> viewModel.updateOrderForm { it.copy(area = v.take(100)) } },
                        label = "Area",
                        isError = errors.area != null,
                        supportingText = errors.area,
                        modifier = Modifier.weight(1f)
                    )
                    BookingTextField(
                        value = form.pincode,
                        onValueChange = { v -> viewModel.updateOrderForm { it.copy(pincode = v.filter(Char::isDigit).take(6)) } },
                        label = "Pincode",
                        isError = errors.pincode != null,
                        supportingText = errors.pincode,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Preferred delivery date", color = BookingPalette.Muted, style = MaterialTheme.typography.labelLarge)
                DeliveryDateChips(form.preferredDeliveryDate) { date -> viewModel.updateOrderForm { it.copy(preferredDeliveryDate = date) } }
                Text("Preferred time", color = BookingPalette.Muted, style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RiceOrderRules.deliverySlots, key = { it.first }) { (value, label) ->
                        RiceChip(label, selected = form.preferredDeliverySlot == value) {
                            viewModel.updateOrderForm { it.copy(preferredDeliverySlot = value) }
                        }
                    }
                }
                BookingTextField(
                    value = form.deliveryNotes,
                    onValueChange = { v -> viewModel.updateOrderForm { it.copy(deliveryNotes = v.take(1000)) } },
                    label = "Delivery notes (optional)",
                    placeholder = "Landmark, gate instructions…",
                    singleLine = false,
                    minLines = 2
                )
            }

            PriceSummaryCard(product, state.quote)

            state.orderError?.let { ErrorBanner(it) }

            BookingPrimaryButton(
                text = state.quote?.let { "Review order · ${RiceOrderRules.formatInr(it.total)}" } ?: "Review order",
                onClick = viewModel::requestOrderConfirmation,
                loading = state.orderSubmitting,
                loadingText = "Placing your order…",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    product: RiceProductResponse,
    quantityText: String,
    error: String?,
    onQuantityChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val quantity = quantityText.toIntOrNull()
    val min = product.minOrderKg ?: 1
    val step = RiceOrderRules.stepKg(product)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val stepperColors = IconButtonDefaults.filledIconButtonColors(containerColor = RicePalette.PaleGreen, contentColor = RicePalette.Green)
        FilledIconButton(
            onClick = onDecrement,
            enabled = quantity == null || quantity > min,
            colors = stepperColors,
            modifier = Modifier.size(48.dp).semantics { contentDescription = "Decrease quantity by $step kg" }
        ) { Icon(Icons.Filled.Remove, null) }
        BookingTextField(
            value = quantityText,
            onValueChange = onQuantityChange,
            label = "Quantity (kg)",
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        FilledIconButton(
            onClick = onIncrement,
            enabled = product.maxOrderKg == null || quantity == null || quantity < product.maxOrderKg,
            colors = stepperColors,
            modifier = Modifier.size(48.dp).semantics { contentDescription = "Increase quantity by $step kg" }
        ) { Icon(Icons.Filled.Add, null) }
    }
    Text(
        error ?: buildString {
            append("Minimum $min kg")
            if (step > 1) append(" · in steps of $step kg")
            product.maxOrderKg?.let { append(" · up to $it kg") }
        },
        color = if (error != null) BookingPalette.Danger else BookingPalette.Muted,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun DeliveryDateChips(selectedIso: String?, onSelect: (String?) -> Unit) {
    val today = remember { LocalDate.now() }
    val dates = remember(today) { (1L..14L).map { today.plusDays(it) } }
    val formatter = remember { DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()) }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 4.dp)) {
        item { RiceChip("No preference", selected = selectedIso == null) { onSelect(null) } }
        items(dates, key = { it.toEpochDay() }) { date ->
            val iso = date.toString()
            RiceChip(date.format(formatter), selected = selectedIso == iso) { onSelect(iso) }
        }
    }
}

@Composable
private fun RiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RicePalette.Green,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = BookingPalette.Ink
        )
    )
}

@Composable
private fun PriceSummaryCard(product: RiceProductResponse, quote: RiceQuote?) {
    BookingCard {
        BookingSectionTitle("Price summary", icon = Icons.Filled.Receipt)
        if (quote == null) {
            Text("Enter a valid quantity to see your total.", color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium)
            return@BookingCard
        }
        PriceRow("Rate", "${RiceOrderRules.formatInr(product.basePackPrice)} per ${product.basePackKg} kg")
        PriceRow("Quantity", "${quote.quantityKg} kg")
        PriceRow("Rice subtotal", RiceOrderRules.formatInr(quote.subtotal))
        PriceRow("Delivery", quote.deliveryCharge?.let { RiceOrderRules.formatInr(it) } ?: "Confirmed by our team")
        HorizontalDivider(color = BookingPalette.Border)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Total", color = BookingPalette.Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(RiceOrderRules.formatInr(quote.total), color = RicePalette.GreenDeep, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        }
        if (quote.deliveryCharge == null) {
            Text("Delivery charges, if any, are confirmed by our team before dispatch.", color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
        }
        product.paymentNote?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, color = BookingPalette.Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = BookingPalette.DangerSoft) {
        Text(message, color = BookingPalette.Danger, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(12.dp))
    }
}

@Composable
private fun OrderConfirmationDialog(
    form: RiceOrderForm,
    product: RiceProductResponse,
    quote: RiceQuote,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Confirm your rice order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceRow("Product", product.name ?: "Village Farm Rice")
                PriceRow("Quantity", "${quote.quantityKg} kg")
                PriceRow("Rice subtotal", RiceOrderRules.formatInr(quote.subtotal))
                PriceRow("Delivery", quote.deliveryCharge?.let { RiceOrderRules.formatInr(it) } ?: "Confirmed by our team")
                HorizontalDivider(color = BookingPalette.Border)
                Row(Modifier.fillMaxWidth()) {
                    Text("Final price", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(RiceOrderRules.formatInr(quote.total), color = RicePalette.GreenDeep, fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    "Deliver to: ${form.deliveryAddress.trim()}, ${form.area.trim()} - ${form.pincode.trim()}",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                product.paymentNote?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Confirm order", color = RicePalette.Green, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Edit", color = BookingPalette.Muted) }
        }
    )
}

@Composable
private fun OrderSuccess(order: RiceOrderResponse, product: RiceProductResponse, onViewOrders: () -> Unit, onNewOrder: () -> Unit) {
    SuccessHeader("Order placed", "Thank you! Our team will call you to confirm delivery.")
    BookingCard {
        PriceRow("Order number", order.orderNumber ?: "#${order.id}")
        PriceRow("Quantity", "${order.quantityKg ?: "-"} kg")
        PriceRow("Total", RiceOrderRules.formatInr(order.totalAmount))
        PriceRow("Delivery", order.deliveryCharge?.let { RiceOrderRules.formatInr(it) } ?: "Confirmed by our team")
        PriceRow("Status", RiceOrderRules.displayStatus(order.status))
        PriceRow("Payment", RiceOrderRules.displayStatus(order.paymentStatus))
        product.paymentNote?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
    BookingPrimaryButton("View my orders", onClick = onViewOrders, modifier = Modifier.fillMaxWidth())
    BookingSecondaryButton("Place another order", onClick = onNewOrder, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SuccessHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Filled.CheckCircle, null, tint = RicePalette.Green, modifier = Modifier.size(56.dp))
        Text(title, color = BookingPalette.Ink, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, color = BookingPalette.Muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SampleTab(state: RiceUiState, viewModel: RiceViewModel, onViewOrders: () -> Unit) {
    ProductStateGate(state, viewModel) { product ->
        val result = state.sampleResult
        ScrollColumn {
            if (result != null) {
                SuccessHeader("Sample request received", "Your free sample is reserved. Pickup only — no home delivery.")
                SampleDetailsCard(result, product)
                BookingPrimaryButton("View my requests", onClick = onViewOrders, modifier = Modifier.fillMaxWidth())
                BookingSecondaryButton("Done", onClick = viewModel::clearSampleResult, modifier = Modifier.fillMaxWidth())
                return@ScrollColumn
            }
            if (product.sampleEnabled != true || product.active != true) {
                CaterHubEmptyState(title = "Samples unavailable", message = "Free samples are not available right now. Please check back soon.")
                return@ScrollColumn
            }
            RiceSampleOfferCard(product = product, onRequestSample = null)
            BookingCard {
                BookingSectionTitle("Request your free sample", icon = Icons.Filled.Storefront)
                val form = state.sampleForm
                BookingTextField(
                    value = form.customerName,
                    onValueChange = { viewModel.updateSampleForm(name = it.take(120)) },
                    label = "Full name",
                    isError = form.nameError != null,
                    supportingText = form.nameError
                )
                BookingTextField(
                    value = form.mobileNumber,
                    onValueChange = { viewModel.updateSampleForm(mobile = it.take(16)) },
                    label = "Mobile number",
                    leadingIcon = Icons.Filled.Phone,
                    isError = form.mobileError != null,
                    supportingText = form.mobileError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                PriceRow("Sample", RiceOrderRules.formatSampleWeight(product.sampleWeightGrams))
                PriceRow("Price", "FREE (₹0)")
                PriceRow("Collection", "Pickup only")
                Text(
                    "You will not be charged. Our team will confirm the pickup location and collection time.",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.sampleError?.let { ErrorBanner(it) }
            BookingPrimaryButton(
                text = "Request free sample",
                onClick = viewModel::submitSample,
                loading = state.sampleSubmitting,
                loadingText = "Submitting request…",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SampleDetailsCard(sample: RiceSampleResponse, product: RiceProductResponse?) {
    BookingCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(sample.requestNumber ?: "Request #${sample.id}", color = BookingPalette.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            CaterHubStatusChip(sample.status.orEmpty())
        }
        PriceRow("Sample", "${RiceOrderRules.formatSampleWeight(sample.sampleWeightGrams)} · FREE (₹0)")
        PriceRow("Collection", "Pickup only")
        val location = sample.pickupLocation?.takeIf { it.isNotBlank() }
        val time = formatDateTime(sample.pickupTime)
        if (location != null && time != null) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Place, null, tint = RicePalette.Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(location, color = BookingPalette.Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Collect on $time", color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else if (sample.status == "REQUESTED") {
            Text(
                "Our team will contact you to confirm the pickup location and collection time.",
                color = BookingPalette.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        (sample.pickupInstructions?.takeIf { it.isNotBlank() } ?: product?.samplePickupInstructions?.takeIf { it.isNotBlank() })?.let {
            Text("Pickup instructions: $it", color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HistoryTab(state: RiceUiState, viewModel: RiceViewModel, onOrderRice: () -> Unit) {
    val history = state.history
    when {
        history.loading && !history.loaded -> CaterHubLoadingState("Loading your rice orders…")
        history.error != null -> Box(Modifier.fillMaxSize().padding(20.dp)) {
            CaterHubErrorState(history.error, onRetry = { viewModel.loadHistory(force = true) })
        }
        history.orders.isEmpty() && history.samples.isEmpty() -> ScrollColumn {
            CaterHubEmptyState(
                title = "No rice orders yet",
                message = "Your Village Farm Rice orders and free sample requests will appear here.",
                actionText = "Order rice",
                onActionClick = onOrderRice
            )
        }
        else -> ScrollColumn {
            if (history.orders.isNotEmpty()) {
                Text("Bulk orders", color = BookingPalette.Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                history.orders.forEach { OrderHistoryCard(it) }
            }
            if (history.samples.isNotEmpty()) {
                Text("Free sample requests", color = BookingPalette.Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                history.samples.forEach { SampleDetailsCard(it, state.product) }
            }
            TextButton(onClick = { viewModel.loadHistory(force = true) }, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh", color = RicePalette.Green)
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: RiceOrderResponse) {
    BookingCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(order.orderNumber ?: "Order #${order.id}", color = BookingPalette.Ink, fontWeight = FontWeight.Bold)
                formatDateTime(order.createdAt)?.let { Text("Placed $it", color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall) }
            }
            CaterHubStatusChip(order.status.orEmpty())
        }
        PriceRow("Quantity", "${order.quantityKg ?: "-"} kg")
        PriceRow("Total", RiceOrderRules.formatInr(order.totalAmount))
        PriceRow("Payment", RiceOrderRules.displayStatus(order.paymentStatus))
        val deliveryParts = listOfNotNull(
            order.preferredDeliveryDate?.let { runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMM yyyy")) }.getOrNull() },
            order.preferredDeliverySlot?.let { slot -> RiceOrderRules.deliverySlots.firstOrNull { it.first == slot }?.second }
        )
        if (deliveryParts.isNotEmpty()) PriceRow("Preferred delivery", deliveryParts.joinToString(" · "))
        listOfNotNull(order.deliveryAddress, order.area, order.pincode).filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.let {
            Text(it.joinToString(", "), color = BookingPalette.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatDateTime(value: String?): String? = value?.let {
    runCatching {
        LocalDateTime.parse(it.take(19)).format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault()))
    }.getOrNull()
}
