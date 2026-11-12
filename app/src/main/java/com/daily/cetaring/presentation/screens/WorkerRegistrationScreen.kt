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
import com.daily.cetaring.presentation.components.WorkerBioField
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

    val isSubmitting = uiState is WorkerUiState.Loading
    val stepError = WorkerRegistrationRules.stepError(draft, draft.step)
    val info = WorkerRegistrationRules.stepInfo(draft.step)

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
                StepProgress(
                    current = draft.step,
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
                onBack = handleBack,
                onContinue = {
                    if (draft.step < WorkerRegistrationRules.LAST_STEP) draft = WorkerRegistrationRules.next(draft) else submit()
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
                        onEdit = { target -> update(WorkerRegistrationRules.goTo(draft, target)) },
                        onBio = { update(WorkerRegistrationRules.setBio(draft, it)) }
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
private fun StepProgress(current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = "Step ${current + 1} of $STEP_COUNT" },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(STEP_COUNT) { index ->
            val fill by animateFloatAsState(
                targetValue = if (index <= current) 1f else 0f, animationSpec = tween(320), label = "step-fill-$index"
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Border)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .background(if (index == current) Gold else Emerald, CircleShape)
                )
            }
        }
    }
}

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
                        Text(if (isLast) "Submit Registration" else "Continue", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                        CategoryCard(
                            category = category,
                            selected = category.id == selectedCategoryId,
                            onClick = { onSelect(category.id) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: ServiceCategoryDefinition, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val visual = categoryUiMeta(category)
    val background by animateColorAsState(if (selected) EmeraldTint else Color.White, tween(200), label = "category-bg")
    val borderColor by animateColorAsState(if (selected) Emerald else Border, tween(200), label = "category-border")
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .heightIn(min = 150.dp)
            .clip(shape)
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { stateDescription = if (selected) "Selected" else "Not selected" }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(52.dp).background(visual.accent.copy(alpha = 0.13f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(visual.icon, contentDescription = null, tint = visual.accent, modifier = Modifier.size(28.dp))
            }
            Text(category.title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(category.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        SelectionBadge(selected, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun SelectionBadge(selected: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(if (selected) Emerald else Color.Transparent, tween(200), label = "badge")
    Box(
        modifier
            .size(24.dp)
            .background(color, CircleShape)
            .border(1.5.dp, if (selected) Emerald else FieldBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

// ---------------------------------------------------------------------------------------------
// Step 2 – role
// ---------------------------------------------------------------------------------------------

@Composable
private fun RoleStep(categoryId: String, selectedRoleId: String, onChangeCategory: () -> Unit, onSelect: (String) -> Unit) {
    val roles = remember(categoryId) { ServiceCatalog.workerRegistrationRolesForCategory(categoryId) }
    val category = remember(categoryId) { ServiceCatalog.category(categoryId) }
    val accent = category?.let { categoryUiMeta(it).accent } ?: Emerald
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (category != null) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GoldTint).padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(categoryUiMeta(category).icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                Text(
                    category.title,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(start = 10.dp)
                )
                TextButton(onClick = onChangeCategory) { Text("Change", color = Maroon, fontWeight = FontWeight.Bold) }
            }
        }
        roles.forEach { role ->
            key(role.id) {
                RoleCard(
                    title = role.title,
                    description = ServiceCatalog.workerRegistrationRoleDescription(role.id),
                    icon = workerRoleIcon(categoryId, role.id),
                    accent = accent,
                    selected = role.id == selectedRoleId,
                    onClick = { onSelect(role.id) }
                )
            }
        }
    }
}

@Composable
private fun RoleCard(title: String, description: String?, icon: ImageVector, accent: Color, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(if (selected) EmeraldTint else Color.White, tween(200), label = "role-bg")
    val borderColor by animateColorAsState(if (selected) Emerald else Border, tween(200), label = "role-border")
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { stateDescription = if (selected) "Selected" else "Not selected" }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(48.dp).background(if (selected) Emerald else accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else accent, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            if (description != null) Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        SelectionBadge(selected)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ReviewStep(
    draft: WorkerRegistrationDraft,
    enabled: Boolean,
    onEdit: (Int) -> Unit,
    onBio: (String) -> Unit
) {
    val role = ServiceCatalog.workerRegistrationRole(draft.roleId)
    val category = ServiceCatalog.category(draft.categoryId)
    val completion = WorkerRegistrationRules.completion(draft)
    val issues = WorkerRegistrationRules.validationSummary(draft)
    val profileLabel = role?.workerType?.label?.takeIf { it != role.title }
    val progress by animateFloatAsState(completion / 100f, tween(500), label = "completion")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = Emerald) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier.size(56.dp).background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            role?.let { workerRoleIcon(draft.categoryId, it.id) } ?: Icons.Filled.Badge,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(role?.title ?: "Role not selected", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            listOfNotNull(category?.title, WorkerProfileCatalog.experienceLabel(draft.experienceYears)).joinToString(" · "),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text("Profile strength", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text("$completion%", color = Gold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Gold,
                        trackColor = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                    if (draft.bio.isBlank() && issues.isEmpty()) {
                        Text("Add a short bio to reach 100%.", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (issues.isNotEmpty()) {
            Surface(shape = RoundedCornerShape(18.dp), color = Maroon.copy(alpha = 0.06f), border = BorderStroke(1.dp, Maroon.copy(alpha = 0.3f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Maroon, modifier = Modifier.size(20.dp))
                        Text("Complete these before submitting", color = Maroon, fontWeight = FontWeight.Bold)
                    }
                    issues.forEach { (step, message) ->
                        TextButton(onClick = { onEdit(step) }, enabled = enabled) { Text("• $message", color = Ink) }
                    }
                }
            }
        }

        ReviewSection("Service & role", onEdit = { onEdit(1) }, enabled = enabled) {
            ReviewValue(category?.title ?: "Not selected")
            ReviewValue(role?.title ?: "Not selected", strong = true)
            if (profileLabel != null) Text("Listed on your profile as $profileLabel", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        ReviewSection("Experience", onEdit = { onEdit(2) }, enabled = enabled) {
            ReviewValue(WorkerProfileCatalog.experienceLabel(draft.experienceYears), strong = true)
        }
        ReviewSection("Skills", onEdit = { onEdit(3) }, enabled = enabled) { ReviewChips(draft.skills) }
        ReviewSection("Preferred service areas", onEdit = { onEdit(3) }, enabled = enabled) { ReviewChips(draft.areas) }
        ReviewSection("Languages", onEdit = { onEdit(3) }, enabled = enabled) { ReviewChips(draft.languages) }

        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About you", color = Ink, fontWeight = FontWeight.Bold)
                WorkerBioField(
                    value = draft.bio,
                    onValueChange = onBio,
                    maxLength = WorkerProfileCatalog.MAX_BIO_LENGTH,
                    enabled = enabled
                )
            }
        }

        Text(
            "By submitting, your profile is sent to CaterHub for verification. You can edit skills, areas, languages and bio later.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReviewSection(title: String, onEdit: () -> Unit, enabled: Boolean, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title.uppercase(), color = Muted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onEdit, enabled = enabled, modifier = Modifier.semantics { contentDescription = "Edit $title" }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Maroon, modifier = Modifier.size(16.dp))
                    Text(" Edit", color = Maroon, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
        }
    }
}

@Composable
private fun ReviewValue(value: String, strong: Boolean = false) {
    Text(value, color = Ink, fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyLarge)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewChips(items: List<String>) {
    if (items.isEmpty()) {
        Text("None selected", color = Maroon, style = MaterialTheme.typography.bodyMedium)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Text(
                item,
                color = Emerald,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(CircleShape).background(EmeraldTint).padding(horizontal = 12.dp, vertical = 6.dp)
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
