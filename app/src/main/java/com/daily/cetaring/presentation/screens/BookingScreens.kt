package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.daily.cetaring.domain.booking.BookingPriceDisplay
import com.daily.cetaring.domain.booking.CateringJourney
import com.daily.cetaring.presentation.components.BookingReviewHeader
import com.daily.cetaring.presentation.components.BookingChip
import com.daily.cetaring.presentation.components.BookingHeroBanner
import com.daily.cetaring.presentation.components.BookingImagery
import com.daily.cetaring.presentation.components.BookingLocationSection
import com.daily.cetaring.presentation.components.BookingReviewRow
import com.daily.cetaring.presentation.components.BookingReviewSection
import com.daily.cetaring.presentation.components.BookingSubheading
import com.daily.cetaring.presentation.components.BookingSuccessMark
import com.daily.cetaring.presentation.components.EventImageGrid
import com.daily.cetaring.presentation.components.GuestCountSelector
import com.daily.cetaring.presentation.components.ImageTileCard
import com.daily.cetaring.presentation.components.PackageCard
import com.daily.cetaring.presentation.components.PriceBreakdownCard
import com.daily.cetaring.presentation.components.PriceLine
import com.daily.cetaring.presentation.components.SelectionSummaryText
import com.daily.cetaring.presentation.components.StartTimeSlotPicker
import com.daily.cetaring.presentation.components.rememberReducedMotion
import java.text.NumberFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import com.daily.cetaring.presentation.components.EventDateSelector
import com.daily.cetaring.presentation.components.EventTimeSelector
import com.daily.cetaring.presentation.components.EventTypeGrid
import com.daily.cetaring.presentation.components.ScheduleMessage
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
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
import com.daily.cetaring.data.remote.dto.BookingValidator
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import com.daily.cetaring.domain.catalog.EventTypeCatalog
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
import com.daily.cetaring.presentation.components.BookingFlowTheme
import com.daily.cetaring.presentation.components.BookingFlowTopBar
import com.daily.cetaring.presentation.components.BookingIconTile
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
import com.daily.cetaring.ui.theme.CaterHubLightSystemBars

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

private val CateringDraftSaver: Saver<BookingDraft, Any> = listSaver(
    save = { CateringJourney.toSaveable(it) },
    restore = { CateringJourney.fromSaveable(it) ?: BookingDraft() }
)

private val IndianNumberFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN"))

internal fun formatRupees(amount: Long): String = "₹" + IndianNumberFormat.format(amount)

