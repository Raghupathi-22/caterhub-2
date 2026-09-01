package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.daily.cetaring.data.remote.dto.WorkerStatus
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSectionHeader
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.categoryUiMeta
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
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadDashboard() })
            }
            is WorkerUiState.DashboardLoaded -> {
                val d = state.dashboard
                val category = ServiceCatalog.categoryForWorkerType(d.profile.workerType)
                val visual = category?.let(::categoryUiMeta)
                val isVerified = d.profile.status == WorkerStatus.ACTIVE
                val isRejected = d.profile.status == WorkerStatus.REJECTED
                Column(
                    Modifier.fillMaxSize().background(Cream).padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Welcome, ${d.profile.fullName}", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = Red)
                    Text("Manage your services, availability and bookings from one place.", color = Muted)

                    Card(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = (visual?.accent ?: Red).copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = visual?.icon ?: Icons.Filled.Work,
                                        contentDescription = null,
                                        tint = visual?.accent ?: Red,
                                        modifier = Modifier.padding(12.dp).size(26.dp)
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(d.profile.workerType.label, style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold, color = Ink)
                                    Text(category?.title ?: d.profile.workerType.category, color = visual?.accent ?: Green, fontWeight = FontWeight.Bold)
                                }
                                CaterHubStatusChip(d.profile.status.label)
                            }
                            VerificationStateCard(
                                status = d.profile.status,
                                rejectionReason = d.profile.rejectionReason,
                                onUpdateProfile = onProfileClick
                            )
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PaleGold.copy(alpha = 0.45f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Available for work", color = Ink, fontWeight = FontWeight.ExtraBold)
                                        Text(
                                            when {
                                                !isVerified -> "Available after verification"
                                                d.availableForWork -> "You're available for suitable jobs."
                                                else -> "You're currently unavailable for new jobs."
                                            },
                                            color = Muted
                                        )
                                    }
                                    Switch(
                                        checked = d.availableForWork,
                                        enabled = isVerified,
                                        onCheckedChange = viewModel::updateAvailability
                                    )
                                }
                            }
                            Text("Profile ${d.profileCompletionPercent}% complete", color = Muted, fontWeight = FontWeight.Bold)
                            CaterHubPrimaryButton("Find Available Jobs", onFindJobsClick, Modifier.fillMaxWidth())
                        }
                    }

                    val earnings = d.myJobs.filter { it.status == "COMPLETED" }.sumOf { it.payment.toInt() }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Stat("Available Jobs", d.nearbyOpportunities.size.toString(), PaleGold, Gold, Modifier.weight(1f))
                            Stat("My Jobs", d.myJobs.size.toString(), PaleGreen, Green, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Stat("Completed", d.myJobs.count { it.status == "COMPLETED" }.toString(), PaleRed, Red, Modifier.weight(1f))
                            Stat("Earnings", "₹$earnings", PaleGreen, Green, Modifier.weight(1f))
                        }
                    }

                    CaterHubSectionHeader("Jobs for You", "View All", onFindJobsClick)
                    if (d.nearbyOpportunities.isEmpty()) {
                        CaterHubEmptyState(
                            title = "No matching jobs right now",
                            message = "Try another preferred area or check again later.",
                            actionText = "Refresh",
                            onActionClick = { viewModel.loadDashboard() }
                        )
                    } else {
                        d.nearbyOpportunities.take(5).forEach { job ->
                            WorkerStaffingJobCard(
                                job = job,
                                onClick = { onJobClick(job.id) }
                            )
                        }
                    }

                    CaterHubSectionHeader("Upcoming Jobs")
                    val upcomingJobs = d.myJobs.filter { it.status == "ACCEPTED" || it.status == "OFFERED" }.take(3)
                    if (upcomingJobs.isEmpty()) {
                        CaterHubEmptyState("No upcoming jobs", "Accepted upcoming jobs will appear here.")
                    } else {
                        upcomingJobs.forEach {
                            BookingMiniCard(it.eventType, it.workerType.label, it.area, it.eventDate, it.startTime, it.status)
                        }
                    }

                    CaterHubSectionHeader("My Jobs", "View All", onMyJobsClick)
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
private fun VerificationStateCard(
    status: WorkerStatus,
    rejectionReason: String?,
    onUpdateProfile: () -> Unit
) {
    val pending = status == WorkerStatus.PENDING_VERIFICATION
    val rejected = status == WorkerStatus.REJECTED
    val verified = status == WorkerStatus.ACTIVE
    val tint = when {
        verified -> Green
        rejected -> Red
        else -> Gold
    }
    val bg = when {
        verified -> PaleGreen
        rejected -> PaleRed
        else -> PaleGold
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = when {
                        rejected -> Icons.Filled.WarningAmber
                        pending -> Icons.Filled.HourglassTop
                        else -> Icons.Filled.EventAvailable
                    },
                    contentDescription = null,
                    tint = tint
                )
                Text(
                    text = when {
                        pending -> "Profile verification pending"
                        rejected -> "Profile verification needs attention"
                        else -> "Profile verified"
                    },
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = when {
                    pending -> "Your profile is under review. Once CaterHub verifies your profile, you can turn on availability and start receiving suitable jobs."
                    rejected -> rejectionReason?.takeIf { it.isNotBlank() }
                        ?: "Your profile was rejected. Please update your profile details and submit again."
                    else -> "Your profile is verified. You can manage availability and receive suitable jobs."
                },
                color = Muted
            )
            if (rejected) {
                OutlinedButton(onClick = onUpdateProfile, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Update Profile", color = Red, fontWeight = FontWeight.Bold)
                }
            }
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
