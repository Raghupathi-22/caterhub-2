@file:OptIn(ExperimentalMaterial3Api::class)

package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.CateringStaffLineItem
import com.daily.cetaring.data.remote.dto.CreateCateringStaffBookingRequest
import com.daily.cetaring.data.remote.dto.ServiceRequestRequest
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.domain.booking.BookingPriceDisplay
import com.daily.cetaring.domain.booking.ServiceJourney
import com.daily.cetaring.presentation.components.BookingReviewHeader
import com.daily.cetaring.domain.booking.ServiceJourneyCatalog
import com.daily.cetaring.domain.booking.ServiceJourneyStep
import com.daily.cetaring.domain.booking.ServiceRequestCatalog
import com.daily.cetaring.domain.booking.ServiceRequestDraft
import com.daily.cetaring.domain.booking.ServiceRequestLine
import com.daily.cetaring.domain.booking.ServiceRequestValidator
import com.daily.cetaring.domain.catalog.EventTypeCatalog
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.BookingBottomActionBar
import com.daily.cetaring.presentation.components.BookingCard
import com.daily.cetaring.presentation.components.BookingFlowTheme
import com.daily.cetaring.presentation.components.BookingFlowTopBar
import com.daily.cetaring.presentation.components.BookingHeroBanner
import com.daily.cetaring.presentation.components.BookingImagery
import com.daily.cetaring.presentation.components.BookingLocationSection
import com.daily.cetaring.presentation.components.BookingPalette
import com.daily.cetaring.presentation.components.BookingPrimaryButton
import com.daily.cetaring.presentation.components.BookingReviewRow
import com.daily.cetaring.presentation.components.BookingReviewSection
import com.daily.cetaring.presentation.components.BookingSecondaryButton
import com.daily.cetaring.presentation.components.BookingStepHeading
import com.daily.cetaring.presentation.components.BookingStepProgress
import com.daily.cetaring.presentation.components.BookingSubheading
import com.daily.cetaring.presentation.components.BookingSuccessMark
import com.daily.cetaring.presentation.components.BookingTextField
import com.daily.cetaring.presentation.components.EndTimeDurationPicker
import com.daily.cetaring.presentation.components.EventDateSelector
import com.daily.cetaring.presentation.components.EventImageGrid
import com.daily.cetaring.presentation.components.EventTimeRules
import com.daily.cetaring.presentation.components.ImageRowCard
import com.daily.cetaring.presentation.components.PriceBreakdownCard
import com.daily.cetaring.presentation.components.PriceLine
import com.daily.cetaring.presentation.components.QuantityStepper
import com.daily.cetaring.presentation.components.ScheduleMessage
import com.daily.cetaring.presentation.components.SelectToggleButton
import com.daily.cetaring.presentation.components.SelectionSummaryText
import com.daily.cetaring.presentation.components.ServiceImagery
import com.daily.cetaring.presentation.components.StartTimeSlotPicker
import com.daily.cetaring.presentation.components.rememberReducedMotion
import com.daily.cetaring.ui.theme.CaterHubLightSystemBars
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateReviewFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
private val TimeDisplayFormatter = DateTimeFormatter.ofPattern("h:mm a")

internal data class SubmittedServiceRequestSummary(
    val categoryTitle: String,
    val eventType: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val reference: String = "",
    val totalLabel: String = ""
)

internal fun bookingReferenceFor(id: Long): String = "CH-SR-%06d".format(id)

// Mirror the backend column/DTO limits so customers never hit a server-side length error.
internal const val EVENT_ADDRESS_MAX_LENGTH = ServiceRequestDraft.ADDRESS_MAX
internal const val EVENT_AREA_MAX_LENGTH = ServiceRequestDraft.AREA_MAX
internal const val EVENT_NOTES_MAX_LENGTH = ServiceRequestDraft.NOTES_MAX

/** Idempotency keys must match the backend pattern [A-Za-z0-9_-]{1,64}. */
internal fun newBookingIdempotencyKey(): String = java.util.UUID.randomUUID().toString()

