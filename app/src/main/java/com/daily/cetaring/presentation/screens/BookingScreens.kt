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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.daily.cetaring.presentation.components.BookingBottomActionBar
import com.daily.cetaring.presentation.components.BookingCard
import com.daily.cetaring.presentation.components.BookingCardShape
import com.daily.cetaring.presentation.components.BookingDatePickerDialog
import com.daily.cetaring.presentation.components.BookingDateRules
import com.daily.cetaring.presentation.components.BookingFlowTheme
import com.daily.cetaring.presentation.components.BookingFlowTopBar
import com.daily.cetaring.presentation.components.BookingIconTile
import com.daily.cetaring.presentation.components.BookingLightSystemBars
import com.daily.cetaring.presentation.components.BookingPalette
import com.daily.cetaring.presentation.components.BookingPickerField
import com.daily.cetaring.presentation.components.BookingPrimaryButton
import com.daily.cetaring.presentation.components.BookingSecondaryButton
import com.daily.cetaring.presentation.components.BookingSectionTitle
import com.daily.cetaring.presentation.components.BookingSelectableCard
import com.daily.cetaring.presentation.components.BookingSelectionIndicator
import com.daily.cetaring.presentation.components.BookingSelectionMode
import com.daily.cetaring.presentation.components.BookingStepHeading
import com.daily.cetaring.presentation.components.BookingStepProgress
import com.daily.cetaring.presentation.components.BookingTextField
import com.daily.cetaring.presentation.components.BookingTimePickerDialog

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

private val BookingFlowStepTitles = listOf(
    "Event & guests",
    "Food services",
    "Plan & location",
    "Date & time",
    "Review"
)

@Composable
fun BookingFlowScreen(
    viewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onSubmitted: (Long) -> Unit,
    onAuthRequired: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val isSubmitting = uiState is BookingUiState.Loading
    val currentStepValidation = remember(step, draft) {
        if (step == BookingFlowReviewStepIndex) BookingValidationResult.Valid else viewModel.validateStep(step)
    }
    val canContinue = currentStepValidation is BookingValidationResult.Valid && !isSubmitting

    LaunchedEffect(step) { scrollState.scrollTo(0) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BookingUiState.Error -> snackbar.showSnackbar(state.message)
            is BookingUiState.Submitted -> onSubmitted(state.booking.id)
            is BookingUiState.AuthRequired -> {
                snackbar.showSnackbar(state.message)
                viewModel.markAuthPromptHandled()
                onAuthRequired()
            }
            else -> Unit
        }
    }

    BookingFlowTheme {
        BookingLightSystemBars()
        Scaffold(
            containerColor = BookingPalette.Background,
            topBar = { BookingFlowTopBar("Book catering", onBackClick) },
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                BookingBottomActionBar {
                    BookingSecondaryButton(
                        text = if (step == 0) "Cancel" else "Back",
                        onClick = { if (step == 0) onBackClick() else step-- },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    )
                    BookingPrimaryButton(
                        text = if (step == BookingFlowReviewStepIndex) "Submit Booking" else "Continue",
                        loadingText = "Submitting…",
                        enabled = canContinue,
                        loading = isSubmitting,
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
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                BookingStepProgress(
                    stepIndex = step,
                    stepTitles = BookingFlowStepTitles,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        0 -> EventStep(draft, viewModel)
                        1 -> FoodServiceSelectionStep(draft, viewModel)
                        2 -> CateringPlanAndLocationStep(draft, viewModel)
                        3 -> DateTimeStep(draft, viewModel)
                        else -> ReviewStep(draft, enabled = !isSubmitting) { step = it }
                    }
                }
            }
        }
    }
}

/** Number of columns for selection grids, reduced on narrow screens or with large font scaling. */
@Composable
private fun responsiveColumns(maxWidth: Dp, wide: Int, regular: Int, compact: Int): Int {
    val fontScale = LocalDensity.current.fontScale
    val effectiveWidth = maxWidth / fontScale.coerceAtLeast(1f)
    return when {
        effectiveWidth >= 560.dp -> wide
        effectiveWidth >= 280.dp -> regular
        else -> compact
    }
}

