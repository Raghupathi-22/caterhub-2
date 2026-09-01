package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerStatus
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.SummaryRow
import com.daily.cetaring.presentation.components.categoryUiMeta
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel
import java.util.Locale

private val Cream = Color(0xFFFFFBF3)
private val Red = Color(0xFFA61920)
private val Green = Color(0xFF08752D)
private val Gold = Color(0xFFC28A12)
private val Ink = Color(0xFF292623)
private val Muted = Color(0xFF746E68)
private val Border = Color(0xFFE1D8CA)
private val PaleRed = Color(0xFFF9E8E5)
private val PaleGreen = Color(0xFFE9F4EA)
private val PaleGold = Color(0xFFFFF3D5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = Ink, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Red) } },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Cream)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobsScreen(viewModel: WorkerViewModel, onBackClick: () -> Unit, onJobClick: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var profile by remember { mutableStateOf<WorkerProfileResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var jobs by remember { mutableStateOf<List<StaffingJobResponse>>(emptyList()) }
    var selectedSkill by remember { mutableStateOf("") }
    var skillExpanded by remember { mutableStateOf(false) }
    var areaFilter by remember { mutableStateOf("") }
    var initialSearchDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> loading = true
            is WorkerUiState.ProfileLoaded -> {
                profile = state.profile
                loading = false
                errorMessage = null
                val role = ServiceCatalog.roles.firstOrNull { it.workerType == state.profile.workerType }
                val categoryId = role?.categoryId
                    ?: ServiceCatalog.categoryForWorkerType(state.profile.workerType)?.id
                    ?: "other-event-services"
                val roleId = role?.id
                val suggestions = ServiceCatalog.skillSuggestionsFor(categoryId, roleId)
                val preferredSkills = splitWorkerField(state.profile.skills)
                val preferredAreas = splitWorkerField(state.profile.preferredAreas)
                if (selectedSkill.isBlank()) {
                    selectedSkill = preferredSkills.firstOrNull { preferred ->
                        suggestions.any { it.equals(preferred, ignoreCase = true) }
                    } ?: suggestions.firstOrNull().orEmpty()
                }
                if (areaFilter.isBlank()) {
                    areaFilter = preferredAreas.firstOrNull().orEmpty()
                }
                if (state.profile.status == WorkerStatus.ACTIVE && !initialSearchDone) {
                    initialSearchDone = true
                    loading = true
                    viewModel.loadAvailableJobs(
                        role = state.profile.workerType,
                        area = areaFilter.ifBlank { null }
                    )
                }
            }
            is WorkerUiState.JobsLoaded -> {
                jobs = state.jobs
                loading = false
                errorMessage = null
            }
            is WorkerUiState.Error -> {
                loading = false
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    Scaffold(containerColor = Cream, topBar = { WorkerTopBar("Available Bookings", onBackClick) }) { padding ->
        val workerProfile = profile
        if (loading && workerProfile == null) {
            CaterHubLoadingState("Loading your worker profile...")
            return@Scaffold
        }

        if (workerProfile == null) {
            Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(
                    message = errorMessage ?: "Unable to load worker profile.",
                    onRetry = { viewModel.loadProfile() }
                )
            }
            return@Scaffold
        }

        val role = ServiceCatalog.roles.firstOrNull { it.workerType == workerProfile.workerType }
        val categoryId = role?.categoryId
            ?: ServiceCatalog.categoryForWorkerType(workerProfile.workerType)?.id
            ?: "other-event-services"
        val roleId = role?.id
        val category = ServiceCatalog.category(categoryId)
        val visual = category?.let(::categoryUiMeta)
        val skillSuggestions = ServiceCatalog.skillSuggestionsFor(categoryId, roleId)
        val isVerified = workerProfile.status == WorkerStatus.ACTIVE
        val filteredJobs = jobs.filter { job ->
            val areaMatches = areaFilter.isBlank() || job.area.contains(areaFilter, ignoreCase = true)
            val skillMatches = selectedSkill.isBlank() || jobMatchesSkill(job, selectedSkill)
            areaMatches && skillMatches
        }

        Column(
            Modifier.fillMaxSize().background(Cream).padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Available bookings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Red)
            Text("Find work matching your skills and preferred area.", color = Muted)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = visual?.icon ?: Icons.Filled.Work,
                            contentDescription = null,
                            tint = visual?.accent ?: Red
                        )
                        Text("  ${workerProfile.workerType.label}", color = Ink, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(category?.title ?: workerProfile.workerType.category, color = Muted)

                    if (!isVerified) {
                        WorkerVerificationGateCard(workerProfile.status, workerProfile.rejectionReason)
                    }

                    Box {
                        OutlinedTextField(
                            value = selectedSkill.ifBlank { "Select skill" },
                            onValueChange = {},
                            readOnly = true,
                            enabled = isVerified,
                            label = { Text("Skill") },
                            leadingIcon = { Icon(Icons.Filled.Work, null, tint = Red) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isVerified) { skillExpanded = true }
                        )
                        DropdownMenu(expanded = skillExpanded, onDismissRequest = { skillExpanded = false }) {
                            skillSuggestions.forEach { skill ->
                                DropdownMenuItem(
                                    text = { Text(skill) },
                                    onClick = {
                                        selectedSkill = skill
                                        skillExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = areaFilter,
                        onValueChange = { areaFilter = it },
                        enabled = isVerified,
                        label = { Text("Area") },
                        placeholder = { Text("Enter/select preferred area") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = Green) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CaterHubPrimaryButton(
                        text = "Search Bookings",
                        onClick = {
                            errorMessage = null
                            viewModel.loadAvailableJobs(
                                role = workerProfile.workerType,
                                area = areaFilter.ifBlank { null }
                            )
                        },
                        enabled = isVerified && selectedSkill.isNotBlank() && areaFilter.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text("Available bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Ink)
            if (errorMessage != null) {
                CaterHubErrorState(
                    message = errorMessage ?: "Unable to load bookings",
                    onRetry = {
                        viewModel.loadAvailableJobs(
                            role = workerProfile.workerType,
                            area = areaFilter.ifBlank { null }
                        )
                    }
                )
            } else if (!isVerified) {
                CaterHubEmptyState(
                    title = "Your profile is under verification",
                    message = "You can start receiving bookings after CaterHub verifies your profile."
                )
            } else if (filteredJobs.isEmpty()) {
                CaterHubEmptyState(
                    title = "No matching bookings",
                    message = "Try another skill or area.",
                    actionText = "Clear Filters",
                    onActionClick = {
                        selectedSkill = skillSuggestions.firstOrNull().orEmpty()
                        areaFilter = splitWorkerField(workerProfile.preferredAreas).firstOrNull().orEmpty()
                        viewModel.loadAvailableJobs(
                            role = workerProfile.workerType,
                            area = areaFilter.ifBlank { null }
                        )
                    }
                )
            } else {
                filteredJobs.forEach { job ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        WorkerStaffingJobCard(job = job, onClick = { onJobClick(job.id) })
                        CaterHubPrimaryButton(
                            text = "View Booking",
                            onClick = { onJobClick(job.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerVerificationGateCard(status: WorkerStatus, rejectionReason: String?) {
    val title = when (status) {
        WorkerStatus.PENDING_VERIFICATION -> "Your profile is under verification"
        WorkerStatus.REJECTED -> "Profile verification needs attention"
        WorkerStatus.SUSPENDED -> "Profile access is temporarily restricted"
        WorkerStatus.ACTIVE -> "Profile verified"
    }
    val description = when (status) {
        WorkerStatus.PENDING_VERIFICATION -> "You can start receiving bookings after CaterHub verifies your profile."
        WorkerStatus.REJECTED -> rejectionReason?.takeIf { it.isNotBlank() }
            ?: "Please update your profile details and contact support."
        WorkerStatus.SUSPENDED -> "Your profile is temporarily suspended from booking acceptance."
        WorkerStatus.ACTIVE -> "You can search and accept matching bookings."
    }
    val tone = when (status) {
        WorkerStatus.PENDING_VERIFICATION -> PaleGold
        WorkerStatus.REJECTED, WorkerStatus.SUSPENDED -> PaleRed
        WorkerStatus.ACTIVE -> PaleGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tone),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (status == WorkerStatus.PENDING_VERIFICATION) Icons.Filled.HourglassTop else Icons.Filled.Work,
                contentDescription = null,
                tint = if (status == WorkerStatus.PENDING_VERIFICATION) Gold else Red
            )
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(description, color = Muted)
            }
        }
    }
}

private fun splitWorkerField(raw: String?): List<String> =
    raw.orEmpty()
        .split(",", "\n", ";")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun jobMatchesSkill(job: StaffingJobResponse, skill: String): Boolean {
    val query = skill.trim().lowercase(Locale.ROOT)
    if (query.isBlank()) return true
    val searchable = buildString {
        append(job.workerType.label).append(' ')
        append(job.eventType).append(' ')
        append(job.additionalRequirements.orEmpty()).append(' ')
        append(job.area).append(' ')
        append(job.location)
    }.lowercase(Locale.ROOT)
    return searchable.contains(query)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobDetailsScreen(viewModel: WorkerViewModel, jobId: Long, onBackClick: () -> Unit, onAccepted: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(jobId) { viewModel.loadJob(jobId) }
    LaunchedEffect(uiState) { if (uiState is WorkerUiState.JobAccepted) onAccepted() }

    Scaffold(containerColor = Cream, topBar = { WorkerTopBar("Booking Details", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading booking...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadJob(jobId) })
            }
            is WorkerUiState.JobDetailsLoaded -> JobDetails(state.job, null, { viewModel.acceptJob(state.job.id) }, Modifier.padding(padding))
            is WorkerUiState.JobAccepted -> JobDetails(state.job, state.message, null, Modifier.padding(padding))
            else -> Unit
        }
    }
}

@Composable
private fun JobDetails(job: StaffingJobResponse, message: String?, onAccept: (() -> Unit)?, modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (message != null) Text(message, color = Green, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(job.eventType, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Red)
                        Text(job.workerType.label, color = Green, fontWeight = FontWeight.Bold)
                    }
                    CaterHubStatusChip(job.status)
                }
                Detail("Event date", job.eventDate, Icons.Filled.Event)
                Detail("Time", "${job.startTime} – ${job.endTime}", Icons.Filled.Event)
                Detail("Location", "${job.location}, ${job.area}", Icons.Filled.LocationOn)
                SummaryRow("Payment", "₹${job.payment}")
                SummaryRow("Positions", "${job.acceptedWorkers}/${job.requiredWorkers} filled · ${job.remainingPositions} remaining")
                SummaryRow("Requirements", job.additionalRequirements.orEmpty().ifBlank { "No additional requirements" })
            }
        }
        CaterHubPrimaryButton(
            if (job.remainingPositions <= 0) "All positions filled" else "Accept Booking",
            { onAccept?.invoke() },
            Modifier.fillMaxWidth(),
            enabled = job.remainingPositions > 0 && job.alreadyAccepted != true && onAccept != null
        )
    }
}

@Composable
private fun Detail(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Red)
        Column(Modifier.padding(start = 12.dp)) {
            Text(label, color = Muted)
            Text(value, color = Ink, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMyJobsScreen(viewModel: WorkerViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMyJobs() }
    Scaffold(containerColor = Cream, topBar = { WorkerTopBar("My Bookings", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading your bookings...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadMyJobs() })
            }
            is WorkerUiState.MyJobsLoaded -> Column(
                Modifier.fillMaxSize().background(Cream).padding(padding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Your bookings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Red)
                Text("Track accepted service bookings and their status.", color = Muted)
                if (state.jobs.isEmpty()) CaterHubEmptyState("No bookings yet", "Accepted bookings will appear here.")
                state.jobs.forEach { MyJobCard(it) }
            }
            else -> Unit
        }
    }
}

@Composable
private fun MyJobCard(job: WorkerJobResponse) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Event, null, tint = Green)
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(job.eventType, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text(job.workerType.label, color = Red, fontWeight = FontWeight.Bold)
                }
                CaterHubStatusChip(job.status)
            }
            Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = Muted)
            Text("₹${job.payment}", color = Green, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(viewModel: WorkerViewModel, onBackClick: () -> Unit, onLogoutClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Scaffold(containerColor = Cream, topBar = { WorkerTopBar("My Profile", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading your profile...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadProfile() })
            }
            is WorkerUiState.ProfileLoaded -> ProfileContent(state.profile, onLogoutClick, Modifier.padding(padding))
            else -> Unit
        }
    }
}

@Composable
private fun ProfileContent(profile: WorkerProfileResponse, onLogout: () -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PaleRed)) {
                        Icon(Icons.Filled.Person, null, tint = Red, modifier = Modifier.padding(16.dp))
                    }
                    Column(Modifier.padding(start = 14.dp).weight(1f)) {
                        Text(profile.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text(ServiceCatalog.categoryForWorkerType(profile.workerType)?.title ?: profile.workerType.category, color = Green)
                    }
                    CaterHubStatusChip(profile.status.label)
                }
                androidx.compose.material3.HorizontalDivider(color = Border)
                ProfileRow("Verification status", profile.status.label)
                ProfileRow("Service category", ServiceCatalog.categoryForWorkerType(profile.workerType)?.title ?: profile.workerType.category)
                ProfileRow("Service role", profile.workerType.label)
                ProfileRow("Experience", "${profile.experienceYears} years")
                ProfileRow("Skills", profile.skills.orEmpty().ifBlank { "Not added" })
                ProfileRow("Languages", profile.languages.orEmpty().ifBlank { "Not added" })
                ProfileRow("Preferred areas", profile.preferredAreas.orEmpty().ifBlank { "Not added" })
                ProfileRow("Availability", "Manage from worker dashboard")
                ProfileRow("Rating", "${profile.rating}")
                ProfileRow("Reviews", "${profile.totalRatings}")
            }
        }

        Text("Account status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Red)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PaleGreen)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(profile.status.label, color = Green, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (profile.status.name == "ACTIVE")
                        "Your service profile is active and can be matched to customer bookings."
                    else
                        "Your profile will become bookable after CaterHub admin verification.",
                    color = Ink
                )
            }
        }

        CaterHubPrimaryButton("Logout", onLogout, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = Green, fontWeight = FontWeight.Bold)
        Text(value, color = Ink, fontWeight = FontWeight.Medium)
    }
}
