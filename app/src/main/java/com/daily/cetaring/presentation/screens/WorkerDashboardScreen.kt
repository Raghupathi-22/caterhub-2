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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle ->
                CaterHubLoadingState("Loading your workspace...")

            is WorkerUiState.Error ->
                Column(Modifier.padding(padding).padding(20.dp)) {
                    CaterHubErrorState(message = state.message, onRetry = { viewModel.loadDashboard() })
                }

            is WorkerUiState.DashboardLoaded -> {
                val dashboard = state.dashboard
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Hello, ${dashboard.profile.fullName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Manage your availability and find suitable CaterHub opportunities.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Your service", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(dashboard.profile.workerType.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                    Text(dashboard.profile.workerType.category, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                CaterHubStatusChip(dashboard.profile.status.label)
                            }

                            Text("Profile ${dashboard.profileCompletionPercent}% complete", fontWeight = FontWeight.Bold)
                            Text(
                                "Verification: ${dashboard.profile.status.label}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Available for work", fontWeight = FontWeight.Bold)
                                    Text(
                                        if (dashboard.availableForWork) "You can receive suitable jobs" else "Turn this on to receive jobs",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Switch(
                                    checked = dashboard.availableForWork,
                                    onCheckedChange = { viewModel.updateAvailability(it) }
                                )
                            }

                            CaterHubPrimaryButton(
                                "Find Suitable Jobs",
                                onFindJobsClick,
                                Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("Available", dashboard.nearbyOpportunities.size.toString(), Modifier.weight(1f))
                        StatCard("My Jobs", dashboard.myJobs.size.toString(), Modifier.weight(1f))
                        StatCard("Completed", dashboard.myJobs.count { it.status == "COMPLETED" }.toString(), Modifier.weight(1f))
                    }

                    CaterHubSectionHeader("Nearby opportunities", action = "View all", onActionClick = onFindJobsClick)
                    if (dashboard.nearbyOpportunities.isEmpty()) {
                        CaterHubEmptyState(
                            "No matching jobs right now",
                            "Turn on availability or update your preferred areas.",
                            actionText = "Refresh",
                            onActionClick = { viewModel.loadDashboard() }
                        )
                    } else {
                        dashboard.nearbyOpportunities.take(5).forEach {
                            WorkerStaffingJobCard(it, onClick = { onJobClick(it.id) })
                        }
                    }

                    CaterHubSectionHeader("My Jobs", action = "View all", onActionClick = onMyJobsClick)
                    if (dashboard.myJobs.isEmpty()) {
                        CaterHubEmptyState("No accepted jobs", "Accepted jobs will appear here.")
                    } else {
                        dashboard.myJobs.take(3).forEach { job ->
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(job.eventType, fontWeight = FontWeight.Bold)
                                        CaterHubStatusChip(job.status)
                                    }
                                    Text(job.workerType.label, color = MaterialTheme.colorScheme.primary)
                                    Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    CaterHubPrimaryButton("View My Professional Profile", onProfileClick, Modifier.fillMaxWidth())
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(vertical = 16.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WorkerStaffingJobCard(job: StaffingJobResponse, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Work, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(job.eventType, fontWeight = FontWeight.ExtraBold)
                        Text(job.workerType.label, color = MaterialTheme.colorScheme.primary)
                    }
                }
                CaterHubStatusChip(job.status)
            }
            Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${job.payment} · ${job.remainingPositions} positions remaining", fontWeight = FontWeight.Bold)
        }
    }
}