@Composable
private fun <T> SelectionGrid(
    items: List<T>,
    columns: Int,
    spacing: Dp = 10.dp,
    itemContent: @Composable (T, Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowItems.forEach { item ->
                    itemContent(item, Modifier.weight(1f).fillMaxHeight())
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun eventTypeIcon(event: EventTypeDefinition): ImageVector = when (event.id) {
    "birthday" -> Icons.Filled.Cake
    "wedding", "engagement" -> Icons.Filled.Favorite
    "reception" -> Icons.Filled.Celebration
    "anniversary" -> Icons.Filled.Favorite
    "baby_shower", "naming_ceremony" -> Icons.Filled.ChildCare
    "housewarming" -> Icons.Filled.Home
    "pooja" -> Icons.Filled.TempleHindu
    "corporate" -> Icons.Filled.BusinessCenter
    "school_college" -> Icons.Filled.School
    "festival" -> Icons.Filled.AutoAwesome
    "party" -> Icons.Filled.Celebration
    else -> Icons.Filled.Event
}

@Composable
private fun EventStep(draft: BookingDraft, viewModel: BookingViewModel) {
    BookingStepHeading(
        title = "Tell us about your event",
        subtitle = "Choose the occasion and how many guests you expect."
    )

    BookingCard {
        BookingSectionTitle(
            "Event type",
            Icons.Filled.Event,
            trailing = draft.eventType?.let { selected ->
                {
                    Text(
                        EventTypeCatalog.displayNameForBackendValue(selected),
                        color = BookingPalette.Burgundy,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp).widthIn(max = 160.dp)
                    )
                }
            }
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = responsiveColumns(maxWidth, wide = 3, regular = 2, compact = 1)
            SelectionGrid(EventTypeCatalog.eventTypes, columns) { event, itemModifier ->
                val selected = draft.eventType == event.backendValue
                BookingSelectableCard(
                    selected = selected,
                    onClick = { viewModel.updateDraft { it.copy(eventType = event.backendValue) } },
                    modifier = itemModifier,
                    contentPadding = 12.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookingIconTile(eventTypeIcon(event), selected, size = 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            event.displayName,
                            color = BookingPalette.Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    BookingCard {
        BookingSectionTitle(
            "Guest count",
            Icons.Filled.Groups,
            trailing = draft.guestCount?.let { count ->
                {
                    Text(
                        "$count guests",
                        color = BookingPalette.Burgundy,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        )
        val customOption = -1
        val guestOptions = BookingOptions.guestQuickOptions + customOption
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = responsiveColumns(maxWidth, wide = 6, regular = 3, compact = 2)
            SelectionGrid(guestOptions, columns, spacing = 8.dp) { guests, itemModifier ->
                val isCustom = guests == customOption
                val selected = if (isCustom) {
                    draft.guestCountSelection == BookingOptions.guestSelectionCustom
                } else {
                    draft.guestCountSelection == BookingOptions.guestSelectionPreset && draft.guestCount == guests
                }
                BookingSelectableCard(
                    selected = selected,
                    onClick = {
                        viewModel.updateDraft {
                            if (isCustom) {
                                it.copy(
                                    guestCountSelection = BookingOptions.guestSelectionCustom,
                                    guestCount = if (it.customGuestCountInput.isBlank()) null else it.guestCount
                                )
                            } else {
                                it.copy(
                                    guestCount = guests,
                                    guestCountSelection = BookingOptions.guestSelectionPreset,
                                    customGuestCountInput = ""
                                )
                            }
                        }
                    },
                    modifier = itemModifier,
                    contentPadding = 10.dp
                ) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (isCustom) "Custom" else guests.toString(),
                            color = if (selected) BookingPalette.Burgundy else BookingPalette.Ink,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            if (isCustom) "Enter count" else "guests",
                            color = BookingPalette.Muted,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (draft.guestCountSelection == BookingOptions.guestSelectionCustom) {
            val invalidCustomGuestCount = draft.customGuestCountInput.isNotBlank() && draft.guestCount == null
            BookingTextField(
                value = draft.customGuestCountInput,
                onValueChange = { value ->
                    if (value.length > BookingOptions.maxGuestInputLength) return@BookingTextField
                    if (value.isNotEmpty() && !value.all(Char::isDigit)) return@BookingTextField
                    val parsed = value.toIntOrNull()?.takeIf { it in 1..BookingOptions.maxGuestCount }
                    viewModel.updateDraft {
                        it.copy(
                            guestCountSelection = BookingOptions.guestSelectionCustom,
                            customGuestCountInput = value,
                            guestCount = parsed
                        )
                    }
                },
                label = "Number of guests",
                placeholder = "Enter number of guests",
                supportingText = if (invalidCustomGuestCount) CustomGuestValidationMessage else "Between 1 and 2,000 guests",
                isError = invalidCustomGuestCount,
                leadingIcon = Icons.Filled.Groups,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
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
private fun FoodServiceSelectionStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    BookingStepHeading(
        title = "What would you like to serve?",
        subtitle = "Select one or more food services for your event."
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BookingOptions.cateringMealServiceOptions.forEach { option ->
            val selected = option.type in draft.selectedFoodServices
            BookingSelectableCard(
                selected = selected,
                mode = BookingSelectionMode.Multiple,
                onClick = {
                    viewModel.updateDraft { current ->
                        val updated = current.selectedFoodServices.toMutableSet()
                        if (!updated.add(option.type)) {
                            updated.remove(option.type)
                        }
                        current.copy(selectedFoodServices = updated)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookingIconTile(mealServiceIcon(option.type), selected, size = 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            option.displayName,
                            color = BookingPalette.Ink,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            option.description,
                            color = BookingPalette.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    BookingSelectionIndicator(selected = selected)
                }
            }
        }
    }

    val selectedServices = draft.selectedFoodServices
        .sortedBy { it.sortOrder }
        .map { it.backendLabel }
    val count = selectedServices.size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BookingCardShape,
        color = if (count > 0) BookingPalette.GreenSoft else BookingPalette.AmberSoft,
        border = BorderStroke(1.dp, if (count > 0) BookingPalette.Green.copy(alpha = 0.25f) else BookingPalette.Border)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (count > 0) Icons.Filled.CheckCircle else Icons.Filled.Info,
                contentDescription = null,
                tint = if (count > 0) BookingPalette.Green else BookingPalette.Amber
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    when (count) {
                        0 -> "No services selected"
                        1 -> "1 service selected"
                        else -> "$count services selected"
                    },
                    color = BookingPalette.Ink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (count == 0) "Select at least one service to continue."
                    else selectedServices.joinToString(" • "),
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CateringPlanAndLocationStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    BookingStepHeading(
        title = "Choose your catering plan",
        subtitle = "Pick a vegetarian or non-vegetarian package, or build a custom menu."
    )

    val selectedFoodType = draft.cateringFoodType.ifBlank { BookingOptions.foodTypeNonVegetarian }
    val visiblePlans = BookingOptions.plansByFoodType(selectedFoodType)
    val foodTypes = listOf(
        Triple(BookingOptions.foodTypeVegetarian, "Vegetarian", Icons.Filled.Eco),
        Triple(BookingOptions.foodTypeNonVegetarian, "Non-Veg", Icons.Filled.Restaurant),
        Triple(BookingOptions.foodTypeCustom, "Custom", Icons.Filled.AutoAwesome)
    )

    BookingCard {
        BookingSectionTitle("Food preference", Icons.Filled.RestaurantMenu)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = responsiveColumns(maxWidth, wide = 3, regular = 3, compact = 1)
            SelectionGrid(foodTypes, columns, spacing = 8.dp) { (type, label, icon), itemModifier ->
                val selected = selectedFoodType == type
                BookingSelectableCard(
                    selected = selected,
                    onClick = {
                        val defaultPlan = BookingOptions.defaultPlanForFoodType(type)
                        viewModel.updateDraft {
                            it.copy(
                                cateringFoodType = type,
                                cateringPlan = defaultPlan.backendValue
                            )
                        }
                    },
                    modifier = itemModifier,
                    contentPadding = 10.dp
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (selected) BookingPalette.Burgundy else BookingPalette.Green,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            label,
                            color = BookingPalette.Ink,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        visiblePlans.forEach { plan ->
            PlanCard(
                option = plan,
                selected = draft.cateringPlan == plan.backendValue
            ) {
                viewModel.updateDraft { it.copy(cateringPlan = plan.backendValue) }
            }
        }
    }

    if (draft.cateringPlan == BookingOptions.planCustomMenu) {
        BookingTextField(
            value = draft.foodRequirements,
            onValueChange = { viewModel.updateDraft { current -> current.copy(foodRequirements = it) } },
            label = "Your menu requirements",
            placeholder = "Example: Chicken Biryani, Paneer Curry, Gulab Jamun…",
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }

    BookingCard {
        BookingSectionTitle("Event location", Icons.Filled.LocationOn)
        BookingTextField(
            value = draft.address,
            onValueChange = { value -> viewModel.updateDraft { it.copy(address = value) } },
            label = "Event address",
            placeholder = "House / building, street",
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        BookingTextField(
            value = draft.area,
            onValueChange = { value -> viewModel.updateDraft { it.copy(area = value) } },
            label = "Area / locality",
            placeholder = "Enter area or locality",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        BookingTextField(
            value = draft.city,
            onValueChange = { value -> viewModel.updateDraft { it.copy(city = value) } },
            label = "City",
            placeholder = "Enter city",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        BookingTextField(
            value = draft.landmark,
            onValueChange = { value -> viewModel.updateDraft { it.copy(landmark = value) } },
            label = "Landmark (optional)",
            placeholder = "Nearby landmark",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun PlanCard(
    option: BookingOptions.CateringPlanOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    BookingSelectableCard(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 16.dp
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            BookingSelectionIndicator(selected = selected, radio = true, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FlowRowCompat {
                    Text(
                        option.title,
                        color = BookingPalette.Ink,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (option.popular) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = BookingPalette.AmberSoft
                        ) {
                            Text(
                                "POPULAR",
                                color = BookingPalette.Amber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    option.pricePerPerson?.let { "₹$it / person" } ?: "Price on request",
                    color = if (selected) BookingPalette.Burgundy else BookingPalette.Green,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                option.description?.let {
                    Text(it, color = BookingPalette.Ink, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    option.menuItems.joinToString(" • "),
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(
    draft: BookingDraft,
    viewModel: BookingViewModel
) {
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }

    BookingStepHeading(
        title = "When is your event?",
        subtitle = "Choose the event date and start time, and share any food preferences."
    )

    BookingCard {
        BookingSectionTitle("Date & time", Icons.Filled.CalendarMonth)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val sideBySide = responsiveColumns(maxWidth, wide = 2, regular = 1, compact = 1) > 1
            val dateField = @Composable { modifier: Modifier ->
                BookingPickerField(
                    label = "Event date",
                    value = draft.eventDate.takeIf { it.isNotBlank() }?.let(::formatBookingDate),
                    placeholder = "Select date",
                    icon = Icons.Filled.CalendarMonth,
                    onClick = { showDate = true },
                    modifier = modifier
                )
            }
            val timeField = @Composable { modifier: Modifier ->
                BookingPickerField(
                    label = "Start time",
                    value = draft.eventTime.takeIf { it.isNotBlank() }?.let(::formatBookingTime),
                    placeholder = "Select time",
                    icon = Icons.Filled.Schedule,
                    onClick = { showTime = true },
                    modifier = modifier
                )
            }
            if (sideBySide) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    dateField(Modifier.weight(1f))
                    timeField(Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    dateField(Modifier.fillMaxWidth())
                    timeField(Modifier.fillMaxWidth())
                }
            }
        }
    }

    BookingCard {
        BookingSectionTitle("Requirements", Icons.AutoMirrored.Filled.Notes)
        BookingTextField(
            value = draft.foodRequirements,
            onValueChange = { value -> viewModel.updateDraft { it.copy(foodRequirements = value) } },
            label = "Food requirements (optional)",
            placeholder = "Dietary needs, spice level, must-have dishes…",
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        BookingTextField(
            value = draft.specialInstructions,
            onValueChange = { value -> viewModel.updateDraft { it.copy(specialInstructions = value) } },
            label = "Additional instructions (optional)",
            placeholder = "Serving time, setup, parking or access details…",
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }

    if (showDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = BookingDateRules.utcMillisForIsoDate(draft.eventDate),
            selectableDates = remember {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        BookingDateRules.isSelectableDate(utcTimeMillis)

                    override fun isSelectableYear(year: Int): Boolean =
                        BookingDateRules.isSelectableYear(year)
                }
            }
        )
        BookingDatePickerDialog(
            state = dateState,
            onDismiss = { showDate = false },
            onConfirm = { millis ->
                viewModel.updateDraft { it.copy(eventDate = BookingDateRules.isoDateForUtcMillis(millis)) }
                showDate = false
            }
        )
    }

    if (showTime) {
        val initialTime = draft.eventTime.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalTime.parse(it, BookingTimeStorageFormatter) }.getOrNull()
        }
        val timeState = rememberTimePickerState(
            initialHour = initialTime?.hour ?: 12,
            initialMinute = initialTime?.minute ?: 0,
            is24Hour = false
        )
        BookingTimePickerDialog(
            state = timeState,
            onDismiss = { showTime = false },
            onConfirm = {
                viewModel.updateDraft {
                    it.copy(eventTime = "%02d:%02d".format(Locale.ENGLISH, timeState.hour, timeState.minute))
                }
                showTime = false
            }
        )
    }
}

@Composable
private fun ReviewStep(draft: BookingDraft, enabled: Boolean, onEdit: (Int) -> Unit) {
    BookingStepHeading(
        title = "Review your booking",
        subtitle = "Check the details below before submitting your request."
    )

    ReviewSection("Event", Icons.Filled.Event, editStep = 0, enabled = enabled, onEdit = onEdit) {
        ReviewDetail("Event type", EventTypeCatalog.displayNameForBackendValue(draft.eventType).ifBlank { "Not selected" })
        ReviewDetail("Guests", draft.guestCount?.let { "$it guests" } ?: "Not selected")
    }

    ReviewSection("Food services", Icons.Filled.LunchDining, editStep = 1, enabled = enabled, onEdit = onEdit) {
        ReviewDetail("Services", draft.selectedFoodServicesLabel())
    }

    val plan = BookingOptions.planByBackendValue(draft.cateringPlan)
    ReviewSection("Plan & location", Icons.Filled.RestaurantMenu, editStep = 2, enabled = enabled, onEdit = onEdit) {
        ReviewDetail("Catering plan", draft.cateringPlan.ifBlank { BookingOptions.fullCatering })
        plan?.pricePerPerson?.let { ReviewDetail("Plan price", "₹$it / person") }
        ReviewDetail("Location", draft.deliveryAddress().ifBlank { "Not provided" })
    }

    ReviewSection("Date & requirements", Icons.Filled.CalendarMonth, editStep = 3, enabled = enabled, onEdit = onEdit) {
        ReviewDetail("Date", draft.eventDate.takeIf { it.isNotBlank() }?.let(::formatBookingDate) ?: "Not selected")
        ReviewDetail("Start time", formatBookingReviewTimeRange(draft.eventTime))
        ReviewDetail("Food requirements", draft.foodRequirements.ifBlank { "Standard plan menu" })
        if (draft.specialInstructions.isNotBlank()) {
            ReviewDetail("Additional instructions", draft.specialInstructions)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BookingCardShape,
        color = BookingPalette.GreenSoft,
        border = BorderStroke(1.dp, BookingPalette.Green.copy(alpha = 0.25f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = BookingPalette.Green)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Total", color = BookingPalette.Muted, style = MaterialTheme.typography.labelMedium)
                Text(
                    "To be quoted",
                    color = BookingPalette.Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "CaterHub will confirm the final quote for your request.",
                    color = BookingPalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    icon: ImageVector,
    editStep: Int,
    enabled: Boolean,
    onEdit: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    BookingCard {
        BookingSectionTitle(
            title,
            icon,
            trailing = {
                TextButton(
                    onClick = { onEdit(editStep) },
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = BookingPalette.Burgundy)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics {
                        contentDescription = "Edit $title"
                    })
                }
            }
        )
        HorizontalDivider(color = BookingPalette.Divider)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
private fun ReviewDetail(label: String, value: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = BookingPalette.Muted, style = MaterialTheme.typography.labelMedium)
        Text(value, color = BookingPalette.Ink, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