// The summary is held in rememberSaveable, so it must be Bundle-compatible. Without this
// Saver, leaving the success screen (Back to Home, View My Bookings, system Back) disposes the
// destination, SaveableStateHolder calls performSave(), and Compose throws IllegalStateException
// because a plain data class cannot be stored in a Bundle.
internal val SubmittedServiceRequestSummarySaver: Saver<SubmittedServiceRequestSummary?, Any> =
    listSaver(
        save = { summary ->
            if (summary == null) {
                emptyList()
            } else {
                listOf(
                    summary.categoryTitle,
                    summary.eventType,
                    summary.eventDate,
                    summary.startTime,
                    summary.endTime,
                    summary.location,
                    summary.reference,
                    summary.totalLabel
                )
            }
        },
        restore = { values ->
            if (values.size == 6 || values.size == 8) {
                SubmittedServiceRequestSummary(
                    categoryTitle = values[0],
                    eventType = values[1],
                    eventDate = values[2],
                    startTime = values[3],
                    endTime = values[4],
                    location = values[5],
                    reference = values.getOrElse(6) { "" },
                    totalLabel = values.getOrElse(7) { "" }
                )
            } else {
                null
            }
        }
    )

/** Keeps the whole request (including selected quantities) across login and process death. */
private val ServiceRequestDraftSaver: Saver<ServiceRequestDraft, Any> = listSaver(
    save = { ServiceRequestDraft.toSaveable(it) },
    restore = { ServiceRequestDraft.fromSaveable(it) ?: ServiceRequestDraft() }
)

private data class ServiceCategoryInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val serviceType: String
)

private fun categoryInfoFor(type: String): ServiceCategoryInfo {
    val category = ServiceCatalog.category(type) ?: ServiceCatalog.category("event-support")!!
    return ServiceCategoryInfo(
        id = category.id,
        title = "Book ${category.title}",
        subtitle = category.subtitle,
        serviceType = category.serviceType
    )
}

private fun rupees(amount: Int): String = formatRupees(amount.toLong())

