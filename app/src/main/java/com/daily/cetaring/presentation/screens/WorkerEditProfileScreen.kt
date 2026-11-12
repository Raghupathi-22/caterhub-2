package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.WorkerProfileCatalog
import com.daily.cetaring.domain.worker.WorkerRegistrationRules
import com.daily.cetaring.presentation.components.WorkerBioField
import com.daily.cetaring.presentation.components.WorkerMultiSelectSection
import com.daily.cetaring.presentation.components.WorkerPalette
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton
import com.daily.cetaring.presentation.viewmodel.WorkerProfileEditRules
import com.daily.cetaring.presentation.viewmodel.WorkerProfileEditRules.Field
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

private val EditCream = WorkerPalette.Ivory
private val EditRed = WorkerPalette.Maroon
private val EditInk = WorkerPalette.Ink
private val EditMuted = WorkerPalette.Muted
private val EditBorder = WorkerPalette.Border
private val EditPaleGreen = WorkerPalette.EmeraldTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerEditProfileScreen(
    viewModel: WorkerViewModel,
    onCancel: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.editState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var prefilledProfileId by rememberSaveable { mutableStateOf<Long?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var skills by rememberSaveable { mutableStateOf("") }
    var areas by rememberSaveable { mutableStateOf("") }
    var languages by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (prefilledProfileId == null) viewModel.loadProfileForEdit()
    }

    // Pre-fill once from the backend copy; later recompositions must not overwrite user edits.
    LaunchedEffect(state.profile?.id) {
        val profile = state.profile ?: return@LaunchedEffect
        if (prefilledProfileId != profile.id) {
            name = profile.fullName
            age = profile.age?.toString().orEmpty()
            skills = profile.skills.orEmpty()
            areas = profile.preferredAreas.orEmpty()
            languages = profile.languages.orEmpty()
            bio = profile.bio.orEmpty()
            prefilledProfileId = profile.id
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.resetEditState()
            onSaved()
        }
    }

    LaunchedEffect(state.saveError) {
        val message = state.saveError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearEditSaveError()
    }

    val cancel = {
        if (!state.isSaving) {
            viewModel.resetEditState()
            onCancel()
        }
    }
    BackHandler(onBack = cancel)

    val errors = WorkerProfileEditRules.validate(name, age, skills, areas, languages, bio)
    val visibleErrors = if (showErrors) errors else emptyMap()

    // Same catalogue and rules as registration; values already on the profile are kept as chips.
    val workerType = state.profile?.workerType
    val role = remember(workerType) { WorkerProfileCatalog.registrationRoleFor(workerType) }
    val category = remember(workerType, role) {
        role?.let { ServiceCatalog.category(it.categoryId) } ?: ServiceCatalog.categoryForWorkerType(workerType)
    }
    val categoryId = category?.id.orEmpty()
    val skillOptions = remember(categoryId, role) { WorkerProfileCatalog.skillsForRole(categoryId, role?.id) }
    val skillList = remember(skills) { WorkerProfileCatalog.parseList(skills) }
    val areaList = remember(areas) { WorkerProfileCatalog.parseList(areas) }
    val languageList = remember(languages) { WorkerProfileCatalog.parseList(languages) }

    Scaffold(
        containerColor = EditCream,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = EditInk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = cancel, enabled = !state.isSaving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cancel editing", tint = EditRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EditCream)
            )
        }
    ) { padding ->
        when {
            state.isLoading || (state.profile == null && state.loadError == null) ->
                Column(Modifier.padding(padding)) { CaterHubLoadingState("Loading your profile...") }

            state.profile == null -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(
                    message = state.loadError ?: "Unable to load your profile",
                    onRetry = { viewModel.loadProfileForEdit() }
                )
            }

            else -> Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = EditPaleGreen)
                ) {
                    Text(
                        "Your mobile number, service role and verification status stay the same. " +
                            "Contact CaterHub support to change them.",
                        color = EditInk,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, EditBorder)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Personal details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EditRed
                        )
                        EditField(
                            value = name,
                            onValueChange = { name = it.take(WorkerProfileEditRules.MAX_NAME_LENGTH) },
                            label = "Full name",
                            error = visibleErrors[Field.NAME],
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        EditField(
                            value = age,
                            onValueChange = { age = it.filter(Char::isDigit).take(2) },
                            label = "Age (optional)",
                            error = visibleErrors[Field.AGE],
                            supporting = "Between ${WorkerProfileEditRules.MIN_AGE} and ${WorkerProfileEditRules.MAX_AGE}",
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )

                        Text(
                            "Work details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EditRed,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (role != null || category != null) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(WorkerPalette.EmeraldTint)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    workerRoleIcon(categoryId, role?.id.orEmpty()),
                                    contentDescription = null,
                                    tint = WorkerPalette.Emerald
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        role?.title ?: workerType?.label ?: "Service role",
                                        color = EditInk,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        category?.title.orEmpty() + " · role changes need CaterHub support",
                                        color = EditMuted,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                WorkerMultiSelectSection(
                    title = "Skills",
                    subtitle = "Skills for your role",
                    icon = Icons.Filled.WorkspacePremium,
                    options = skillOptions,
                    selected = skillList,
                    onToggle = { skills = WorkerProfileCatalog.joinList(WorkerRegistrationRules.toggle(skillList, it, WorkerProfileCatalog.MAX_SKILLS)) },
                    searchPlaceholder = "Search or add a skill",
                    maxSelected = WorkerProfileCatalog.MAX_SKILLS.coerceAtLeast(skillList.size),
                    error = visibleErrors[Field.SKILLS],
                    enabled = !state.isSaving,
                    onAddCustom = { value ->
                        WorkerProfileCatalog.validateCustomItem(value, skillList).fold(
                            onSuccess = {
                                skills = WorkerProfileCatalog.joinList(WorkerRegistrationRules.add(skillList, it, WorkerProfileCatalog.MAX_SKILLS))
                                null
                            },
                            onFailure = { it.message }
                        )
                    }
                )
                WorkerMultiSelectSection(
                    title = "Preferred service areas",
                    subtitle = "Where in Hyderabad can you work?",
                    icon = Icons.Filled.LocationOn,
                    options = WorkerProfileCatalog.serviceAreas,
                    selected = areaList,
                    onToggle = { areas = WorkerProfileCatalog.joinList(WorkerRegistrationRules.toggle(areaList, it, WorkerProfileCatalog.MAX_AREAS)) },
                    searchPlaceholder = "Search areas",
                    maxSelected = WorkerProfileCatalog.MAX_AREAS.coerceAtLeast(areaList.size),
                    error = visibleErrors[Field.AREAS],
                    enabled = !state.isSaving,
                    onAddCustom = { value ->
                        WorkerProfileCatalog.validateCustomItem(value, areaList).fold(
                            onSuccess = {
                                areas = WorkerProfileCatalog.joinList(WorkerRegistrationRules.add(areaList, it, WorkerProfileCatalog.MAX_AREAS))
                                null
                            },
                            onFailure = { it.message }
                        )
                    }
                )
                WorkerMultiSelectSection(
                    title = "Languages",
                    subtitle = "Languages you can speak with customers",
                    icon = Icons.Filled.Language,
                    options = WorkerProfileCatalog.languages,
                    selected = languageList,
                    onToggle = {
                        languages = WorkerProfileCatalog.joinList(
                            WorkerRegistrationRules.toggle(languageList, it, WorkerProfileCatalog.MAX_LANGUAGES)
                        )
                    },
                    searchPlaceholder = "Search languages",
                    maxSelected = WorkerProfileCatalog.MAX_LANGUAGES.coerceAtLeast(languageList.size),
                    error = visibleErrors[Field.LANGUAGES],
                    enabled = !state.isSaving
                )

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, EditBorder)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("About me", color = EditInk, fontWeight = FontWeight.Bold)
                        WorkerBioField(
                            value = bio,
                            onValueChange = { bio = it },
                            maxLength = WorkerProfileEditRules.MAX_TEXT_LENGTH,
                            enabled = !state.isSaving,
                            label = "About me (optional)",
                            error = visibleErrors[Field.BIO]
                        )
                    }
                }

                if (showErrors && errors.isNotEmpty()) {
                    Text(
                        "Please fix the highlighted fields.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CaterHubSecondaryButton(
                        text = "Cancel",
                        onClick = cancel,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                    CaterHubPrimaryButton(
                        text = if (state.isSaving) "Saving..." else "Save Changes",
                        onClick = {
                            showErrors = true
                            if (errors.isEmpty()) {
                                viewModel.saveProfile(
                                    WorkerProfileEditRules.toRequest(name, age, skills, areas, languages, bio)
                                )
                            }
                        },
                        enabled = !state.isSaving,
                        loading = state.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Changes are saved to your CaterHub worker profile.",
                    color = EditMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    placeholder: String? = null,
    supporting: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = workerInputColors(),
        modifier = Modifier.fillMaxWidth()
    )
}
