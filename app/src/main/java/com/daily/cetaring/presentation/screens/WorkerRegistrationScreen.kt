package com.daily.cetaring.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    viewModel: WorkerViewModel,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var step by remember { mutableIntStateOf(0) }
    var workerType by remember { mutableStateOf(WorkerType.CHEF) }
    var experience by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var areas by remember { mutableStateOf("") }
    var languages by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var showSubmittedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is WorkerUiState.Error -> snackbarHostState.showSnackbar(state.message)
            is WorkerUiState.Submitted -> showSubmittedDialog = true
            else -> Unit
        }
    }

    if (showSubmittedDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
            title = { Text("Profile submitted") },
            text = { Text("Your worker profile is now pending admin verification. You can track the status from your dashboard.") },
            confirmButton = {
                Button(onClick = {
                    showSubmittedDialog = false
                    viewModel.reset()
                    onSubmitted()
                }) { Text("Go to dashboard") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker registration") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Join verified catering jobs in Hyderabad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Complete this quick profile now. Documents, availability and OTP verification are prepared as the next secured steps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (step + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(targetState = step, label = "worker-step") { currentStep ->
                when (currentStep) {
                    0 -> WorkerTypeStep(workerType = workerType, onWorkerTypeChange = { workerType = it })
                    1 -> ExperienceStep(experience = experience, onExperienceChange = { experience = it })
                    2 -> SkillsStep(
                        skills = skills,
                        areas = areas,
                        languages = languages,
                        onSkillsChange = { skills = it },
                        onAreasChange = { areas = it },
                        onLanguagesChange = { languages = it }
                    )
                    else -> ReviewStep(
                        workerType = workerType,
                        experience = experience,
                        skills = skills,
                        areas = areas,
                        languages = languages,
                        bio = bio,
                        onBioChange = { bio = it }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { if (step == 0) onBackClick() else step-- },
                    shape = RoundedCornerShape(16.dp)
                ) { Text(if (step == 0) "Cancel" else "Back") }

                Button(
                    enabled = uiState !is WorkerUiState.Loading && isStepValid(step, experience, skills, areas, languages),
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            viewModel.submitProfile(
                                CreateWorkerProfileRequest(
                                    workerType = workerType,
                                    experienceYears = experience.toIntOrNull() ?: 0,
                                    skills = skills.trim(),
                                    preferredAreas = areas.trim(),
                                    languages = languages.trim(),
                                    bio = bio.trim().ifBlank { null }
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is WorkerUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (step < 3) "Continue" else "Submit for verification")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerTypeStep(workerType: WorkerType, onWorkerTypeChange: (WorkerType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    WizardCard(title = "Professional role", subtitle = "Choose the job category that best matches your experience.") {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = workerType.label,
                onValueChange = { },
                readOnly = true,
                label = { Text("Worker type") },
                leadingIcon = { Icon(Icons.Filled.Work, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                WorkerType.entries.forEach { type ->
                    DropdownMenuItem(text = { Text(type.label) }, onClick = {
                        onWorkerTypeChange(type)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun ExperienceStep(experience: String, onExperienceChange: (String) -> Unit) {
    WizardCard(title = "Experience", subtitle = "Tell admins how long you have worked in catering or hospitality.") {
        OutlinedTextField(
            value = experience,
            onValueChange = { value -> onExperienceChange(value.filter { it.isDigit() }.take(2)) },
            label = { Text("Experience in years") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            supportingText = { Text("Enter 0 for fresher") },
            isError = experience.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SkillsStep(
    skills: String,
    areas: String,
    languages: String,
    onSkillsChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    onLanguagesChange: (String) -> Unit
) {
    WizardCard(title = "Skills and availability areas", subtitle = "Use comma-separated values so admins can match you quickly.") {
        OutlinedTextField(value = skills, onValueChange = onSkillsChange, label = { Text("Skills") }, placeholder = { Text("Biryani, bulk cooking, plating") }, isError = skills.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = areas, onValueChange = onAreasChange, label = { Text("Preferred areas") }, placeholder = { Text("Madhapur, Kondapur, Jubilee Hills") }, isError = areas.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = languages, onValueChange = onLanguagesChange, label = { Text("Languages") }, placeholder = { Text("Telugu, Hindi, English") }, isError = languages.isBlank(), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ReviewStep(
    workerType: WorkerType,
    experience: String,
    skills: String,
    areas: String,
    languages: String,
    bio: String,
    onBioChange: (String) -> Unit
) {
    WizardCard(title = "Review", subtitle = "Confirm your profile before submitting for admin verification.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { }, label = { Text(workerType.label) })
            AssistChip(onClick = { }, label = { Text("$experience years") })
        }
        SummaryLine("Skills", skills)
        SummaryLine("Areas", areas)
        SummaryLine("Languages", languages)
        OutlinedTextField(value = bio, onValueChange = onBioChange, label = { Text("Short bio") }, minLines = 3, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun WizardCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value.ifBlank { "Not provided" }, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun isStepValid(step: Int, experience: String, skills: String, areas: String, languages: String): Boolean {
    return when (step) {
        1 -> experience.isNotBlank()
        2, 3 -> skills.isNotBlank() && areas.isNotBlank() && languages.isNotBlank()
        else -> true
    }
}