@Composable
fun ServiceRequestScreen(
    serviceType: String,
    workerRepository: WorkerRepository,
    onBackClick: () -> Unit,
    preselectedServiceId: String? = null,
    isAuthenticated: Boolean = true,
    onAuthRequired: () -> Unit = {},
    resumeSubmissionAfterAuth: Boolean = false,
    onResumeSubmissionHandled: () -> Unit = {},
    onSubmitted: () -> Unit = {},
    onViewMyBookings: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val category = remember(serviceType) { categoryInfoFor(serviceType) }
    val journey = remember(category.id) { ServiceJourneyCatalog.forCategory(category.id) }
    val lines = remember(category.id) { ServiceRequestCatalog.linesFor(category.id) }

    var draft by rememberSaveable(serviceType, stateSaver = ServiceRequestDraftSaver) {
        mutableStateOf(ServiceRequestDraft.initial(lines, preselectedServiceId))
    }
    var step by rememberSaveable(serviceType) { mutableIntStateOf(0) }
    var returnToReview by rememberSaveable(serviceType) { mutableStateOf(false) }
    var showErrors by rememberSaveable(serviceType) { mutableStateOf(false) }
    var submitting by rememberSaveable(serviceType) { mutableStateOf(false) }
    var error by rememberSaveable(serviceType) { mutableStateOf<String?>(null) }
    var idempotencyKey by rememberSaveable(serviceType) { mutableStateOf<String?>(null) }
    var submittedSummary by rememberSaveable(serviceType, stateSaver = SubmittedServiceRequestSummarySaver) {
        mutableStateOf<SubmittedServiceRequestSummary?>(null)
    }
    val scope = rememberCoroutineScope()
    val reducedMotion = rememberReducedMotion()
    val currentStep = journey.steps[step.coerceIn(0, journey.steps.lastIndex)]
    val isReview = currentStep == ServiceJourneyStep.REVIEW

    fun updateDraft(transform: (ServiceRequestDraft) -> ServiceRequestDraft) {
        draft = transform(draft)
        error = null
    }

    fun goTo(target: Int) {
        showErrors = false
        error = null
        step = target
    }

    fun submitRequest() {
        if (submitting) return
        // One key per confirmed review: retries after timeouts/5xx reuse it so the server returns the
        // original booking instead of creating a duplicate.
        val key = idempotencyKey ?: newBookingIdempotencyKey().also { idempotencyKey = it }
        // Set before launching so a rapid double tap cannot start a second request.
        submitting = true
        error = null
        val snapshot = draft
        scope.launch {
            try {
                val confirmedEventType = snapshot.eventType ?: run {
                    error = "Please choose the event type."
                    step = journey.indexOf(ServiceJourneyStep.EVENT)
                    return@launch
                }
                val selectedLines = snapshot.selectedLines(lines)
                val fixedTotal = snapshot.fixedTotal(lines)
                val hasQuoteServices = snapshot.hasQuoteServices(lines)
                val staffingLines = selectedLines.mapNotNull { line ->
                    line.workerType?.let { workerType ->
                        CateringStaffLineItem(
                            workerType = workerType,
                            requiredWorkers = snapshot.quantityOf(line.id),
                            payment = BigDecimal((line.unitPrice ?: 0).coerceAtLeast(1))
                        )
                    }
                }
                val selectedNames = selectedLines.map { it.name }
                val details = selectedLines.joinToString("; ") { line ->
                    val qty = snapshot.quantityOf(line.id)
                    if (line.quoteOnly) "${line.name}: selected x$qty (quote)" else "${line.name}: $qty x ₹${line.unitPrice}"
                }
                val address = snapshot.address.trim()
                val area = snapshot.area.trim()
                val instructions = snapshot.notes.trim().ifBlank { null }

                // Both flows are atomic on the backend: the customer request and its worker jobs are
                // created in one transaction, so workers never see a job for a failed booking.
                val response = if (category.id == "catering-staff") {
                    workerRepository.createCateringStaffBooking(
                        CreateCateringStaffBookingRequest(
                            eventType = confirmedEventType,
                            eventDate = snapshot.eventDate,
                            startTime = snapshot.startTime,
                            endTime = snapshot.endTime,
                            location = address,
                            area = area,
                            selectedServices = selectedNames,
                            instructions = instructions,
                            details = "Catering staff: $details",
                            totalAmount = BigDecimal(fixedTotal),
                            staffing = staffingLines
                        ),
                        idempotencyKey = key
                    )
                } else {
                    workerRepository.createServiceRequest(
                        ServiceRequestRequest(
                            serviceType = category.serviceType,
                            eventType = confirmedEventType,
                            eventDate = snapshot.eventDate,
                            startTime = snapshot.startTime,
                            endTime = snapshot.endTime,
                            location = address,
                            area = area,
                            selectedServices = selectedNames,
                            instructions = instructions,
                            details = "Services: $details",
                            quoteBased = hasQuoteServices,
                            totalAmount = BigDecimal(fixedTotal),
                            staffing = staffingLines.ifEmpty { null }
                        ),
                        idempotencyKey = key
                    )
                }
                val serverTotal = response.totalAmount.stripTrailingZeros().toPlainString()
                submittedSummary = SubmittedServiceRequestSummary(
                    categoryTitle = category.title.removePrefix("Book "),
                    eventType = confirmedEventType,
                    eventDate = snapshot.eventDate,
                    startTime = snapshot.startTime,
                    endTime = snapshot.endTime,
                    location = "$area, $address",
                    reference = bookingReferenceFor(response.id),
                    totalLabel = when {
                        hasQuoteServices && response.totalAmount.signum() > 0 -> "₹$serverTotal + quote"
                        hasQuoteServices -> BookingPriceDisplay.PRICE_ON_QUOTE
                        else -> "₹$serverTotal"
                    }
                )
                idempotencyKey = null
                onSubmitted()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.message ?: "Unable to submit request. Please try again."
            } finally {
                submitting = false
            }
        }
    }

    // After the customer completes OTP login, finish the submission they had already confirmed.
    LaunchedEffect(resumeSubmissionAfterAuth, isAuthenticated) {
        if (resumeSubmissionAfterAuth && isAuthenticated) {
            onResumeSubmissionHandled()
            if (step == journey.reviewIndex && submittedSummary == null && !submitting &&
                ServiceRequestValidator.validateForSubmit(draft, lines) == null
            ) {
                submitRequest()
            }
        }
    }

    val summary = submittedSummary
    if (summary != null) {
        ServiceRequestSubmittedView(
            summary = summary,
            categoryId = category.id,
            onViewMyBookings = onViewMyBookings,
            onBackToHome = onBackToHome
        )
        return
    }

    fun goBack() {
        returnToReview = false
        idempotencyKey = null
        if (step == 0) onBackClick() else goTo(step - 1)
    }

    BackHandler(enabled = step > 0 && !submitting) { goBack() }

    BookingFlowTheme {
        CaterHubLightSystemBars()
        Scaffold(
            containerColor = BookingPalette.Background,
            topBar = { BookingFlowTopBar(category.title, onBackClick) },
            bottomBar = {
                val fixedTotal = draft.fixedTotal(lines)
                val hasQuote = draft.hasQuoteServices(lines)
                BookingBottomActionBar(
                    summary = {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                when {
                                    isReview -> "Ready to submit"
                                    returnToReview -> "Editing · returns to review"
                                    else -> "Next: ${journey.stepTitles()[step + 1]}"
                                },
                                color = BookingPalette.Muted,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            if (draft.hasSelection(lines)) {
                                Text(
                                    when {
                                        hasQuote && fixedTotal > 0 -> "${rupees(fixedTotal)} + quote"
                                        hasQuote -> BookingPriceDisplay.PRICE_ON_QUOTE
                                        else -> rupees(fixedTotal)
                                    },
                                    color = BookingPalette.Emerald,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    BookingSecondaryButton(
                        text = if (step == 0) "Cancel" else "Back",
                        onClick = { goBack() },
                        enabled = !submitting,
                        modifier = Modifier.weight(1f)
                    )
                    BookingPrimaryButton(
                        text = when {
                            isReview -> "Submit request"
                            returnToReview -> "Save & review"
                            else -> "Continue"
                        },
                        loading = submitting,
                        loadingText = "Submitting…",
                        enabled = !submitting,
                        onClick = {
                            val message = ServiceRequestValidator.validateStep(currentStep, draft, lines)
                            when {
                                message != null -> {
                                    showErrors = true
                                    error = message
                                }
                                isReview -> if (isAuthenticated) submitRequest() else onAuthRequired()
                                returnToReview -> {
                                    returnToReview = false
                                    goTo(journey.reviewIndex)
                                }
                                else -> goTo(step + 1)
                            }
                        },
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                BookingStepProgress(
                    stepIndex = step,
                    stepTitles = journey.stepTitles(),
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
                    label = "serviceRequestStep",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { shownStep ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (journey.steps[shownStep.coerceIn(0, journey.steps.lastIndex)]) {
                            ServiceJourneyStep.EVENT -> ServiceEventStep(journey, draft, showErrors) { value ->
                                updateDraft { it.copy(eventType = value) }
                            }
                            ServiceJourneyStep.SERVICES -> ServiceSelectionStep(
                                journey = journey,
                                subtitle = category.subtitle,
                                lines = lines,
                                draft = draft,
                                showErrors = showErrors,
                                onQuantity = { id, qty -> updateDraft { it.withQuantity(id, qty) } }
                            )
                            ServiceJourneyStep.WHEN_WHERE -> ServiceWhenWhereStep(journey, draft, showErrors) { transform ->
                                updateDraft(transform)
                            }
                            ServiceJourneyStep.REVIEW -> ServiceReviewStep(
                                journey = journey,
                                categoryTitle = category.title.removePrefix("Book "),
                                lines = lines,
                                draft = draft,
                                enabled = !submitting
                            ) { target ->
                                returnToReview = true
                                idempotencyKey = null
                                goTo(journey.indexOf(target))
                            }
                        }
                        error?.let { ScheduleMessage(it, isError = true) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceEventStep(
    journey: ServiceJourney,
    draft: ServiceRequestDraft,
    showErrors: Boolean,
    onSelect: (String) -> Unit
) {
    BookingStepHeading(title = journey.eventHeading, subtitle = journey.eventSubtitle)
    EventImageGrid(
        selectedBackendValue = draft.eventType,
        onSelect = { onSelect(it.backendValue) }
    )
    if (showErrors && draft.eventType.isNullOrBlank()) {
        ScheduleMessage("Please select an event type.", isError = true)
    }
}

@Composable
private fun ServiceSelectionStep(
    journey: ServiceJourney,
    subtitle: String,
    lines: List<ServiceRequestLine>,
    draft: ServiceRequestDraft,
    showErrors: Boolean,
    onQuantity: (String, Int) -> Unit
) {
    BookingHeroBanner(
        visual = remember(journey.categoryId) { ServiceImagery.bannerVisual(journey.categoryId) },
        eyebrow = subtitle,
        title = journey.servicesHeading
    )
    Text(journey.servicesSubtitle, color = BookingPalette.Muted, style = MaterialTheme.typography.bodyMedium)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lines.forEach { line ->
            val qty = draft.quantityOf(line.id)
            val selected = qty > 0
            ImageRowCard(
                title = line.name,
                description = line.description.ifBlank { null },
                priceText = line.unitPrice?.let { "${rupees(it)} / ${line.priceUnit}" } ?: "Price on quote",
                visual = remember(journey.categoryId, line.id) { ServiceImagery.serviceVisual(journey.categoryId, line.id) },
                selected = selected,
                onClick = if (line.quoteOnly) {
                    { onQuantity(line.id, if (selected) 0 else 1) }
                } else if (!selected) {
                    { onQuantity(line.id, 1) }
                } else {
                    null
                },
                footer = if (!line.quoteOnly && selected) {
                    {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (line.workerType == null) {
                                BookingTextField(
                                    value = qty.toString(),
                                    onValueChange = { text ->
                                        onQuantity(line.id, text.filter(Char::isDigit).take(4).toIntOrNull() ?: 0)
                                    },
                                    label = "Quantity",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                                )
                            }
                            Text(
                                "Subtotal ${rupees(qty * (line.unitPrice ?: 0))}",
                                color = BookingPalette.Emerald,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    null
                }
            ) {
                if (line.quoteOnly) {
                    SelectToggleButton(
                        selected = selected,
                        onClick = { onQuantity(line.id, if (selected) 0 else 1) },
                        label = line.name
                    )
                } else {
                    QuantityStepper(
                        value = qty,
                        onDecrement = { onQuantity(line.id, qty - 1) },
                        onIncrement = { onQuantity(line.id, qty + 1) },
                        label = line.name,
                        canIncrement = qty < ServiceRequestDraft.MAX_QUANTITY
                    )
                }
            }
        }
    }

    val labels = draft.selectedLines(lines).map { line ->
        val qty = draft.quantityOf(line.id)
        if (line.quoteOnly || qty == 1) line.name else "$qty × ${line.name}"
    }
    if (showErrors && labels.isEmpty()) {
        ScheduleMessage("Please select at least one service.", isError = true)
    } else {
        SelectionSummaryText(labels, emptyHint = "Select at least one service to continue.")
    }
}

@Composable
private fun ServiceWhenWhereStep(
    journey: ServiceJourney,
    draft: ServiceRequestDraft,
    showErrors: Boolean,
    onUpdate: ((ServiceRequestDraft) -> ServiceRequestDraft) -> Unit
) {
    BookingStepHeading(
        title = journey.whenWhereHeading,
        subtitle = "Choose the date, timing and venue."
    )
    val scheduleMessage = ServiceRequestValidator.validateSchedule(draft)
    val invalidRange = draft.startTime.isNotBlank() && draft.endTime.isNotBlank() &&
        !EventTimeRules.isEndAfterStart(draft.startTime, draft.endTime)
    val showScheduleError = scheduleMessage != null &&
        (showErrors || invalidRange || (draft.eventDate.isNotBlank() && draft.startTime.isNotBlank() && draft.endTime.isNotBlank()))

    BookingCard {
        BookingSubheading(
            "Date",
            icon = Icons.Filled.CalendarMonth,
            trailing = draft.eventDate.takeIf { it.isNotBlank() }?.let(::displayDateForReview)
        )
        EventDateSelector(
            isoDate = draft.eventDate.takeIf { it.isNotBlank() },
            onDateSelected = { iso -> onUpdate { it.copy(eventDate = iso) } },
            isError = showErrors && draft.eventDate.isBlank()
        )
        BookingSubheading(
            "Start time",
            icon = Icons.Filled.Schedule,
            trailing = draft.startTime.takeIf { it.isNotBlank() }?.let(::displayTime)
        )
        StartTimeSlotPicker(
            time = draft.startTime.takeIf { it.isNotBlank() },
            onTimeSelected = { time ->
                onUpdate { current ->
                    // Keep a previously chosen end only while it is still after the new start.
                    val keepEnd = current.endTime.isNotBlank() && EventTimeRules.isEndAfterStart(time, current.endTime)
                    current.copy(startTime = time, endTime = if (keepEnd) current.endTime else "")
                }
            },
            isError = showErrors && draft.startTime.isBlank()
        )
        BookingSubheading(
            "How long?",
            icon = Icons.Filled.Schedule,
            trailing = draft.endTime.takeIf { it.isNotBlank() }?.let { "Ends ${displayTime(it)}" }
        )
        EndTimeDurationPicker(
            startTime = draft.startTime.takeIf { it.isNotBlank() },
            endTime = draft.endTime.takeIf { it.isNotBlank() },
            onEndSelected = { end -> onUpdate { it.copy(endTime = end) } },
            isError = invalidRange || (showErrors && draft.endTime.isBlank())
        )
        if (showScheduleError) ScheduleMessage(scheduleMessage.orEmpty(), isError = true)
    }

    BookingCard {
        BookingSubheading("Venue", icon = Icons.Filled.LocationOn)
        BookingLocationSection(
            address = draft.address,
            onAddressChange = { value -> onUpdate { it.copy(address = value.take(EVENT_ADDRESS_MAX_LENGTH)) } },
            area = draft.area,
            onAreaChange = { value -> onUpdate { it.copy(area = value.take(EVENT_AREA_MAX_LENGTH)) } },
            addressMaxLength = EVENT_ADDRESS_MAX_LENGTH,
            areaMaxLength = EVENT_AREA_MAX_LENGTH,
            showErrors = showErrors
        )
    }

    BookingCard {
        BookingSubheading("Anything else?", icon = Icons.AutoMirrored.Filled.Notes)
        BookingTextField(
            value = draft.notes,
            onValueChange = { value -> onUpdate { it.copy(notes = value.take(EVENT_NOTES_MAX_LENGTH)) } },
            label = "Additional instructions (optional)",
            placeholder = "Access, parking, dress code or special requests…",
            supportingText = "${draft.notes.length}/$EVENT_NOTES_MAX_LENGTH",
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

@Composable
private fun ServiceReviewStep(
    journey: ServiceJourney,
    categoryTitle: String,
    lines: List<ServiceRequestLine>,
    draft: ServiceRequestDraft,
    enabled: Boolean,
    onEdit: (ServiceJourneyStep) -> Unit
) {
    val event = EventTypeCatalog.eventTypes.firstOrNull { it.backendValue == draft.eventType }
    val occasion = EventTypeCatalog.displayNameForBackendValue(draft.eventType).ifBlank { null }
    BookingReviewHeader(
        visual = remember(event?.id) { BookingImagery.visual(BookingImagery.eventPhoto(event?.id ?: "other")) },
        title = occasion ?: categoryTitle,
        subtitle = if (occasion != null) categoryTitle else null,
        facts = listOfNotNull(
            draft.eventDate.takeIf { it.isNotBlank() }?.let { Icons.Filled.CalendarMonth to displayDateForReview(it) },
            draft.startTime.takeIf { it.isNotBlank() }?.let { Icons.Filled.Schedule to displayTime(it) }
        )
    )

    journey.steps.forEach { step ->
        when (step) {
            ServiceJourneyStep.EVENT -> BookingReviewSection(
                if (journey.categoryId == "beauty") "Occasion" else "Event details",
                Icons.Filled.Event,
                { onEdit(ServiceJourneyStep.EVENT) },
                enabled
            ) {
                BookingReviewRow("Event type", occasion ?: "Not selected")
                BookingReviewRow("Service", categoryTitle)
            }
            ServiceJourneyStep.SERVICES -> BookingReviewSection(
                journey.servicesTitle,
                Icons.Filled.Star,
                { onEdit(ServiceJourneyStep.SERVICES) },
                enabled
            ) {
                draft.selectedLines(lines).forEach { line ->
                    val qty = draft.quantityOf(line.id)
                    BookingReviewRow(
                        line.name,
                        if (line.quoteOnly) "Selected · price on quote" else "$qty × ${rupees(line.unitPrice ?: 0)} / ${line.priceUnit}"
                    )
                }
            }
            ServiceJourneyStep.WHEN_WHERE -> {
                BookingReviewSection("Date & time", Icons.Filled.CalendarMonth, { onEdit(ServiceJourneyStep.WHEN_WHERE) }, enabled) {
                    BookingReviewRow("Date", draft.eventDate.takeIf { it.isNotBlank() }?.let(::displayDateForReview) ?: "Not selected")
                    BookingReviewRow("Starts", draft.startTime.takeIf { it.isNotBlank() }?.let(::displayTime) ?: "Not selected")
                    BookingReviewRow("Ends", draft.endTime.takeIf { it.isNotBlank() }?.let(::displayTime) ?: "Not selected")
                }
                BookingReviewSection("Location", Icons.Filled.LocationOn, { onEdit(ServiceJourneyStep.WHEN_WHERE) }, enabled) {
                    BookingReviewRow("Venue", draft.location().ifBlank { "Not provided" })
                    if (draft.notes.isNotBlank()) BookingReviewRow("Instructions", draft.notes.trim())
                }
            }
            ServiceJourneyStep.REVIEW -> Unit
        }
    }

    val selected = draft.selectedLines(lines)
    val fixedTotal = draft.fixedTotal(lines)
    val hasQuote = draft.hasQuoteServices(lines)
    PriceBreakdownCard(
        lines = selected.map { line ->
            val qty = draft.quantityOf(line.id)
            if (line.quoteOnly) {
                PriceLine(line.name, null, BookingPriceDisplay.PRICE_ON_QUOTE)
            } else {
                PriceLine(line.name, "$qty × ${rupees(line.unitPrice ?: 0)} / ${line.priceUnit}", rupees(qty * (line.unitPrice ?: 0)))
            }
        },
        totalLabel = BookingPriceDisplay.serviceTotalLabel(hasQuote, fixedTotal),
        totalValue = BookingPriceDisplay.serviceTotal(fixedTotal, hasQuote) { rupees(it) },
        note = if (hasQuote) {
            "CaterHub will confirm the price for quoted services before your booking is finalised."
        } else {
            "Based on published prices. Your booking reference and confirmed total appear after submission."
        }
    )
}

@Composable
private fun ServiceRequestSubmittedView(
    summary: SubmittedServiceRequestSummary,
    categoryId: String,
    onViewMyBookings: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }
    val isStaff = categoryId == "catering-staff"
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
                if (isStaff) "Staff booking submitted" else "Request submitted",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BookingPalette.Ink,
                textAlign = TextAlign.Center
            )
            Text(
                if (isStaff) {
                    "Your staff request is confirmed and has been sent to matching CaterHub workers."
                } else {
                    "Your event request has been received by CaterHub."
                },
                color = BookingPalette.Muted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            BookingCard {
                if (summary.reference.isNotBlank()) BookingReviewRow("Booking reference", summary.reference)
                BookingReviewRow("Service", summary.categoryTitle)
                BookingReviewRow("Occasion", EventTypeCatalog.displayNameForBackendValue(summary.eventType).ifBlank { summary.eventType })
                BookingReviewRow(
                    "Date & time",
                    "${displayDateForReview(summary.eventDate)} · ${displayTime(summary.startTime)} – ${displayTime(summary.endTime)}"
                )
                BookingReviewRow("Location", summary.location)
                if (summary.totalLabel.isNotBlank()) BookingReviewRow("Total", summary.totalLabel)
                BookingReviewRow("Status", "Pending confirmation")
            }
            Spacer(Modifier.height(4.dp))
            BookingPrimaryButton("View my bookings", onViewMyBookings, Modifier.fillMaxWidth())
            BookingSecondaryButton("Back to Home", onBackToHome, Modifier.fillMaxWidth())
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this, DateFormatter) }.getOrNull()
private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this, TimeFormatter) }.getOrNull()
private fun displayDateForReview(value: String): String =
    value.toLocalDateOrNull()?.format(DateReviewFormatter) ?: value
private fun displayTime(value: String): String =
    value.toLocalTimeOrNull()?.format(TimeDisplayFormatter) ?: value
