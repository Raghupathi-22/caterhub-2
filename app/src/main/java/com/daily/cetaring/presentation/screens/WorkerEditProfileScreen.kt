package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton
import com.daily.cetaring.presentation.viewmodel.WorkerProfileEditRules
import com.daily.cetaring.presentation.viewmodel.WorkerProfileEditRules.Field
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

private val EditCream = Color(0xFFFFFBF3)
private val EditRed = Color(0xFFA61920)
private val EditInk = Color(0xFF292623)
private val EditMuted = Color(0xFF746E68)
private val EditBorder = Color(0xFFE1D8CA)
private val EditPaleGreen = Color(0xFFE9F4EA)

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
                        EditField(
                            value = skills,
                            onValueChange = { skills = it.take(WorkerProfileEditRules.MAX_TEXT_LENGTH) },
                            label = "Skills",
                            placeholder = "Bulk cooking, Biryani, Live counters",
                            error = visibleErrors[Field.SKILLS],
                            enabled = !state.isSaving,
                            singleLine = false,
                            minLines = 2,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        EditField(
                            value = areas,
                            onValueChange = { areas = it.take(WorkerProfileEditRules.MAX_TEXT_LENGTH) },
                            label = "Preferred service areas",
                            placeholder = "Kondapur, Gachibowli, Madhapur",
                            error = visibleErrors[Field.AREAS],
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        EditField(
                            value = languages,
                            onValueChange = { languages = it.take(WorkerProfileEditRules.MAX_TEXT_LENGTH) },
                            label = "Languages",
                            placeholder = "Telugu, Hindi, English",
                            error = visibleErrors[Field.LANGUAGES],
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        EditField(
                            value = bio,
                            onValueChange = { bio = it.take(WorkerProfileEditRules.MAX_TEXT_LENGTH) },
                            label = "About me (optional)",
                            placeholder = "Tell customers about your experience",
                            error = visibleErrors[Field.BIO],
                            supporting = "${bio.length}/${WorkerProfileEditRules.MAX_TEXT_LENGTH}",
                            enabled = !state.isSaving,
                            singleLine = false,
                            minLines = 3,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
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
