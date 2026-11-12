package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceCategoryDefinition
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog
import com.daily.cetaring.domain.worker.WorkerRegistrationDraft
import com.daily.cetaring.domain.worker.WorkerRegistrationRules
import com.daily.cetaring.presentation.components.ProfilePreviewHeader
import com.daily.cetaring.presentation.components.ProfileReviewSection
import com.daily.cetaring.presentation.components.PremiumProfessionalRoleCard
import com.daily.cetaring.presentation.components.PremiumServiceCategoryCard
import com.daily.cetaring.presentation.components.RegistrationProgressIndicator
import com.daily.cetaring.presentation.components.ServiceImage
import com.daily.cetaring.presentation.components.ServiceImagery
import com.daily.cetaring.presentation.components.WorkerBioField
import com.daily.cetaring.presentation.components.WorkerRemovableChip
import com.daily.cetaring.presentation.components.WorkerMultiSelectSection
import com.daily.cetaring.presentation.components.WorkerPalette
import com.daily.cetaring.presentation.components.WorkerSelectChip
import com.daily.cetaring.presentation.components.categoryUiMeta
import com.daily.cetaring.presentation.components.workerSearchFieldColors
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

private val Ivory = WorkerPalette.Ivory
private val Emerald = WorkerPalette.Emerald
private val EmeraldTint = WorkerPalette.EmeraldTint
private val Maroon = WorkerPalette.Maroon
private val Gold = WorkerPalette.Gold
private val GoldTint = WorkerPalette.GoldTint
private val Ink = WorkerPalette.Ink
private val Muted = WorkerPalette.Muted
private val Border = WorkerPalette.Border
private val FieldBorder = WorkerPalette.FieldBorder

private const val STEP_COUNT = WorkerRegistrationRules.LAST_STEP + 1

