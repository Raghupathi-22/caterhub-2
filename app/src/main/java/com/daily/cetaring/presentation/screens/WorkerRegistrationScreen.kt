package com.daily.cetaring.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.CreateWorkerProfileRequest
import com.daily.cetaring.data.remote.dto.WorkerType
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

private val ChCream = Color(0xFFFFFBF3)
private val ChRed = Color(0xFFA61920)
private val ChGreen = Color(0xFF08752D)
private val ChGold = Color(0xFFC28A12)
private val ChInk = Color(0xFF292623)
private val ChMuted = Color(0xFF746E68)
private val ChBorder = Color(0xFFE1D8CA)

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
    var showSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is WorkerUiState.Error) {
            snackbarHostState.showSnackbar((uiState as WorkerUiState.Error).message)
        }
        if (uiState is WorkerUiState.Submitted) showSubmitted = true
    }

    if (showSubmitted) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = ChGreen) },
            title = { Text("Profile submitted", color = ChRed, fontWeight = FontWeight.ExtraBold) },
            text = { Text("Your profile has been submitted for CaterHub admin verification.") },
            confirmButton = {
                Button(onClick = {
                    showSubmitted = false
                    viewModel.reset()
                    onSubmitted()
                }) { Text("Go to dashboard") }
            }
        )
    }

    Scaffold(
        containerColor = ChCream,
        topBar = {
            TopAppBar(
                title = { Text("Worker Registration", color = ChInk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ChRed)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = ChCream)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(ChCream).padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Join CaterHub", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold, color = ChRed)
            Text(
                "Offer your professional service to customers across Hyderabad.",
                style = MaterialTheme.typography.bodyLarge, color = ChMuted
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    androidx.compose.foundation.layout.Box(
                        Modifier.weight(1f).padding(vertical = 2.dp)
                            .background(if (i <= step) ChRed else ChBorder, RoundedCornerShape(20.dp))
                            .padding(vertical = 4.dp)
                    )
                }
            }
            Text("Step ${step + 1} of 4", color = ChGreen, fontWeight = FontWeight.Bold)

            AnimatedContent(step, label = "worker-registration-step") { current ->
                when (current) {
                    0 -> RoleStep(workerType) { workerType = it }
                    1 -> ExperienceStep(experience) { experience = it }
                    2 -> SkillsStep(skills, areas, languages, { skills = it }, { areas = it }, { languages = it })
                    else -> ReviewStep(workerType, experience, skills, areas, languages, bio) { bio = it }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { if (step == 0) onBackClick() else step-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(if (step == 0) "Cancel" else "Back", color = ChGreen) }

                Button(
                    enabled = uiState !is WorkerUiState.Loading && valid(step, experience, skills, areas, languages),
                    onClick = {
                        if (step < 3) step++
                        else viewModel.submitProfile(
                            CreateWorkerProfileRequest(
                                workerType, experience.toIntOrNull() ?: 0,
                                skills.trim(), areas.trim(), languages.trim(), bio.trim().ifBlank { null }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ChRed)
                ) {
                    if (uiState is WorkerUiState.Loading) CircularProgressIndicator(strokeWidth = 2.dp)
                    else Text(if (step < 3) "Continue" else "Submit")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleStep(selected: WorkerType, onChange: (WorkerType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val groups = WorkerType.displayRoles().groupBy { it.category }
    CreamCard("Select your service", "Customers will see this as your primary professional role.") {
        ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
            OutlinedTextField(
                selected.label, {}, readOnly = true,
                label = { Text("Service role") },
                leadingIcon = { Icon(Icons.Filled.Work, null, tint = ChRed) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                groups.forEach { (category, roles) ->
                    DropdownMenuItem(
                        text = { Text(category, color = ChGreen, fontWeight = FontWeight.Bold) },
                        onClick = {}, enabled = false
                    )
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.label) },
                            onClick = { onChange(role); expanded = false }
                        )
                    }
                }
            }
        }
        Text(selected.category, color = ChGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExperienceStep(value: String, onChange: (String) -> Unit) {
    CreamCard("Your experience", "Add your experience so CaterHub can match you with suitable bookings.") {
        OutlinedTextField(
            value, { onChange(it.filter(Char::isDigit).take(2)) },
            label = { Text("Experience in years") },
            leadingIcon = { Icon(Icons.Filled.Badge, null, tint = ChRed) },
            supportingText = { Text("Enter 0 if you are a fresher.") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SkillsStep(
    skills: String, areas: String, languages: String,
    onSkills: (String) -> Unit, onAreas: (String) -> Unit, onLanguages: (String) -> Unit
) {
    CreamCard("Skills & service areas", "These details help us match you with nearby bookings.") {
        OutlinedTextField(skills, onSkills, label = { Text("Skills") },
            placeholder = { Text("Biryani, bulk cooking, plating") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(areas, onAreas, label = { Text("Preferred areas") },
            placeholder = { Text("Kondapur, Gachibowli, Madhapur") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(languages, onLanguages, label = { Text("Languages") },
            placeholder = { Text("Telugu, Hindi, English") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ReviewStep(
    role: WorkerType, experience: String, skills: String, areas: String, languages: String,
    bio: String, onBio: (String) -> Unit
) {
    CreamCard("Review your profile", "Make sure your details are correct before submitting.") {
        androidx.compose.material3.AssistChip(onClick = {}, label = { Text(role.label) })
        ReviewLine("Category", role.category)
        ReviewLine("Experience", "$experience years")
        ReviewLine("Skills", skills)
        ReviewLine("Preferred areas", areas)
        ReviewLine("Languages", languages)
        OutlinedTextField(bio, onBio, label = { Text("Short bio (optional)") },
            minLines = 3, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CreamCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChBorder),
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

private fun valid(step: Int, experience: String, skills: String, areas: String, languages: String) =
    when (step) {
        1 -> experience.isNotBlank()
        2, 3 -> skills.isNotBlank() && areas.isNotBlank() && languages.isNotBlank()
        else -> true
    }
