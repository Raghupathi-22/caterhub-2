package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
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
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text("Worker Dashboard", color = Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Red) } },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            WorkerUiState.Loading, WorkerUiState.Idle -> CaterHubLoadingState("Loading your dashboard...")
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(state.message) { viewModel.loadDashboard() }
            }
            is WorkerUiState.DashboardLoaded -> {
                val d = state.dashboard
                Column(
                    Modifier.fillMaxSize().background(Cream).padding(padding)
                        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text("Hello, ${d.profile.fullName}", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = Red)
                    Text("Manage your services, availability and bookings from one place.", color = Muted)

                    Card(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Red)
                    ) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("YOUR SERVICE", color = Color.White.copy(.75f), fontWeight = FontWeight.Bold)
                                    Text(d.profile.workerType.label, style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text(d.profile.workerType.category, color = Color.White.copy(.9f))
                                }
                                CaterHubStatusChip(d.profile.status.label)
                            }
                            Text("Profile ${d.profileCompletionPercent}% complete", color = Color.White, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Available for bookings", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(if (d.availableForWork) "Customers can be matched to you" else "Turn on when you are ready", color = Color.White.copy(.8f))
                                }
                                Switch(checked = d.availableForWork, onCheckedChange = viewModel::updateAvailability)
                            }
                            CaterHubPrimaryButton("Find Available Jobs", onFindJobsClick, Modifier.fillMaxWidth())
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Stat("Available", d.nearbyOpportunities.size.toString(), PaleGold, Gold, Modifier.weight(1f))
                        Stat("My Jobs", d.myJobs.size.toString(), PaleGreen, Green, Modifier.weight(1f))
                        Stat("Completed", d.myJobs.count { it.status == "COMPLETED" }.toString(), PaleRed, Red, Modifier.weight(1f))
                    }

                    CaterHubSectionHeader("Nearby opportunities", "View All", onFindJobsClick)
                    if (d.nearbyOpportunities.isEmpty()) {
                        CaterHubEmptyState("No matching bookings right now", "Refresh or update your preferred areas.",
                            "Refresh") { viewModel.loadDashboard() }
                    } else {
                        d.nearbyOpportunities.take(5).forEach { WorkerStaffingJobCard(it) { onJobClick(it.id) } }
                    }

                    CaterHubSectionHeader("My Bookings", "View All", onMyJobsClick)
                    if (d.myJobs.isEmpty()) {
                        CaterHubEmptyState("No bookings yet", "Accepted service bookings will appear here.")
                    } else {
                        d.myJobs.take(3).forEach {
                            BookingMiniCard(it.eventType, it.workerType.label, it.area, it.eventDate, it.startTime, it.status)
                        }
                    }

                    CaterHubPrimaryButton("View Professional Profile", onProfileClick, Modifier.fillMaxWidth())
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun Stat(label: String, value: String, bg: Color, fg: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = fg)
            Text(label, color = Ink, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BookingMiniCard(event: String, service: String, area: String, date: String, time: String, status: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EventAvailable, null, tint = Green)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(event, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(service, color = Red, fontWeight = FontWeight.Bold)
                Text("$area · $date · $time", color = Muted)
            }
            CaterHubStatusChip(status)
        }
    }
}

@Composable
fun WorkerStaffingJobCard(job: StaffingJobResponse, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Work, null, tint = Green)
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(job.eventType, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text(job.workerType.label, color = Red, fontWeight = FontWeight.Bold)
                }
                CaterHubStatusChip(job.status)
            }
            Text("${job.area} · ${job.eventDate} · ${job.startTime}–${job.endTime}", color = Muted)
            Text("₹${job.payment} · ${job.remainingPositions} position(s) available", fontWeight = FontWeight.Bold, color = Green)
        }
    }
}