/** Bundle-safe saver so the whole draft survives rotation and process recreation. */
private val DraftSaver = listSaver<WorkerRegistrationDraft, Any>(
    save = { it.toSaveable() },
    restore = { WorkerRegistrationDraft.fromSaveable(it) }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    viewModel: WorkerViewModel,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var draft by rememberSaveable(stateSaver = DraftSaver) { mutableStateOf(WorkerRegistrationDraft()) }
    var showSubmitted by rememberSaveable { mutableStateOf(false) }
    // Set when a section is edited from the review; Continue then returns straight to the review.
    var returnToReview by rememberSaveable { mutableStateOf(false) }

    val isSubmitting = uiState is WorkerUiState.Loading
    val stepError = WorkerRegistrationRules.stepError(draft, draft.step)
    val info = WorkerRegistrationRules.stepInfo(draft.step)

    LaunchedEffect(draft.step) {
        if (draft.step == WorkerRegistrationRules.LAST_STEP) returnToReview = false
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is WorkerUiState.Error -> snackbarHostState.showSnackbar(state.message)
            is WorkerUiState.Submitted -> showSubmitted = true
            else -> Unit
        }
    }

    val handleBack: () -> Unit = {
        if (!isSubmitting) {
            if (draft.step > WorkerRegistrationRules.FIRST_STEP) draft = WorkerRegistrationRules.previous(draft) else onBackClick()
        }
    }
    val update: (WorkerRegistrationDraft) -> Unit = { if (!isSubmitting) draft = it }
    val submit: () -> Unit = {
        WorkerRegistrationRules.toCreateRequest(draft)?.let { if (!isSubmitting) viewModel.submitProfile(it) }
    }

    BackHandler(enabled = !showSubmitted) { handleBack() }

    if (showSubmitted) {
        SubmittedDialog(
            onDashboard = {
                showSubmitted = false
                viewModel.reset()
                onSubmitted()
            },
            onDismiss = { showSubmitted = false }
        )
    }

    Scaffold(
        containerColor = Ivory,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(Modifier.background(Ivory)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Partner Registration", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Step ${draft.step + 1} of $STEP_COUNT · ${info.shortTitle}", color = Muted, style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = handleBack, enabled = !isSubmitting) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (draft.step == 0) "Close registration" else "Previous step",
                                tint = Emerald
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ivory)
                )
                RegistrationProgressIndicator(
                    current = draft.step,
                    total = STEP_COUNT,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )
            }
        },
        bottomBar = {
            BottomActions(
                step = draft.step,
                hint = stepError,
                canContinue = stepError == null,
                isSubmitting = isSubmitting,
                returningToReview = returnToReview && draft.step < WorkerRegistrationRules.LAST_STEP,
                onBack = handleBack,
                onContinue = {
                    when {
                        draft.step == WorkerRegistrationRules.LAST_STEP -> submit()
                        returnToReview -> {
                            draft = WorkerRegistrationRules.returnToReview(draft)
                            if (draft.step == WorkerRegistrationRules.LAST_STEP) returnToReview = false
                        }
                        else -> draft = WorkerRegistrationRules.next(draft)
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = draft.step,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally(tween(260)) { w -> if (forward) w / 5 else -w / 5 } + fadeIn(tween(220)))
                    .togetherWith(slideOutHorizontally(tween(200)) { w -> if (forward) -w / 6 else w / 6 } + fadeOut(tween(160)))
            },
            label = "worker-registration-step",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { step ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val stepInfo = WorkerRegistrationRules.stepInfo(step)
                StepHeader(stepInfo.title, stepInfo.description)
                when (step) {
                    0 -> ServiceStep(draft.categoryId) { update(WorkerRegistrationRules.selectCategory(draft, it)) }
                    1 -> RoleStep(
                        categoryId = draft.categoryId,
                        selectedRoleId = draft.roleId,
                        onChangeCategory = { update(WorkerRegistrationRules.goTo(draft, 0)) },
                        onSelect = { update(WorkerRegistrationRules.selectRole(draft, it)) }
                    )
                    2 -> ExperienceStep(draft.experienceYears) { update(WorkerRegistrationRules.setExperience(draft, it)) }
                    3 -> DetailsStep(draft = draft, enabled = !isSubmitting, onChange = update)
                    else -> ReviewStep(
                        draft = draft,
                        enabled = !isSubmitting,
                        onEdit = { target ->
                            if (!isSubmitting) {
                                returnToReview = true
                                draft = WorkerRegistrationRules.goTo(draft, target)
                            }
                        },
                        onDraftChange = update
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Shared layout
// ---------------------------------------------------------------------------------------------

@Composable
private fun StepHeader(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            modifier = Modifier.semantics { heading() }
        )
        Text(description, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

@Composable
private fun BottomActions(
    step: Int,
    hint: String?,
    canContinue: Boolean,
    isSubmitting: Boolean,
    returningToReview: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val isLast = step == WorkerRegistrationRules.LAST_STEP
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hint != null) {
                Text(hint, color = Muted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, FieldBorder)
                ) { Text(if (step == 0) "Cancel" else "Back", color = Emerald, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = onContinue,
                    enabled = canContinue && !isSubmitting,
                    modifier = Modifier.weight(if (isLast) 1.6f else 1.3f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLast) Maroon else Emerald,
                        contentColor = Color.White,
                        disabledContainerColor = Border,
                        disabledContentColor = Muted
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
                        Text("  Submitting…", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            when {
                                isLast -> "Submit Registration"
                                returningToReview -> "Save & review"
                                else -> "Continue"
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 1 – service
// ---------------------------------------------------------------------------------------------

@Composable
private fun ServiceStep(selectedCategoryId: String, onSelect: (String) -> Unit) {
    val categories = ServiceCatalog.workerRegistrationCategories
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val columns = if (maxWidth < 340.dp || fontScale > 1.3f) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { category ->
                        PremiumServiceCategoryCard(
                            title = category.title,
                            subtitle = category.subtitle,
                            visual = ServiceImagery.categoryVisual(category.id),
                            selected = category.id == selectedCategoryId,
                            onClick = { onSelect(category.id) },
                            imageHeight = if (columns == 1) 130.dp else 96.dp,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 2 – role
// ---------------------------------------------------------------------------------------------

@Composable
private fun RoleStep(categoryId: String, selectedRoleId: String, onChangeCategory: () -> Unit, onSelect: (String) -> Unit) {
    val roles = remember(categoryId) { ServiceCatalog.workerRegistrationRolesForCategory(categoryId) }
    val category = remember(categoryId) { ServiceCatalog.category(categoryId) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (category != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GoldTint)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceImage(
                    visual = ServiceImagery.categoryVisual(category.id),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    iconSize = 18.dp
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("SELECTED SERVICE", color = Muted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(category.title, color = Ink, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onChangeCategory) { Text("Change", color = Maroon, fontWeight = FontWeight.Bold) }
            }
            Text(
                "${roles.size} roles available in ${category.title}",
                color = Muted,
                style = MaterialTheme.typography.labelMedium
            )
        }
        roles.forEach { role ->
            key(role.id) {
                val visual = ServiceImagery.serviceVisual(categoryId, role.id)
                PremiumProfessionalRoleCard(
                    title = role.title,
                    description = ServiceCatalog.workerRegistrationRoleDescription(role.id),
                    visual = visual,
                    icon = workerRoleIcon(categoryId, role.id),
                    selected = role.id == selectedRoleId,
                    onClick = { onSelect(role.id) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 3 – experience
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExperienceStep(years: Int?, onChange: (Int?) -> Unit) {
    var typed by rememberSaveable { mutableStateOf(years?.toString().orEmpty()) }
    LaunchedEffect(years) { if (typed.toIntOrNull() != years) typed = years?.toString().orEmpty() }
    val max = WorkerProfileCatalog.MAX_EXPERIENCE_YEARS

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepperButton(Icons.Filled.Remove, "Decrease experience", enabled = (years ?: 0) > 0) {
                    onChange(((years ?: 0) - 1).coerceAtLeast(0))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when {
                            years == null -> "—"
                            years == 0 -> "Fresher"
                            else -> "$years"
                        },
                        color = Emerald,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        when {
                            years == null -> "Select your experience"
                            years == 0 -> "0 years · welcome aboard"
                            years == 1 -> "year of experience"
                            else -> "years of experience"
                        },
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                StepperButton(Icons.Filled.Add, "Increase experience", enabled = (years ?: -1) < max) {
                    onChange(((years ?: -1) + 1).coerceAtMost(max))
                }
            }
        }

        Text("Quick select", color = Ink, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkerProfileCatalog.experiencePresets.forEach { preset ->
                val isLastPreset = preset == WorkerProfileCatalog.experiencePresets.last()
                WorkerSelectChip(
                    label = preset.label,
                    selected = years != null && (years == preset.years || (isLastPreset && years >= preset.years)),
                    onClick = { onChange(preset.years) }
                )
            }
        }

        OutlinedTextField(
            value = typed,
            onValueChange = { input ->
                typed = input.filter(Char::isDigit).take(2)
                onChange(WorkerRegistrationRules.parseExperienceInput(typed))
            },
            label = { Text("Or enter exact years") },
            supportingText = { Text("Enter 0 if you are a fresher. Maximum $max years.") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = workerSearchFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        GuidanceCard(
            "Freshers are welcome. Many CaterHub bookings are for helpers and assistants, and your " +
                "rating grows with every completed job."
        )
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp).background(if (enabled) EmeraldTint else Border.copy(alpha = 0.5f), CircleShape)
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) Emerald else Muted)
    }
}

@Composable
private fun GuidanceCard(text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GoldTint).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFF9A7A2E), modifier = Modifier.size(20.dp))
        Text(text, color = Ink, style = MaterialTheme.typography.bodySmall)
    }
}

// ---------------------------------------------------------------------------------------------
// Step 4 – skills, areas and languages
// ---------------------------------------------------------------------------------------------

@Composable
private fun DetailsStep(draft: WorkerRegistrationDraft, enabled: Boolean, onChange: (WorkerRegistrationDraft) -> Unit) {
    val role = remember(draft.roleId) { ServiceCatalog.workerRegistrationRole(draft.roleId) }
    val skillOptions = remember(draft.categoryId, draft.roleId) { WorkerRegistrationRules.suggestedSkills(draft) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (role != null) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(EmeraldTint).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(workerRoleIcon(draft.categoryId, role.id), contentDescription = null, tint = Emerald, modifier = Modifier.size(20.dp))
                Text("Showing skills for ${role.title}", color = Emerald, fontWeight = FontWeight.SemiBold)
            }
        }
        WorkerMultiSelectSection(
            title = "Skills",
            subtitle = "Choose what you're great at",
            icon = Icons.Filled.WorkspacePremium,
            options = skillOptions,
            selected = draft.skills,
            onToggle = { onChange(WorkerRegistrationRules.toggleSkill(draft, it)) },
            searchPlaceholder = "Search or add a skill",
            maxSelected = WorkerProfileCatalog.MAX_SKILLS,
            enabled = enabled,
            onAddCustom = { value ->
                WorkerProfileCatalog.validateCustomItem(value, draft.skills).fold(
                    onSuccess = { onChange(WorkerRegistrationRules.toggleSkill(draft, it)); null },
                    onFailure = { it.message }
                )
            }
        )
        WorkerMultiSelectSection(
            title = "Preferred service areas",
            subtitle = "Where in Hyderabad can you work?",
            icon = Icons.Filled.LocationOn,
            options = WorkerProfileCatalog.serviceAreas,
            selected = draft.areas,
            onToggle = { onChange(WorkerRegistrationRules.toggleArea(draft, it)) },
            searchPlaceholder = "Search areas",
            maxSelected = WorkerProfileCatalog.MAX_AREAS,
            enabled = enabled,
            onAddCustom = { value ->
                WorkerProfileCatalog.validateCustomItem(value, draft.areas).fold(
                    onSuccess = { onChange(WorkerRegistrationRules.toggleArea(draft, it)); null },
                    onFailure = { it.message }
                )
            }
        )
        WorkerMultiSelectSection(
            title = "Languages",
            subtitle = "Languages you can speak with customers",
            icon = Icons.Filled.Language,
            options = WorkerProfileCatalog.languages,
            selected = draft.languages,
            onToggle = { onChange(WorkerRegistrationRules.toggleLanguage(draft, it)) },
            searchPlaceholder = "Search languages",
            maxSelected = WorkerProfileCatalog.MAX_LANGUAGES,
            enabled = enabled
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Step 5 – review
// ---------------------------------------------------------------------------------------------

@Composable
private fun ReviewStep(
    draft: WorkerRegistrationDraft,
    enabled: Boolean,
    onEdit: (Int) -> Unit,
    onDraftChange: (WorkerRegistrationDraft) -> Unit
) {
    val role = ServiceCatalog.workerRegistrationRole(draft.roleId)
    val category = ServiceCatalog.category(draft.categoryId)
    val checklist = WorkerRegistrationRules.checklist(draft)
    val strength = WorkerRegistrationRules.profileStrength(draft)
    val profileLabel = role?.workerType?.label?.takeIf { it != role.title }
    val roleVisual = ServiceImagery.serviceVisual(draft.categoryId, draft.roleId).let { visual ->
        if (visual.photoRes == null && role != null) visual.copy(icon = workerRoleIcon(draft.categoryId, role.id)) else visual
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProfilePreviewHeader(
            roleTitle = role?.title ?: "Role not selected",
            categoryTitle = category?.title,
            experienceLabel = draft.experienceYears?.let { WorkerProfileCatalog.experienceLabel(it) },
            visual = roleVisual,
            strength = strength,
            checklist = checklist,
            onItemClick = { step -> if (enabled) onEdit(step) }
        )

        ProfileReviewSection("Service & role", Icons.Filled.Badge, onEdit = { onEdit(0) }, enabled = enabled, missing = role == null) {
            ReviewKeyValue("Service", category?.title)
            ReviewKeyValue("Professional role", role?.title)
            if (profileLabel != null) Text("Listed on your profile as $profileLabel", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        ProfileReviewSection(
            "Experience",
            Icons.Filled.WorkspacePremium,
            onEdit = { onEdit(2) },
            enabled = enabled,
            missing = draft.experienceYears == null
        ) {
            if (draft.experienceYears == null) {
                MissingValue("Experience not selected")
            } else {
                Text(
                    WorkerProfileCatalog.experienceLabel(draft.experienceYears),
                    color = Emerald,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        ProfileReviewSection(
            "Professional skills",
            Icons.Filled.AutoAwesome,
            onEdit = { onEdit(3) },
            enabled = enabled,
            missing = draft.skills.isEmpty()
        ) {
            if (draft.skills.isEmpty()) {
                MissingValue("Select at least one skill")
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    draft.skills.forEach { skill ->
                        key(skill) {
                            WorkerRemovableChip(
                                label = skill,
                                onRemove = { onDraftChange(WorkerRegistrationRules.toggleSkill(draft, skill)) },
                                enabled = enabled && draft.skills.size > 1
                            )
                        }
                    }
                }
                if (draft.skills.size == 1) {
                    Text("At least one skill is required.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileReviewSection(
            "Preferred service areas",
            Icons.Filled.LocationOn,
            onEdit = { onEdit(3) },
            enabled = enabled,
            missing = draft.areas.isEmpty()
        ) { ReviewChips(draft.areas, "Select at least one service area") }
        ProfileReviewSection(
            "Languages",
            Icons.Filled.Language,
            onEdit = { onEdit(3) },
            enabled = enabled,
            missing = draft.languages.isEmpty()
        ) { ReviewChips(draft.languages, "Select at least one language") }
        ProfileReviewSection("Professional bio (optional)", Icons.Filled.RecordVoiceOver, onEdit = null, enabled = enabled) {
            Text(
                "A short introduction about your work helps customers trust you.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall
            )
            WorkerBioField(
                value = draft.bio,
                onValueChange = { onDraftChange(WorkerRegistrationRules.setBio(draft, it)) },
                maxLength = WorkerProfileCatalog.MAX_BIO_LENGTH,
                enabled = enabled
            )
        }

        Text(
            "By submitting, your profile is sent to CaterHub for verification. You can edit skills, areas, languages and bio later.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReviewKeyValue(label: String, value: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (value == null) {
            MissingValue("Not selected")
        } else {
            Text(value, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun MissingValue(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Maroon, modifier = Modifier.size(16.dp))
        Text(text, color = Maroon, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewChips(items: List<String>, emptyText: String) {
    if (items.isEmpty()) {
        MissingValue(emptyText)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Text(
                item,
                color = Emerald,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(EmeraldTint)
                    .border(1.dp, Emerald.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Success dialog
// ---------------------------------------------------------------------------------------------

@Composable
private fun SubmittedDialog(onDashboard: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(Modifier.size(64.dp).background(EmeraldTint, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CheckCircle, "Profile submitted successfully", tint = Emerald, modifier = Modifier.size(38.dp))
            }
        },
        title = { Text("Registration submitted", color = Ink, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your profile has been sent to CaterHub for verification.", color = Muted, textAlign = TextAlign.Center)
                Column(
                    Modifier.clip(RoundedCornerShape(16.dp)).background(GoldTint).padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("STATUS", color = Emerald, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("Pending verification", color = Ink, fontWeight = FontWeight.Bold)
                }
                Text("You'll be able to receive jobs after your profile is verified.", color = Muted, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = onDashboard,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.White)
            ) { Text("Go to Dashboard", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("View Profile", color = Emerald, fontWeight = FontWeight.Bold) }
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Shared helpers (also used by WorkerEditProfileScreen)
// ---------------------------------------------------------------------------------------------

internal fun workerRoleIcon(categoryId: String, roleId: String): ImageVector = when {
    roleId.contains("supervisor") -> Icons.Filled.SupervisorAccount
    roleId.contains("sweet") -> Icons.Filled.Cake
    roleId.contains("chef") -> Icons.Filled.SoupKitchen
    roleId == "kitchen-helper" -> Icons.Filled.Restaurant
    roleId == "serving-staff" || roleId == "catering-boy" || roleId == "catering-girl" -> Icons.Filled.RoomService
    roleId == "cleaner" -> Icons.Filled.CleaningServices
    roleId.contains("flower") -> Icons.Filled.LocalFlorist
    roleId.contains("lighting") -> Icons.Filled.Lightbulb
    roleId.contains("tent") || roleId.contains("stage-setup") -> Icons.Filled.Construction
    categoryId == "decoration" -> Icons.Filled.Celebration
    roleId == "dj" -> Icons.Filled.Headphones
    roleId == "sound-technician" -> Icons.Filled.GraphicEq
    roleId.contains("singer") -> Icons.Filled.Mic
    roleId.contains("band") || roleId.contains("melam") -> Icons.Filled.MusicNote
    roleId.contains("troupe") -> Icons.Filled.Groups
    roleId.contains("danc") -> Icons.Filled.Celebration
    roleId == "anchor" || roleId == "mc" -> Icons.Filled.RecordVoiceOver
    roleId == "magician" -> Icons.Filled.AutoAwesome
    categoryId == "entertainment" -> Icons.Filled.TheaterComedy
    roleId.contains("hair") -> Icons.Filled.ContentCut
    roleId.contains("mehendi") -> Icons.Filled.Brush
    roleId.contains("saree") -> Icons.Filled.Checkroom
    categoryId == "beauty" -> Icons.Filled.Face
    roleId.contains("drone") -> Icons.Filled.Flight
    roleId.contains("live-streaming") -> Icons.Filled.LiveTv
    roleId.contains("editor") -> Icons.Filled.Edit
    roleId.contains("booth") -> Icons.Filled.PhotoCamera
    roleId.contains("video") -> Icons.Filled.Videocam
    categoryId == "photography-video" -> Icons.Filled.CameraAlt
    categoryId == "religious-ceremony" -> Icons.Filled.TempleHindu
    else -> Icons.Filled.Badge
}

/**
 * Explicit input colors for worker fields. The app theme follows system dark mode and dynamic
 * color, so Material defaults can render light text on these always-white surfaces.
 */
@Composable
internal fun workerInputColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    disabledTextColor = Muted,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    cursorColor = Emerald,
    selectionColors = TextSelectionColors(handleColor = Emerald, backgroundColor = Emerald.copy(alpha = 0.25f)),
    focusedBorderColor = Emerald,
    unfocusedBorderColor = FieldBorder,
    focusedLabelColor = Emerald,
    unfocusedLabelColor = Muted,
    focusedPlaceholderColor = Muted,
    unfocusedPlaceholderColor = Muted,
    focusedLeadingIconColor = Emerald,
    unfocusedLeadingIconColor = Emerald,
    focusedSupportingTextColor = Muted,
    unfocusedSupportingTextColor = Muted
)
