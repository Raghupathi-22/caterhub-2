package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.categoryUiMeta
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

private val ChCream = Color(0xFFFFFBF3)
private val ChRed = Color(0xFFA61920)
private val ChGreen = Color(0xFF08752D)
private val ChInk = Color(0xFF292623)
private val ChMuted = Color(0xFF746E68)
private val ChBorder = Color(0xFFE1D8CA)
private val ChFieldBorder = Color(0xFFB9AE9C)
private val ChPlaceholder = Color(0xFF8F877E)

private const val LAST_STEP = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    viewModel: WorkerViewModel,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Saveable so form entries survive rotation/process recreation as well as step changes.
    var step by rememberSaveable { mutableIntStateOf(0) }
    var selectedCategoryId by rememberSaveable {
        mutableStateOf(ServiceCatalog.workerRegistrationCategories.first().id)
    }
    var selectedRoleId by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }
    var skills by rememberSaveable { mutableStateOf("") }
    var areas by rememberSaveable { mutableStateOf("") }
    var languages by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var showSubmitted by rememberSaveable { mutableStateOf(false) }

    val isSubmitting = uiState is WorkerUiState.Loading

    // Keep the chosen role while it still belongs to the chosen category; otherwise require a new choice.
    LaunchedEffect(selectedCategoryId) {
        val roles = ServiceCatalog.workerRegistrationRolesForCategory(selectedCategoryId)
        if (roles.none { it.id == selectedRoleId }) {
            selectedRoleId = ""
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is WorkerUiState.Error) {
            snackbarHostState.showSnackbar((uiState as WorkerUiState.Error).message)
        }
        if (uiState is WorkerUiState.Submitted) showSubmitted = true
    }

    // Single back path shared by the top-bar arrow, the Back/Cancel button and system Back.
    val handleBack: () -> Unit = {
        if (!isSubmitting) {
            if (step > 0) step -= 1 else onBackClick()
        }
    }

    BackHandler(enabled = !showSubmitted) { handleBack() }

    if (showSubmitted) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ChGreen.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, "Profile submitted successfully", tint = ChGreen, modifier = Modifier.size(34.dp))
                }
            },
            title = { Text("Profile submitted successfully", color = ChInk, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your profile has been sent to CaterHub for verification.",
                        color = ChMuted,
                        textAlign = TextAlign.Center
                    )
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChGreen.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, ChBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("STATUS", color = ChGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("Pending verification", color = ChInk, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "You'll be able to receive jobs after your profile is verified.",
                        color = ChMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                CaterHubPrimaryButton("Go to Dashboard", onClick = {
                    showSubmitted = false
                    viewModel.reset()
                    onSubmitted()
                }, modifier = Modifier.fillMaxWidth())
            },
            dismissButton = {
                TextButton(onClick = {
                    showSubmitted = false
                }) { Text("View Profile", color = ChGreen, fontWeight = FontWeight.Bold) }
            }
        )
    }

    Scaffold(
        containerColor = ChCream,
        topBar = {
            TopAppBar(
                title = { Text("Worker Registration", color = ChInk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = handleBack, enabled = !isSubmitting) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (step == 0) "Back" else "Previous step",
                            tint = ChRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChCream)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(ChCream).padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Worker registration", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold, color = ChRed
            )
            Text(
                "Offer your professional service to customers across Hyderabad.",
                style = MaterialTheme.typography.bodyLarge, color = ChMuted
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(LAST_STEP + 1) { i ->
                    Box(
                        Modifier.weight(1f).padding(vertical = 2.dp)
                            .background(if (i <= step) ChRed else ChBorder, RoundedCornerShape(20.dp))
                            .padding(vertical = 4.dp)
                    )
                }
            }
            Text("Step ${step + 1} of ${LAST_STEP + 1}", color = ChGreen, fontWeight = FontWeight.Bold)

            AnimatedContent(step, label = "worker-registration-step") { current ->
                when (current) {
                    0 -> CategoryStep(selectedCategoryId) { selectedCategoryId = it }
                    1 -> RoleStep(selectedCategoryId, selectedRoleId) { selectedRoleId = it }
                    2 -> ExperienceStep(experience) { experience = it }
                    3 -> SkillsStep(
                        categoryId = selectedCategoryId,
                        roleId = selectedRoleId,
                        skills = skills,
                        areas = areas,
                        languages = languages,
                        onSkills = { skills = it },
                        onAreas = { areas = it },
                        onLanguages = { languages = it }
                    )
                    else -> ReviewStep(
                        categoryId = selectedCategoryId,
                        roleId = selectedRoleId,
                        experience = experience,
                        skills = skills,
                        areas = areas,
                        languages = languages,
                        bio = bio,
                        onBio = { bio = it }
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = handleBack,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(if (step == 0) "Cancel" else "Back", color = ChGreen, fontWeight = FontWeight.Bold) }

                Button(
                    enabled = !isSubmitting &&
                        valid(step, selectedCategoryId, selectedRoleId, experience, skills, areas, languages),
                    onClick = {
                        if (step < LAST_STEP) {
                            step += 1
                        } else {
                            val workerType = ServiceCatalog.roles.firstOrNull { it.id == selectedRoleId }?.workerType
                            if (workerType != null) {
                                viewModel.submitProfile(
                                    CreateWorkerProfileRequest(
                                        workerType, experience.toIntOrNull() ?: 0,
                                        skills.trim(), areas.trim(), languages.trim(), bio.trim().ifBlank { null }
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChRed, contentColor = Color.White)
                ) {
                    if (isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(22.dp))
                    else Text(if (step < LAST_STEP) "Continue" else "Submit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryStep(selectedCategoryId: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepHeader("Choose your service", "Select the category you provide professionally.")
        ServiceCatalog.workerRegistrationCategories.forEach { category ->
            val visual = categoryUiMeta(category)
            SelectableOptionCard(
                title = category.title,
                subtitle = category.subtitle,
                icon = visual.icon,
                accent = visual.accent,
                selected = category.id == selectedCategoryId,
                onClick = { onChange(category.id) }
            )
        }
    }
}

@Composable
private fun RoleStep(categoryId: String, selectedRoleId: String, onChange: (String) -> Unit) {
    val roles = remember(categoryId) { ServiceCatalog.workerRegistrationRolesForCategory(categoryId) }
    val category = remember(categoryId) { ServiceCatalog.category(categoryId) }
    val accent = category?.let { categoryUiMeta(it).accent } ?: ChGreen
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StepHeader(
            "Choose your role",
            "${category?.title ?: "Selected category"} • Select the role that best describes your work."
        )
        // Single column so long role names are never squeezed into narrow grid cells.
        roles.forEach { role ->
            SelectableOptionCard(
                title = role.title,
                subtitle = ServiceCatalog.workerRegistrationRoleDescription(role.id),
                icon = workerRoleIcon(categoryId, role.id),
                accent = accent,
                selected = role.id == selectedRoleId,
                onClick = { onChange(role.id) }
            )
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ChInk)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ChMuted)
    }
}

@Composable
private fun SelectableOptionCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (selected) ChRed.copy(alpha = 0.05f) else Color.White),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ChRed else ChBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    color = if (selected) ChRed else ChInk,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle != null) {
                    Text(subtitle, color = ChMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) ChGreen else ChFieldBorder,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun workerRoleIcon(categoryId: String, roleId: String): ImageVector = when {
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
    roleId.contains("video") -> Icons.Filled.Videocam
    categoryId == "photography-video" -> Icons.Filled.CameraAlt
    categoryId == "religious-ceremony" -> Icons.Filled.TempleHindu
    else -> Icons.Filled.Badge
}

/**
 * Explicit input colors for registration fields. The app theme follows system dark mode and
 * Android 12+ dynamic color, so Material defaults (onSurface text) can render light text on
 * these always-white cards. Pinning the colors keeps typed text, labels and borders readable.
 */
@Composable
internal fun workerInputColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ChInk,
    unfocusedTextColor = ChInk,
    disabledTextColor = ChMuted,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    cursorColor = ChRed,
    selectionColors = TextSelectionColors(handleColor = ChRed, backgroundColor = ChRed.copy(alpha = 0.25f)),
    focusedBorderColor = ChRed,
    unfocusedBorderColor = ChFieldBorder,
    focusedLabelColor = ChRed,
    unfocusedLabelColor = ChMuted,
    focusedPlaceholderColor = ChPlaceholder,
    unfocusedPlaceholderColor = ChPlaceholder,
    focusedLeadingIconColor = ChRed,
    unfocusedLeadingIconColor = ChRed,
    focusedSupportingTextColor = ChMuted,
    unfocusedSupportingTextColor = ChMuted
)

@Composable
private fun ExperienceStep(value: String, onChange: (String) -> Unit) {
    CreamCard("Your experience", "Add your experience so CaterHub can match you with suitable bookings.") {
        OutlinedTextField(
            value, { onChange(it.filter(Char::isDigit).take(2)) },
            label = { Text("Experience in years") },
            leadingIcon = { Icon(Icons.Filled.Badge, null) },
            supportingText = { Text("Enter 0 if you are a fresher.") },
            colors = workerInputColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SkillsStep(
    categoryId: String,
    roleId: String,
    skills: String,
    areas: String,
    languages: String,
    onSkills: (String) -> Unit, onAreas: (String) -> Unit, onLanguages: (String) -> Unit
) {
    val role = remember(roleId) { ServiceCatalog.workerRegistrationRole(roleId) }
    val skillSuggestions = remember(categoryId, roleId) { ServiceCatalog.skillSuggestionsFor(categoryId, roleId) }
    val fieldColors = workerInputColors()
    CreamCard("Skills & service areas", "These details help us match you with nearby bookings.") {
        if (role != null) {
            Text("Role: ${role.title}", color = ChGreen, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            skills,
            onSkills,
            label = { Text("Skills") },
            placeholder = { Text(ServiceCatalog.skillPlaceholderFor(categoryId, roleId)) },
            supportingText = {
                if (skillSuggestions.isNotEmpty()) {
                    Text("Suggestions: ${skillSuggestions.take(5).joinToString(", ")}")
                }
            },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            areas, onAreas, label = { Text("Preferred areas") },
            placeholder = { Text("Kondapur, Gachibowli, Madhapur") },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            languages, onLanguages, label = { Text("Languages") },
            placeholder = { Text("Telugu, Hindi, English") },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReviewStep(
    categoryId: String, roleId: String, experience: String, skills: String, areas: String, languages: String,
    bio: String, onBio: (String) -> Unit
) {
    val role = ServiceCatalog.workerRegistrationRole(roleId)
    val profileLabel = role?.workerType?.label?.takeIf { it != role.title }
    val category = ServiceCatalog.category(categoryId)
    val visual = category?.let(::categoryUiMeta)
    CreamCard("Review your profile", "Make sure your details are correct before submitting.") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ChBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = (visual?.accent ?: ChGreen).copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = role?.let { workerRoleIcon(categoryId, it.id) } ?: visual?.icon ?: Icons.Filled.Badge,
                        contentDescription = "${category?.title ?: "Service"} role icon",
                        tint = visual?.accent ?: ChGreen,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(role?.title ?: "Role not selected", color = ChInk, fontWeight = FontWeight.ExtraBold)
                    Text(category?.title ?: "Category not selected", color = visual?.accent ?: ChGreen, fontWeight = FontWeight.Bold)
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(role?.title ?: "Role", color = ChInk, fontWeight = FontWeight.Bold) },
                    border = BorderStroke(1.dp, (visual?.accent ?: ChGreen).copy(alpha = 0.45f))
                )
            }
        }
        if (profileLabel != null) {
            Text(
                "Listed on your profile as: $profileLabel",
                color = ChMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        ReviewLine("Experience", "${experience.ifBlank { "0" }} years")
        ReviewLine("Skills", skills)
        ReviewLine("Preferred areas", areas)
        ReviewLine("Languages", languages)
        OutlinedTextField(
            bio, onBio, label = { Text("Short bio (optional)") },
            minLines = 3,
            colors = workerInputColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CreamCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ChInk)
            Text(subtitle, color = ChMuted)
            content()
        }
    }
}

@Composable
private fun ReviewLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = ChGreen, fontWeight = FontWeight.Bold)
        Text(value.ifBlank { "Not provided" }, color = ChInk)
    }
}

private fun valid(
    step: Int,
    categoryId: String,
    roleId: String,
    experience: String,
    skills: String,
    areas: String,
    languages: String
) = when (step) {
    0 -> ServiceCatalog.workerRegistrationCategories.any { it.id == categoryId }
    1 -> roleId.isNotBlank() &&
        ServiceCatalog.workerRegistrationRolesForCategory(categoryId).any { it.id == roleId }
    2 -> experience.isNotBlank()
    3, 4 -> skills.isNotBlank() && areas.isNotBlank() && languages.isNotBlank()
    else -> true
}
