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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.BookingResponse
import com.daily.cetaring.data.remote.dto.CustomerBookingSource
import com.daily.cetaring.data.remote.dto.CustomerBookingUiModel
import com.daily.cetaring.data.remote.dto.BookingValidationResult
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.EventTypeDefinition
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.ReviewLineItem
import com.daily.cetaring.presentation.components.ReviewRequestCard
import com.daily.cetaring.presentation.components.SummaryRow
import com.daily.cetaring.presentation.components.categoryUiMeta
import com.daily.cetaring.presentation.components.formatDateTime
import com.daily.cetaring.presentation.viewmodel.BookingUiState
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import com.daily.cetaring.domain.catalog.ServiceCatalog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)
private val SoftGold = Color(0xFFFFF3D6)
private val SoftGreen = Color(0xFFEAF4E7)
private val BookingDateStorageFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val BookingDateDisplayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val BookingTimeStorageFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val BookingTimeDisplayFormatter = DateTimeFormatter.ofPattern("h:mm a")
private const val CustomGuestValidationMessage = "Enter a guest count between 1 and 2,000."
private const val BookingFlowStepCount = 5
private const val BookingFlowReviewStepIndex = BookingFlowStepCount - 1

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
    val currentStepValidation = remember(step, draft) {
        if (step == BookingFlowReviewStepIndex) BookingValidationResult.Valid else viewModel.validateStep(step)
    }
    val canContinue = currentStepValidation is BookingValidationResult.Valid &&
        uiState !is BookingUiState.Loading

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
            ProgressHeader(step, BookingFlowStepCount)

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    0 -> EventStep(draft, viewModel)
                    1 -> FoodServiceSelectionStep(draft, viewModel)
                    2 -> CateringPlanAndLocationStep(draft, viewModel)
                    3 -> DateTimeStep(draft, viewModel)
                    else -> ReviewStep(draft) { step = it }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CaterHubSecondaryButton(
                    text = if (step == 0) "Cancel" else "Back",
                    onClick = { if (step == 0) onBackClick() else step-- },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is BookingUiState.Loading
                )

                CaterHubPrimaryButton(
                    text = if (step == BookingFlowReviewStepIndex) "Submit Booking" else "Continue",
                    enabled = canContinue,
                    onClick = {
                        val validation = if (step == BookingFlowReviewStepIndex) {
                            BookingValidationResult.Valid
                        } else {
                            viewModel.validateStep(step)
                        }

                        if (validation is BookingValidationResult.Invalid) {
                            coroutineScope.launch { snackbar.showSnackbar(validation.message) }
                        } else if (step < BookingFlowReviewStepIndex) {
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
private fun ProgressHeader(step: Int, totalSteps: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Step ${step + 1} of $totalSteps",
            color = Green,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(totalSteps) { index ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index <= step) Green else Border
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
    Text(
        text = draft.eventType?.let { EventTypeCatalog.displayNameForBackendValue(it) } ?: "Select event type",
        color = if (draft.eventType == null) Muted else TextDark,
        fontWeight = FontWeight.SemiBold
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        EventTypeCatalog.eventTypes.forEach { event ->
            EventTypeCard(
                event = event,
                selected = draft.eventType == event.backendValue,
                onClick = { viewModel.updateDraft { it.copy(eventType = event.backendValue) } }
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
            GuestCountOptionCard(
                label = "$guests guests",
                selected = draft.guestCountSelection == BookingOptions.guestSelectionPreset && draft.guestCount == guests,
                onClick = {
                    viewModel.updateDraft {
                        it.copy(
                            guestCount = guests,
                            guestCountSelection = BookingOptions.guestSelectionPreset,
                            customGuestCountInput = ""
                        )
                    }
                }
            )
        }
        GuestCountOptionCard(
            label = "Custom guest count",
            selected = draft.guestCountSelection == BookingOptions.guestSelectionCustom,
            onClick = {
                viewModel.updateDraft {
                    it.copy(
                        guestCountSelection = BookingOptions.guestSelectionCustom,
                        guestCount = if (it.customGuestCountInput.isBlank()) null else it.guestCount
                    )
                }
            }
        )
    }

    if (draft.guestCountSelection == BookingOptions.guestSelectionCustom) {
        val invalidCustomGuestCount = draft.customGuestCountInput.isNotBlank() && draft.guestCount == null
        OutlinedTextField(
            value = draft.customGuestCountInput,
            onValueChange = { value ->
                if (value.length > BookingOptions.maxGuestInputLength) return@OutlinedTextField
                if (value.isNotEmpty() && !value.all(Char::isDigit)) return@OutlinedTextField
                val parsed = value.toIntOrNull()?.takeIf { it in 1..BookingOptions.maxGuestCount }
                viewModel.updateDraft {
                    it.copy(
                        guestCountSelection = BookingOptions.guestSelectionCustom,
                        customGuestCountInput = value,
                        guestCount = parsed
                    )
                }
            },
            label = { Text("Custom guest count") },
            placeholder = { Text("Enter number of guests") },
            supportingText = {
                if (invalidCustomGuestCount) {
                    Text(CustomGuestValidationMessage)
                }
            },
            isError = invalidCustomGuestCount,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FoodServiceSelectionStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    Text(
        "What would you like to serve?",
        color = Maroon,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold
    )
    Text(
        "Select one or more food services for your event.",
        color = Muted
    )

    BookingOptions.cateringMealServiceOptions.forEach { option ->
        val selected = option.type in draft.selectedFoodServices
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.updateDraft { current ->
                        val updated = current.selectedFoodServices.toMutableSet()
                        if (!updated.add(option.type)) {
                            updated.remove(option.type)
                        }
                        current.copy(selectedFoodServices = updated)
                    }
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) SoftGreen.copy(alpha = 0.65f) else Color.White
            ),
            border = BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Green else Border
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) Green else Color(0xFFFFF7E6)
                    )
                ) {
                    Icon(
                        imageVector = mealServiceIcon(option.type),
                        contentDescription = option.displayName,
                        tint = if (selected) Color.White else Green,
                        modifier = Modifier.padding(9.dp)
                    )
                }
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        option.displayName,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        option.description,
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = Green,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Border,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    val selectedServices = draft.selectedFoodServices
        .sortedBy { it.sortOrder }
        .map { it.backendLabel }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9EC)),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${selectedServices.size} services selected",
                color = Green,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                if (selectedServices.isEmpty()) "Select at least one service to continue."
                else "Selected: ${selectedServices.joinToString(" • ")}",
                color = if (selectedServices.isEmpty()) Muted else TextDark,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun mealServiceIcon(type: CateringMealServiceType): ImageVector = when (type) {
    CateringMealServiceType.BREAKFAST -> Icons.Filled.FreeBreakfast
    CateringMealServiceType.LUNCH -> Icons.Filled.LunchDining
    CateringMealServiceType.SNACKS -> Icons.Filled.Fastfood
    CateringMealServiceType.BEVERAGES -> Icons.Filled.LocalCafe
    CateringMealServiceType.DINNER -> Icons.Filled.DinnerDining
}

@Composable
private fun EventTypeCard(
    event: EventTypeDefinition,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (event.id) {
        "birthday" -> Icons.Filled.Cake
        "wedding", "engagement", "reception", "anniversary" -> Icons.Filled.Favorite
        "housewarming" -> Icons.Filled.Home
        "corporate", "school_college" -> Icons.Filled.Groups
        "baby_shower", "naming_ceremony" -> Icons.Filled.Cake
        "festival", "party" -> Icons.Filled.Star
        else -> Icons.Filled.Event
    }

    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Green else Color(0xFFFFFCF7)
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Green else Border
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 5.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color.White else Green,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(7.dp))
            Text(
                event.displayName,
                color = if (selected) Color.White else TextDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GuestCountOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Green else Color(0xFFFFFCF7)
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Green else Border
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 5.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Groups,
                contentDescription = null,
                tint = if (selected) Color.White else Green,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(7.dp))
            Text(
                label,
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
        "Choose a vegetarian or non-vegetarian package, or customize your menu.",
        color = Muted
    )

    val selectedFoodType = draft.cateringFoodType.ifBlank { BookingOptions.foodTypeNonVegetarian }
    val visiblePlans = BookingOptions.plansByFoodType(selectedFoodType)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FoodTypeSelectorOption(
            emoji = "🥬",
            label = "Vegetarian",
            selected = selectedFoodType == BookingOptions.foodTypeVegetarian,
            onClick = {
                val defaultPlan = BookingOptions.defaultPlanForFoodType(BookingOptions.foodTypeVegetarian)
                viewModel.updateDraft {
                    it.copy(
                        cateringFoodType = BookingOptions.foodTypeVegetarian,
                        cateringPlan = defaultPlan.backendValue
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
        FoodTypeSelectorOption(
            emoji = "🍗",
            label = "Non-Vegetarian",
            selected = selectedFoodType == BookingOptions.foodTypeNonVegetarian,
            onClick = {
                val defaultPlan = BookingOptions.defaultPlanForFoodType(BookingOptions.foodTypeNonVegetarian)
                viewModel.updateDraft {
                    it.copy(
                        cateringFoodType = BookingOptions.foodTypeNonVegetarian,
                        cateringPlan = defaultPlan.backendValue
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
        FoodTypeSelectorOption(
            emoji = "✨",
            label = "Custom",
            selected = selectedFoodType == BookingOptions.foodTypeCustom,
            onClick = {
                val defaultPlan = BookingOptions.defaultPlanForFoodType(BookingOptions.foodTypeCustom)
                viewModel.updateDraft {
                    it.copy(
                        cateringFoodType = BookingOptions.foodTypeCustom,
                        cateringPlan = defaultPlan.backendValue
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
    }

    visiblePlans.forEach { plan ->
        PlanCard(
            option = plan,
            selected = draft.cateringPlan == plan.backendValue
        ) {
            viewModel.updateDraft { it.copy(cateringPlan = plan.backendValue) }
        }
    }

    if (draft.cateringPlan == BookingOptions.planCustomMenu) {
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
private fun FoodTypeSelectorOption(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Green else Color(0xFFFFFCF7)
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Green else Border
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.size(6.dp))
            Text(
                label,
                color = if (selected) Color.White else TextDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PlanCard(
    option: BookingOptions.CateringPlanOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SoftGreen.copy(alpha = 0.60f) else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Green else Border
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 5.dp else 2.dp
        )
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (option.foodType) {
                            BookingOptions.foodTypeVegetarian -> "🥬"
                            BookingOptions.foodTypeNonVegetarian -> "🍗"
                            else -> "✨"
                        },
                        fontSize = 16.sp
                    )
                    Text(option.title, color = if (selected) Green else TextDark, fontWeight = FontWeight.ExtraBold)
                }
                if (option.popular) {
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGold)
                    ) {
                        Text(
                            text = "POPULAR",
                            color = Gold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = Green
                    )
                }
            }
            Text(
                option.pricePerPerson?.let { "₹$it / person" } ?: "Build your own menu",
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            option.description?.let {
                Text(it, color = Muted, lineHeight = 20.sp)
                Spacer(Modifier.height(2.dp))
            }
            option.menuItems.forEach { item ->
                Text("• $item", color = Muted, lineHeight = 20.sp)
            }
            if (option.backendValue == BookingOptions.planCustomMenu) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Customize Menu",
                    color = Green,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
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
        initialSelectedDateMillis = draft.eventDate.takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDate.parse(it, BookingDateStorageFormatter)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= today

            override fun isSelectableYear(year: Int): Boolean =
                year >= Calendar.getInstance().get(Calendar.YEAR)
        }
    )
    val timeState = rememberTimePickerState(
        initialHour = draft.eventTime.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalTime.parse(it, BookingTimeStorageFormatter).hour }.getOrNull()
        } ?: 12,
        initialMinute = draft.eventTime.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalTime.parse(it, BookingTimeStorageFormatter).minute }.getOrNull()
        } ?: 0,
        is24Hour = false
    )

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

    PickerField("Date", draft.eventDate.takeIf { it.isNotBlank() }?.let(::formatBookingDate) ?: "Select date") {
        showDate = true
    }

    PickerField("Time", draft.eventTime.takeIf { it.isNotBlank() }?.let(::formatBookingTime) ?: "Select time") {
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
                            timeZone = TimeZone.getDefault()
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

    ReviewRequestCard(
        eventType = EventTypeCatalog.displayNameForBackendValue(draft.eventType),
        eventDate = formatBookingDate(draft.eventDate),
        timeRange = formatBookingReviewTimeRange(draft.eventTime),
        location = draft.deliveryAddress(),
        services = listOf(
            ReviewLineItem(
                title = "Food Services",
                subtitle = draft.selectedFoodServicesLabel(),
                amountText = null
            ),
            ReviewLineItem(
                title = "Catering Plan",
                subtitle = draft.cateringPlan.ifBlank { BookingOptions.fullCatering },
                amountText = null
            ),
            ReviewLineItem(
                title = "Guest Count",
                subtitle = "${draft.guestCount ?: 0} guests",
                amountText = null
            ),
            ReviewLineItem(
                title = "Food requirements",
                subtitle = draft.foodRequirements.ifBlank { "Standard plan menu" },
                amountText = null
            )
        ),
        totalLabel = "To be quoted"
    )
}

@Composable
fun BookingSuccessScreen(
    viewModel: BookingViewModel,
    bookingId: Long,
    onViewBooking: (Long) -> Unit,
    onHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val submitted = uiState as? BookingUiState.Submitted
    val booking = submitted?.booking

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
    onBookingClick: (String, Long) -> Unit,
    onExploreServicesClick: () -> Unit,
    onBackToHome: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var statusFilter by remember { mutableStateOf("All") }
    var categoryFilter by remember { mutableStateOf("all") }
    LaunchedEffect(Unit) { viewModel.loadBookings() }

    Scaffold(
        containerColor = Cream,
        topBar = { BookingTopBar("My Bookings", onBackClick) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onBackToHome,
                    icon = { Icon(Icons.Filled.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Event, "Bookings") },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Filled.AccountCircle, "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
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
                    Text(
                        "Your catering & event services",
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Pending", "Confirmed", "Completed", "Cancelled").forEach { option ->
                            FilterChip(
                                selected = statusFilter == option,
                                onClick = { statusFilter = option },
                                label = { Text(option) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf("all" to "All") + ServiceCatalog.customerCategories.map { it.id to it.title }
                        categories.forEach { option ->
                            AssistChip(
                                onClick = { categoryFilter = option.first },
                                label = { Text(option.second, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = if (categoryFilter == option.first) {
                                    { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Maroon) }
                                } else null
                            )
                        }
                    }

                    val filtered = state.bookings.filter { booking ->
                        val statusMatches = when (statusFilter) {
                            "All" -> true
                            "Pending" -> booking.status.equals("PENDING", true) || booking.status.equals("OPEN", true)
                            "Confirmed" -> booking.status.equals("CONFIRMED", true) || booking.status.equals("APPROVED", true)
                            "Completed" -> booking.status.equals("COMPLETED", true) || booking.status.equals("DELIVERED", true)
                            "Cancelled" -> booking.status.equals("CANCELLED", true) || booking.status.equals("REJECTED", true)
                            else -> true
                        }
                        val categoryMatches = categoryFilter == "all" || booking.categoryId == categoryFilter
                        statusMatches && categoryMatches
                    }

                    if (filtered.isEmpty()) {
                        CaterHubEmptyState(
                            "No bookings yet",
                            "Your event plans will appear here.",
                            actionText = "Explore Services",
                            onActionClick = onExploreServicesClick
                        )
                        CaterHubSecondaryButton(
                            text = "Back to Home",
                            onClick = onBackToHome,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        filtered.forEach { booking ->
                            UnifiedBookingCard(booking = booking) {
                                onBookingClick(booking.source.name.lowercase(Locale.ROOT), booking.id)
                            }
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
    source: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookingSource = remember(source) {
        if (source.equals("service_request", true)) CustomerBookingSource.SERVICE_REQUEST else CustomerBookingSource.CATERING
    }
    LaunchedEffect(bookingId, source) { viewModel.loadBooking(bookingId, bookingSource) }

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
                        { viewModel.loadBooking(bookingId, bookingSource) }
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
                    UnifiedBookingDetailsCard(state.booking)
                }

            else -> Unit
        }
    }
}

@Composable
private fun UnifiedBookingCard(
    booking: CustomerBookingUiModel,
    onClick: () -> Unit
) {
    val category = ServiceCatalog.category(booking.categoryId) ?: ServiceCatalog.category("other-event-services")
    val visual = category?.let(::categoryUiMeta)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = (visual?.accent ?: Maroon).copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = visual?.icon ?: Icons.Filled.Event,
                            contentDescription = null,
                            tint = visual?.accent ?: Maroon,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column {
                        Text(booking.categoryName, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        Text(booking.eventType, color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                CaterHubStatusChip(booking.status)
            }
            Text(formatBookingDate(booking.eventDate), fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(bookingTimeRange(booking.startTime, booking.endTime), color = TextDark)
            Text(bookingLocation(booking), color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                booking.services.take(3).joinToString(" • ").ifBlank { "Services selected" },
                color = TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (booking.quoteBased) "Price: To be quoted" else "Price: ₹${booking.totalAmount ?: 0}",
                color = if (booking.quoteBased) Green else Maroon,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("View details →", color = visual?.accent ?: Maroon, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UnifiedBookingDetailsCard(booking: CustomerBookingUiModel) {
    val category = ServiceCatalog.category(booking.categoryId) ?: ServiceCatalog.category("other-event-services")
    val visual = category?.let(::categoryUiMeta)
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(visual?.icon ?: Icons.Filled.Event, null, tint = visual?.accent ?: Maroon)
                    Text(category?.title ?: booking.categoryName, fontWeight = FontWeight.ExtraBold, color = TextDark)
                }
                CaterHubStatusChip(booking.status)
            }
            HorizontalDivider(color = Border)
            SummaryRow("Category", category?.title ?: booking.categoryName)
            SummaryRow("Event", booking.eventType)
            SummaryRow("Date", formatBookingDate(booking.eventDate))
            SummaryRow("Time", bookingTimeRange(booking.startTime, booking.endTime))
            SummaryRow("Location", bookingLocation(booking))
            Text("SERVICES", style = MaterialTheme.typography.labelSmall, color = Maroon, fontWeight = FontWeight.Bold)
            if (booking.services.isEmpty()) {
                Text("Not specified", color = Muted)
            } else {
                booking.services.forEach { Text(it, color = TextDark) }
            }
            SummaryRow("Price", if (booking.quoteBased) "To be quoted" else "₹${booking.totalAmount ?: 0}")
            SummaryRow("Status", booking.status.replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) })
            SummaryRow("Created", formatDateTime(booking.createdAt))
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

private fun formatBookingDate(raw: String): String =
    runCatching { LocalDate.parse(raw, BookingDateStorageFormatter).format(BookingDateDisplayFormatter) }
        .getOrDefault(raw)

private fun formatBookingTime(raw: String): String =
    runCatching { LocalTime.parse(raw, BookingTimeStorageFormatter).format(BookingTimeDisplayFormatter) }
        .getOrDefault(raw)

private fun formatBookingReviewTimeRange(raw: String): String {
    val display = formatBookingTime(raw)
    return if (display.isBlank() || display == raw && raw.isBlank()) "Time to be confirmed" else display
}

private fun bookingTimeRange(startTime: String, endTime: String?): String {
    val start = formatBookingTime(startTime)
    val end = endTime?.takeIf { it.isNotBlank() }?.let(::formatBookingTime)
    return if (end.isNullOrBlank()) start else "$start – $end"
}

private fun bookingLocation(booking: CustomerBookingUiModel): String {
    val areaText = booking.area?.trim().orEmpty()
    val addressText = booking.address.trim()
    return listOf(areaText, addressText).filter { it.isNotBlank() }.joinToString(", ")
}
