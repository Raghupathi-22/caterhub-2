package com.daily.cetaring.presentation.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.data.remote.dto.WorkerJobResponse
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.SummaryRow
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobsScreen(viewModel: WorkerViewModel, onBackClick: () -> Unit, onJobClick: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.loadAvailableJobs() }
    Scaffold(topBar = { WorkerTopBar("Available Catering Jobs", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading catering jobs...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadAvailableJobs(search = search.ifBlank { null }) }) }
            is WorkerUiState.JobsLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(search, { search = it }, label = { Text("Search area, event or location") }, modifier = Modifier.fillMaxWidth())
                CaterHubPrimaryButton("Search Jobs", { viewModel.loadAvailableJobs(search = search.ifBlank { null }) }, Modifier.fillMaxWidth())
                if (state.jobs.isEmpty()) CaterHubEmptyState("No catering jobs available right now", "Try another area or refresh jobs.", actionText = "Refresh Jobs", onActionClick = { viewModel.loadAvailableJobs() })
                state.jobs.forEach { WorkerStaffingJobCard(it, onClick = { onJobClick(it.id) }) }
            }
            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobDetailsScreen(viewModel: WorkerViewModel, jobId: Long, onBackClick: () -> Unit, onAccepted: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(jobId) { viewModel.loadJob(jobId) }
    LaunchedEffect(uiState) { if (uiState is WorkerUiState.JobAccepted) onAccepted() }
    Scaffold(topBar = { WorkerTopBar("Job Details", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading job details...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadJob(jobId) }) }
            is WorkerUiState.JobDetailsLoaded -> JobDetailsContent(state.job, onAccept = { viewModel.acceptJob(state.job.id) }, modifier = Modifier.padding(padding))
            is WorkerUiState.JobAccepted -> JobDetailsContent(state.job, acceptedMessage = state.message, onAccept = null, modifier = Modifier.padding(padding))
            else -> Unit
        }
    }
}

@Composable
private fun JobDetailsContent(job: StaffingJobResponse, acceptedMessage: String? = null, onAccept: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (acceptedMessage != null) Text(acceptedMessage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(job.eventType, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); CaterHubStatusChip(job.status) }
                SummaryRow("Role required", job.workerType.label)
                SummaryRow("Date", job.eventDate)
                SummaryRow("Time", "${job.startTime} – ${job.endTime}")
                SummaryRow("Location", "${job.location}, ${job.area}")
                SummaryRow("Payment", "₹${job.payment}")
                SummaryRow("Positions", "${job.acceptedWorkers} / ${job.requiredWorkers} filled · ${job.remainingPositions} remaining")
                SummaryRow("Additional requirements", job.additionalRequirements.orEmpty())
            }
        }
        val canAccept = job.remainingPositions > 0 && job.alreadyAccepted != true && onAccept != null
        CaterHubPrimaryButton(if (job.remainingPositions <= 0) "All positions have been filled" else "Accept Job", onClick = { onAccept?.invoke() }, enabled = canAccept, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMyJobsScreen(viewModel: WorkerViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMyJobs() }
    Scaffold(topBar = { WorkerTopBar("My Jobs", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading my jobs...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadMyJobs() }) }
            is WorkerUiState.MyJobsLoaded -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.jobs.isEmpty()) CaterHubEmptyState("No accepted jobs", "Accepted jobs will appear here.")
                state.jobs.forEach { WorkerMyJobCard(it) }
            }
            else -> Unit
        }
    }
}

@Composable
private fun WorkerMyJobCard(job: WorkerJobResponse) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(job.eventType, fontWeight = FontWeight.ExtraBold); CaterHubStatusChip(job.status) }
            Text(job.workerType.label, fontWeight = FontWeight.SemiBold)
            Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${job.payment}", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    viewModel: WorkerViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    Scaffold(topBar = { WorkerTopBar("Worker Profile", onBackClick) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading worker profile...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadProfile() }) }
            is WorkerUiState.ProfileLoaded -> WorkerProfileContent(
                profile = state.profile,
                onLogoutClick = onLogoutClick,
                modifier = Modifier.padding(padding)
            )
            else -> Unit
        }
    }
}

@Composable
private fun WorkerProfileContent(
    profile: WorkerProfileResponse,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(profile.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); CaterHubStatusChip(profile.status.label) }
                SummaryRow("Worker role", profile.workerType.label)
                SummaryRow("Experience", "${profile.experienceYears} years")
                SummaryRow("Skills", profile.skills.orEmpty())
                SummaryRow("Languages", profile.languages.orEmpty())
                SummaryRow("Preferred areas", profile.preferredAreas.orEmpty())
                SummaryRow("Rating", "${profile.rating} (${profile.totalRatings})")
            }
        }
        CaterHubPrimaryButton(
            text = "Logout",
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } })
}