@OptIn(ExperimentalFoundationApi::class)
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
    var step by rememberSaveable { mutableIntStateOf(CateringJourney.STEP_EVENT) }
    // Set when the customer taps Edit on the review: Continue then returns straight to the review.
    var returnToReview by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val isSubmitting = uiState is BookingUiState.Loading
    val reducedMotion = rememberReducedMotion()

    // The ViewModel keeps the draft across the login round trip; this Bundle copy also restores it
    // after the process is killed in the background.
    var savedDraft by rememberSaveable(stateSaver = CateringDraftSaver) { mutableStateOf(draft) }
    var restoreChecked by remember { mutableStateOf(false) }
    LaunchedEffect(draft) {
        if (!restoreChecked) {
            restoreChecked = true
            if (draft == BookingDraft() && savedDraft != BookingDraft()) {
                viewModel.updateDraft { savedDraft }
                return@LaunchedEffect
            }
        }
        savedDraft = draft
    }

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

    fun goTo(target: Int) {
        showErrors = false
        step = target
    }

    BackHandler(enabled = step > CateringJourney.STEP_EVENT && !isSubmitting) {
        returnToReview = false
        goTo(step - 1)
    }

    BookingFlowTheme {
        CaterHubLightSystemBars()
        Scaffold(
            containerColor = BookingPalette.Background,
            topBar = { BookingFlowTopBar("Book catering", onBackClick) },
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                val estimate = CateringJourney.estimate(draft)
                BookingBottomActionBar(
                    summary = {
                        BottomBarSummary(
                            primary = when {
                                step == CateringJourney.STEP_REVIEW -> "Ready to submit"
                                returnToReview -> "Editing · returns to review"
                                else -> "Next: ${CateringJourney.stepTitles[step + 1]}"
                            },
                            secondary = when {
                                estimate != null -> "Est. ${formatRupees(estimate.total)}"
                                draft.cateringPlan == BookingOptions.planCustomMenu -> BookingPriceDisplay.PRICE_ON_QUOTE
                                else -> null
                            }
                        )
                    }
                ) {
                    BookingSecondaryButton(
                        text = if (step == CateringJourney.STEP_EVENT) "Cancel" else "Back",
                        onClick = {
                            returnToReview = false
                            if (step == CateringJourney.STEP_EVENT) onBackClick() else goTo(step - 1)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    )
                    BookingPrimaryButton(
                        text = when {
                            step == CateringJourney.STEP_REVIEW -> "Submit booking"
                            returnToReview -> "Save & review"
                            else -> "Continue"
                        },
                        loadingText = "Submitting…",
                        enabled = !isSubmitting,
                        loading = isSubmitting,
                        onClick = {
                            val validation = CateringJourney.validateUiStep(step, draft)
                            when {
                                validation is BookingValidationResult.Invalid -> {
                                    showErrors = true
                                    coroutineScope.launch { snackbar.showSnackbar(validation.message) }
                                }
                                step == CateringJourney.STEP_REVIEW -> viewModel.submitBooking()
                                returnToReview -> {
                                    returnToReview = false
                                    goTo(CateringJourney.STEP_REVIEW)
                                }
                                else -> goTo(step + 1)
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
                    stepTitles = CateringJourney.stepTitles,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
                )
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (reducedMotion) {
                            fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                        } else {
                            val forward = targetState > initialState
                            (fadeIn(tween(240, delayMillis = 60)) +
                                slideInHorizontally(tween(280)) { width -> if (forward) width / 8 else -width / 8 })
                                .togetherWith(fadeOut(tween(140)))
                        }
                    },
                    label = "bookingStep",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { shownStep ->
                    val scrollState = rememberScrollState()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (shownStep) {
                            CateringJourney.STEP_EVENT -> CateringEventStep(draft, viewModel, showErrors)
                            CateringJourney.STEP_FOOD_SERVICES -> CateringFoodServicesStep(draft, viewModel, showErrors)
                            CateringJourney.STEP_PACKAGE -> CateringPackageStep(draft, viewModel, showErrors)
                            CateringJourney.STEP_WHEN_WHERE -> CateringWhenWhereStep(draft, viewModel, showErrors)
                            else -> CateringReviewStep(draft, enabled = !isSubmitting) { target ->
                                returnToReview = true
                                goTo(target)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarSummary(primary: String, secondary: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            primary,
            color = BookingPalette.Muted,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (secondary != null) {
            Text(
                secondary,
                color = BookingPalette.Emerald,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CateringEventStep(draft: BookingDraft, viewModel: BookingViewModel, showErrors: Boolean) {
    val guestRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val reducedMotion = rememberReducedMotion()

    BookingStepHeading(
        title = "What are you celebrating?",
        subtitle = "Pick the occasion, then tell us how many guests to expect."
    )
    EventImageGrid(
        selectedBackendValue = draft.eventType,
        onSelect = { event ->
            val firstChoice = draft.eventType == null
            viewModel.updateDraft { it.copy(eventType = event.backendValue) }
            // Guide first-time customers to the next question.
            if (firstChoice && !reducedMotion) scope.launch { guestRequester.bringIntoView() }
        }
    )
    if (showErrors && draft.eventType.isNullOrBlank()) {
        ScheduleMessage("Please select an event type.", isError = true)
    }

    BookingCard(modifier = Modifier.bringIntoViewRequester(guestRequester)) {
        GuestCountSelector(
            guestCount = draft.guestCount,
            onGuestCountChange = { guests -> viewModel.updateDraft { CateringJourney.withGuests(it, guests) } },
            onIncrement = {
                viewModel.updateDraft { CateringJourney.withGuests(it, CateringJourney.incrementGuests(it.guestCount)) }
            },
            onDecrement = {
                viewModel.updateDraft { CateringJourney.withGuests(it, CateringJourney.decrementGuests(it.guestCount)) }
            }
        )
        if (showErrors) {
            val guests = BookingValidator.validateGuests(draft.guestCount)
            if (guests is BookingValidationResult.Invalid) ScheduleMessage(guests.message, isError = true)
        }
    }
}

@Composable
private fun CateringFoodServicesStep(draft: BookingDraft, viewModel: BookingViewModel, showErrors: Boolean) {
    BookingStepHeading(
        title = "What would you like to serve?",
        subtitle = "Select every meal you need — you can choose more than one."
    )
    val options = BookingOptions.cateringMealServiceOptions
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = responsiveColumns(maxWidth, wide = 3, regular = 2, compact = 1)
        SelectionGrid(options, columns, spacing = 12.dp) { option, itemModifier ->
            val selected = option.type in draft.selectedFoodServices
            ImageTileCard(
                title = option.displayName,
                description = option.description,
                visual = remember(option.type) {
                    BookingImagery.visual(BookingImagery.mealPhoto(option.type), seed = option.type.ordinal)
                },
                selected = selected,
                onClick = {
                    viewModel.updateDraft { current ->
                        val updated = current.selectedFoodServices.toMutableSet()
                        if (!updated.add(option.type)) updated.remove(option.type)
                        current.copy(selectedFoodServices = updated)
                    }
                },
                modifier = itemModifier
            )
        }
    }
    val labels = draft.selectedFoodServices.sortedBy { it.sortOrder }.map { it.displayName }
    if (showErrors && labels.isEmpty()) {
        ScheduleMessage("Please select at least one food service.", isError = true)
    } else {
        SelectionSummaryText(labels, emptyHint = "Tap a meal to add it to your event.")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CateringPackageStep(draft: BookingDraft, viewModel: BookingViewModel, showErrors: Boolean) {
    BookingStepHeading(
        title = "Choose your menu",
        subtitle = "Pick a package for ${draft.guestCount?.let { "$it guests" } ?: "your guests"} or build a custom menu."
    )

    val foodTypes = listOf(
        BookingOptions.foodTypeVegetarian to "Vegetarian",
        BookingOptions.foodTypeNonVegetarian to "Non-vegetarian",
        BookingOptions.foodTypeCustom to "Custom menu"
    )
    val selectedFoodType = draft.cateringFoodType.ifBlank { BookingOptions.foodTypeNonVegetarian }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.selectableGroup()
    ) {
        foodTypes.forEach { (type, label) ->
            BookingChip(
                text = label,
                selected = selectedFoodType == type,
                leadingIcon = when (type) {
                    BookingOptions.foodTypeVegetarian -> Icons.Filled.Eco
                    BookingOptions.foodTypeNonVegetarian -> Icons.Filled.Restaurant
                    else -> Icons.Filled.AutoAwesome
                },
                onClick = {
                    viewModel.updateDraft { current ->
                        val keepPlan = BookingOptions.planByBackendValue(current.cateringPlan)?.foodType == type
                        current.copy(
                            cateringFoodType = type,
                            cateringPlan = when {
                                keepPlan -> current.cateringPlan
                                type == BookingOptions.foodTypeCustom -> BookingOptions.planCustomMenu
                                else -> ""
                            }
                        )
                    }
                }
            )
        }
    }

    val plans = BookingOptions.plansByFoodType(selectedFoodType)
    val guests = draft.guestCount
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val effectiveWidth = maxWidth / LocalDensity.current.fontScale.coerceAtLeast(1f)
        val carousel = plans.size > 1 && effectiveWidth >= 300.dp
        val cardContent: @Composable (BookingOptions.CateringPlanOption, Modifier) -> Unit = { plan, cardModifier ->
            val total = plan.pricePerPerson?.let { price -> guests?.let { price.toLong() * it } }
            PackageCard(
                title = plan.title.lowercase(Locale.ROOT).split(' ', '-').joinToString(" ") { word ->
                    word.replaceFirstChar { it.titlecase(Locale.ROOT) }
                }.replace("Non Veg", "Non-Veg"),
                priceText = plan.pricePerPerson?.let { formatRupees(it.toLong()) } ?: BookingPriceDisplay.PRICE_ON_QUOTE,
                priceUnit = plan.pricePerPerson?.let { "person" },
                highlights = plan.menuItems,
                visual = remember(plan.backendValue) {
                    BookingImagery.visual(BookingImagery.packagePhoto(plan), seed = plan.backendValue.hashCode())
                },
                selected = draft.cateringPlan == plan.backendValue,
                onClick = { viewModel.updateDraft { it.copy(cateringPlan = plan.backendValue, cateringFoodType = plan.foodType) } },
                badge = if (plan.popular) "MOST POPULAR" else null,
                footnote = when {
                    total != null -> "Estimated ${formatRupees(total)} for $guests guests"
                    plan.pricePerPerson == null -> "Describe your dishes below and CaterHub will send a quote."
                    else -> null
                },
                modifier = cardModifier
            )
        }
        if (carousel) {
            val initialIndex = plans.indexOfFirst { it.backendValue == draft.cateringPlan }.coerceAtLeast(0)
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(listState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().selectableGroup()
            ) {
                items(plans, key = { it.backendValue }) { plan ->
                    cardContent(plan, Modifier.width(maxWidth * 0.84f))
                }
            }
        } else {
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                plans.forEach { plan -> cardContent(plan, Modifier.fillMaxWidth()) }
            }
        }
    }
    if (showErrors && draft.cateringPlan.isBlank()) {
        ScheduleMessage("Please choose a catering package.", isError = true)
    } else if (plans.size > 1) {
        Text("Swipe to compare packages", color = BookingPalette.Muted, style = MaterialTheme.typography.labelMedium)
    }

    val isCustom = draft.cateringPlan == BookingOptions.planCustomMenu
    BookingTextField(
        value = draft.foodRequirements,
        onValueChange = { value -> viewModel.updateDraft { it.copy(foodRequirements = value) } },
        label = if (isCustom) "Dishes for your custom menu" else "Dietary notes (optional)",
        placeholder = if (isCustom) "Example: Chicken Biryani, Paneer Curry, Gulab Jamun…" else "Spice level, Jain food, must-have dishes…",
        singleLine = false,
        minLines = if (isCustom) 3 else 2,
        isError = showErrors && isCustom && draft.foodRequirements.isBlank(),
        supportingText = if (showErrors && isCustom && draft.foodRequirements.isBlank()) "Tell us the dishes you would like." else null,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

@Composable
private fun CateringWhenWhereStep(draft: BookingDraft, viewModel: BookingViewModel, showErrors: Boolean) {
    BookingStepHeading(
        title = "When & where?",
        subtitle = "Choose the date, serving start time and venue."
    )

    val scheduleValidation = remember(draft.eventDate, draft.eventTime) { viewModel.validateStep(3) }
    val scheduleInvalid = scheduleValidation is BookingValidationResult.Invalid
    val showScheduleError = scheduleInvalid &&
        (showErrors || (draft.eventDate.isNotBlank() && draft.eventTime.isNotBlank()))

    BookingCard {
        BookingSubheading(
            "Event date",
            icon = Icons.Filled.CalendarMonth,
            trailing = draft.eventDate.takeIf { it.isNotBlank() }?.let(::formatBookingDate)
        )
        EventDateSelector(
            isoDate = draft.eventDate.takeIf { it.isNotBlank() },
            onDateSelected = { iso -> viewModel.updateDraft { it.copy(eventDate = iso) } },
            isError = showScheduleError && draft.eventDate.isBlank()
        )
        BookingSubheading(
            "Serving starts at",
            icon = Icons.Filled.Schedule,
            trailing = draft.eventTime.takeIf { it.isNotBlank() }?.let(::formatBookingTime)
        )
        StartTimeSlotPicker(
            time = draft.eventTime.takeIf { it.isNotBlank() },
            onTimeSelected = { time -> viewModel.updateDraft { it.copy(eventTime = time) } },
            isError = showScheduleError
        )
        if (showScheduleError) {
            ScheduleMessage((scheduleValidation as BookingValidationResult.Invalid).message, isError = true)
        }
    }

    BookingCard {
        BookingSubheading("Venue", icon = Icons.Filled.LocationOn)
        BookingLocationSection(
            address = draft.address,
            onAddressChange = { value -> viewModel.updateDraft { it.copy(address = value) } },
            area = draft.area,
            onAreaChange = { value -> viewModel.updateDraft { it.copy(area = value) } },
            city = draft.city,
            onCityChange = { value -> viewModel.updateDraft { it.copy(city = value) } },
            landmark = draft.landmark,
            onLandmarkChange = { value -> viewModel.updateDraft { it.copy(landmark = value) } },
            showErrors = showErrors
        )
    }

    BookingCard {
        BookingSubheading("Anything else?", icon = Icons.AutoMirrored.Filled.Notes)
        BookingTextField(
            value = draft.specialInstructions,
            onValueChange = { value -> viewModel.updateDraft { it.copy(specialInstructions = value) } },
            label = "Additional instructions (optional)",
            placeholder = "Serving setup, parking or access details…",
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

@Composable
private fun CateringReviewStep(draft: BookingDraft, enabled: Boolean, onEdit: (Int) -> Unit) {
    val event = EventTypeCatalog.eventTypes.firstOrNull { it.backendValue == draft.eventType }
    val occasion = EventTypeCatalog.displayNameForBackendValue(draft.eventType).ifBlank { null }
    BookingReviewHeader(
        visual = remember(event?.id) {
            BookingImagery.visual(BookingImagery.eventPhoto(event?.id ?: "other"))
        },
        title = occasion ?: "Your event",
        subtitle = "Event catering" + (BookingOptions.planByBackendValue(draft.cateringPlan)?.title?.let { " · $it" } ?: ""),
        facts = listOfNotNull(
            draft.eventDate.takeIf { it.isNotBlank() }?.let { Icons.Filled.CalendarMonth to formatBookingDate(it) },
            draft.guestCount?.let { Icons.Filled.Groups to "$it guests" },
            draft.eventTime.takeIf { it.isNotBlank() }?.let { Icons.Filled.Schedule to formatBookingReviewTimeRange(it) }
        )
    )

    BookingReviewSection("Event details", Icons.Filled.Event, { onEdit(CateringJourney.STEP_EVENT) }, enabled) {
        BookingReviewRow("Event type", occasion ?: "Not selected")
        BookingReviewRow("Guests", draft.guestCount?.let { "$it guests" } ?: "Not selected")
    }

    BookingReviewSection("Selected services", Icons.Filled.LunchDining, { onEdit(CateringJourney.STEP_FOOD_SERVICES) }, enabled) {
        BookingReviewRow("Service", "Event catering")
        BookingReviewRow("Meals", draft.selectedFoodServicesLabel())
    }

    val plan = BookingOptions.planByBackendValue(draft.cateringPlan)
    BookingReviewSection("Menu package", Icons.Filled.RestaurantMenu, { onEdit(CateringJourney.STEP_PACKAGE) }, enabled) {
        BookingReviewRow("Package", plan?.title ?: draft.cateringPlan.ifBlank { "Not selected" })
        BookingReviewRow(
            "Price",
            plan?.pricePerPerson?.let { "${formatRupees(it.toLong())} / person" } ?: BookingPriceDisplay.PRICE_ON_QUOTE
        )
        plan?.menuItems?.takeIf { plan.pricePerPerson != null }?.let { BookingReviewRow("Includes", it.joinToString(", ")) }
        if (draft.foodRequirements.isNotBlank()) {
            BookingReviewRow(if (plan?.pricePerPerson == null) "Your dishes" else "Dietary notes", draft.foodRequirements)
        }
    }

    BookingReviewSection("Date & time", Icons.Filled.CalendarMonth, { onEdit(CateringJourney.STEP_WHEN_WHERE) }, enabled) {
        BookingReviewRow("Date", draft.eventDate.takeIf { it.isNotBlank() }?.let(::formatBookingDate) ?: "Not selected")
        BookingReviewRow("Serving starts", formatBookingReviewTimeRange(draft.eventTime))
    }

    BookingReviewSection("Location", Icons.Filled.LocationOn, { onEdit(CateringJourney.STEP_WHEN_WHERE) }, enabled) {
        val venue = listOf(draft.address, draft.area, draft.city).map { it.trim() }.filter { it.isNotBlank() }.joinToString(", ")
        BookingReviewRow("Venue", venue.ifBlank { "Not provided" })
        if (draft.landmark.isNotBlank()) BookingReviewRow("Landmark", draft.landmark.trim())
        if (draft.specialInstructions.isNotBlank()) BookingReviewRow("Instructions", draft.specialInstructions)
    }

    val estimate = CateringJourney.estimate(draft)
    if (estimate != null) {
        PriceBreakdownCard(
            lines = listOf(
                PriceLine(
                    label = plan?.title ?: draft.cateringPlan,
                    detail = "${formatRupees(estimate.perPerson.toLong())} × ${estimate.guests} guests",
                    amount = formatRupees(estimate.total)
                )
            ),
            totalLabel = "Estimated total",
            totalValue = BookingPriceDisplay.cateringTotal(draft) { formatRupees(it) },
            note = "Based on the published package price. CaterHub confirms the final quote after reviewing your request. Taxes and delivery are not included in this estimate."
        )
    } else {
        PriceBreakdownCard(
            lines = listOf(
                PriceLine(
                    plan?.title ?: draft.cateringPlan.ifBlank { "Custom menu" },
                    draft.guestCount?.let { "$it guests" },
                    BookingPriceDisplay.PRICE_ON_QUOTE
                )
            ),
            totalLabel = "Total",
            totalValue = BookingPriceDisplay.PRICE_ON_QUOTE,
            note = "CaterHub will review your menu and confirm the price before the booking is finalised."
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
    val booking = (uiState as? BookingUiState.Submitted)?.booking

    BookingFlowTheme {
        CaterHubLightSystemBars()
        Column(
            Modifier
                .fillMaxSize()
                .background(BookingPalette.Background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            BookingSuccessMark()
            Text(
                "Booking request submitted",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BookingPalette.Ink,
                textAlign = TextAlign.Center
            )
            Text(
                "Your request has been sent to CaterHub. We'll confirm the details and final quote shortly.",
                color = BookingPalette.Muted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            if (booking != null) {
                BookingSummaryCard(booking)
            }
            Spacer(Modifier.height(8.dp))
            BookingPrimaryButton("View booking", { onViewBooking(bookingId) }, Modifier.fillMaxWidth())
            BookingSecondaryButton("Back to Home", onHome, Modifier.fillMaxWidth())
        }
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
                if (booking.quoteBased || booking.totalAmount == null) "Price on quote" else "Price: ₹${booking.totalAmount}",
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
            SummaryRow("Price", if (booking.quoteBased || booking.totalAmount == null) BookingPriceDisplay.PRICE_ON_QUOTE else "₹${booking.totalAmount}")
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
