package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.StaffingJobResponse
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSectionHeader
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    viewModel: WorkerViewModel,
    onBackClick: () -> Unit,
    onFindJobsClick: () -> Unit,
    onMyJobsClick: () -> Unit,
    onJobClick: (Long) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(topBar = { TopAppBar(title = { Text("Worker Dashboard") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading worker dashboard...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) { CaterHubErrorState(message = state.message, onRetry = { viewModel.loadDashboard() }) }
            is WorkerUiState.DashboardLoaded -> {
                val dashboard = state.dashboard
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("Hello, ${dashboard.profile.fullName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Profile ${dashboard.profileCompletionPercent}% complete", fontWeight = FontWeight.Bold)
                                    Text("Verification: ${dashboard.profile.status.label}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                CaterHubStatusChip(dashboard.profile.status.label)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Available for work", fontWeight = FontWeight.SemiBold)
                                Switch(checked = dashboard.availableForWork, onCheckedChange = { viewModel.updateAvailability(it) })
                            }
                            CaterHubPrimaryButton("Find Catering Jobs", onFindJobsClick, Modifier.fillMaxWidth())
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Available", dashboard.nearbyOpportunities.size.toString(), Modifier.weight(1f))
                        StatCard("My Jobs", dashboard.myJobs.size.toString(), Modifier.weight(1f))
                        StatCard("Completed", dashboard.myJobs.count { it.status == "COMPLETED" }.toString(), Modifier.weight(1f))
                    }
                    CaterHubSectionHeader("Nearby opportunities", action = "View all", onActionClick = onFindJobsClick)
                    if (dashboard.nearbyOpportunities.isEmpty()) {
                        CaterHubEmptyState("No catering jobs available right now", "Refresh jobs or update your preferred areas.", actionText = "Refresh Jobs", onActionClick = { viewModel.loadDashboard() })
                    } else {
                        dashboard.nearbyOpportunities.forEach { WorkerStaffingJobCard(it, onClick = { onJobClick(it.id) }) }
                    }
                    CaterHubSectionHeader("My Jobs", action = "View all", onActionClick = onMyJobsClick)
                    if (dashboard.myJobs.isEmpty()) CaterHubEmptyState("No accepted jobs", "Accepted catering jobs will appear here.")
                    CaterHubPrimaryButton("Worker Profile", onProfileClick, Modifier.fillMaxWidth())
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WorkerStaffingJobCard(job: StaffingJobResponse, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Work, null, tint = MaterialTheme.colorScheme.primary)
                    Text(job.eventType, fontWeight = FontWeight.ExtraBold)
                }
                CaterHubStatusChip(job.status)
            }
            Text(job.workerType.label, fontWeight = FontWeight.SemiBold)
            Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${job.payment} · ${job.remainingPositions} positions remaining", fontWeight = FontWeight.Bold)
        }
    }
}
