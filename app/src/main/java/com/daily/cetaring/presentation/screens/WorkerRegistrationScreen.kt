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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.daily.cetaring.domain.catalog.ServiceCatalog
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
    var selectedCategoryId by remember { mutableStateOf(ServiceCatalog.categories.first().id) }
    var selectedRoleId by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var areas by remember { mutableStateOf("") }
    var languages by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var showSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategoryId) {
        val roles = ServiceCatalog.rolesForCategory(selectedCategoryId).filter { it.workerType != null }
        if (roles.none { it.id == selectedRoleId }) {
            selectedRoleId = roles.firstOrNull()?.id.orEmpty()
        }
    }

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
            Text("Worker registration", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold, color = ChRed)
            Text(
                "Offer your professional service to customers across Hyderabad.",
                style = MaterialTheme.typography.bodyLarge, color = ChMuted
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { i ->
                    androidx.compose.foundation.layout.Box(
                        Modifier.weight(1f).padding(vertical = 2.dp)
                            .background(if (i <= step) ChRed else ChBorder, RoundedCornerShape(20.dp))
                            .padding(vertical = 4.dp)
                    )
                }
            }
            Text("Step ${step + 1} of 5", color = ChGreen, fontWeight = FontWeight.Bold)

            AnimatedContent(step, label = "worker-registration-step") { current ->
                when (current) {
                    0 -> CategoryStep(selectedCategoryId) { selectedCategoryId = it }
                    1 -> RoleStep(selectedCategoryId, selectedRoleId) { selectedRoleId = it }
                    2 -> ExperienceStep(experience) { experience = it }
                    3 -> SkillsStep(skills, areas, languages, { skills = it }, { areas = it }, { languages = it })
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
                    onClick = { if (step == 0) onBackClick() else step-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(if (step == 0) "Cancel" else "Back", color = ChGreen) }

                Button(
                    enabled = uiState !is WorkerUiState.Loading && valid(step, selectedRoleId, experience, skills, areas, languages),
                    onClick = {
                        if (step < 4) {
                            if (step == 0 && selectedRoleId.isBlank()) {
                                selectedRoleId = ServiceCatalog.rolesForCategory(selectedCategoryId)
                                    .firstOrNull { it.workerType != null }?.id.orEmpty()
                            }
                            step++
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ChRed)
                ) {
                    if (uiState is WorkerUiState.Loading) CircularProgressIndicator(strokeWidth = 2.dp)
                    else Text(if (step < 4) "Continue" else "Submit")
                }
            }
        }
    }
}

@Composable
private fun CategoryStep(selectedCategoryId: String, onChange: (String) -> Unit) {
    CreamCard("Choose your service", "Select the category you provide professionally.") {
        ServiceCatalog.categories.filter { it.id != "other-event-services" }.forEach { category ->
            val selected = category.id == selectedCategoryId
            OutlinedButton(
                onClick = { onChange(category.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) ChRed else ChBorder)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(category.title, color = if (selected) ChRed else ChInk, fontWeight = FontWeight.Bold)
                    Text(category.subtitle, color = ChMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RoleStep(categoryId: String, selectedRoleId: String, onChange: (String) -> Unit) {
    val roles = remember(categoryId) {
        ServiceCatalog.rolesForCategory(categoryId).filter { it.workerType != null }
    }
    CreamCard("Choose your role", "Only roles for the selected category are shown.") {
        roles.forEach { role ->
            val selected = role.id == selectedRoleId
            OutlinedButton(
                onClick = { onChange(role.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) ChRed else ChBorder)
            ) {
                Text(role.title, color = if (selected) ChRed else ChInk, fontWeight = FontWeight.Bold)
            }
        }
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
    categoryId: String, roleId: String, experience: String, skills: String, areas: String, languages: String,
    bio: String, onBio: (String) -> Unit
) {
    val role = ServiceCatalog.roles.firstOrNull { it.id == roleId }
    val category = ServiceCatalog.category(categoryId)
    CreamCard("Review your profile", "Make sure your details are correct before submitting.") {
        androidx.compose.material3.AssistChip(onClick = {}, label = { Text(role?.title ?: "Role not selected") })
        ReviewLine("Category", category?.title ?: "Not selected")
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

private fun valid(step: Int, roleId: String, experience: String, skills: String, areas: String, languages: String) =
    when (step) {
        1 -> roleId.isNotBlank()
        2 -> experience.isNotBlank()
        3, 4 -> skills.isNotBlank() && areas.isNotBlank() && languages.isNotBlank()
        else -> true
    }
