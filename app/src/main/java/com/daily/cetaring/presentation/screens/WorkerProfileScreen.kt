package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.WorkerProfileResponse
import com.daily.cetaring.data.remote.dto.WorkerStatus
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.ProfileActionGroup
import com.daily.cetaring.presentation.components.ProfileActionRow
import com.daily.cetaring.presentation.components.ProfileBadge
import com.daily.cetaring.presentation.components.ProfileChipsRow
import com.daily.cetaring.presentation.components.ProfileConfirmDialog
import com.daily.cetaring.presentation.components.ProfileHeaderCard
import com.daily.cetaring.presentation.components.ProfileInfoRow
import com.daily.cetaring.presentation.components.ProfilePalette
import com.daily.cetaring.presentation.components.ProfileSectionCard
import com.daily.cetaring.presentation.viewmodel.WorkerUiState
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    viewModel: WorkerViewModel,
    mobileNumber: String?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onFindJobsClick: () -> Unit,
    onMyJobsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profileSaved by viewModel.profileSavedNotice.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Always re-read from the backend so values saved on the edit screen are shown.
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(profileSaved) {
        if (profileSaved) {
            viewModel.consumeProfileSavedNotice()
            snackbarHostState.showSnackbar("Profile updated successfully")
        }
    }

    Scaffold(
        containerColor = ProfilePalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = ProfilePalette.Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ProfilePalette.Burgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfilePalette.Background)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is WorkerUiState.ProfileLoaded -> WorkerProfileContent(
                profile = state.profile,
                mobileNumber = mobileNumber,
                onEditClick = onEditClick,
                onFindJobsClick = onFindJobsClick,
                onMyJobsClick = onMyJobsClick,
                onHelpClick = onHelpClick,
                onLogoutClick = onLogoutClick,
                modifier = Modifier.padding(padding)
            )
            is WorkerUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadProfile() })
            }
            else -> Column(Modifier.padding(padding)) { CaterHubLoadingState("Loading your profile...") }
        }
    }
}

@Composable
private fun WorkerProfileContent(
    profile: WorkerProfileResponse,
    mobileNumber: String?,
    onEditClick: () -> Unit,
    onFindJobsClick: () -> Unit,
    onMyJobsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val categoryLabel = ServiceCatalog.categoryForWorkerType(profile.workerType)?.title ?: profile.workerType?.category
    val roleLabel = profile.workerType?.label
    val statusStyle = workerStatusStyle(profile.status)

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeaderCard(
            name = profile.fullName.ifBlank { profile.username },
            mobileNumber = mobileNumber,
            mobileVerified = !mobileNumber.isNullOrBlank(),
            accountType = "Worker",
            onEditClick = onEditClick,
            extraBadges = { ProfileBadge(profile.status.label, statusStyle.container, statusStyle.content) },
            footer = {
                HorizontalDivider(color = ProfilePalette.Divider)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    WorkerStat(
                        value = if (profile.totalRatings > 0) String.format(Locale.US, "%.1f", profile.rating) else "–",
                        label = "Rating",
                        showStar = profile.totalRatings > 0,
                        modifier = Modifier.weight(1f)
                    )
                    WorkerStat(profile.totalRatings.toString(), "Reviews", modifier = Modifier.weight(1f))
                    WorkerStat("${profile.experienceYears} yrs", "Experience", modifier = Modifier.weight(1f))
                }
            }
        )

        VerificationBanner(profile, statusStyle, onEditClick)

        ProfileSectionCard(
            title = "Personal information",
            icon = Icons.Filled.Person,
            actionLabel = "Edit",
            onActionClick = onEditClick
        ) {
            ProfileInfoRow("Name", profile.fullName)
            ProfileInfoRow("Mobile number", mobileNumber)
            ProfileInfoRow("Age", profile.age?.let { "$it years" })
            ProfileChipsRow("Languages", profile.languages)
            ProfileInfoRow("About me", profile.bio, showDivider = false)
        }

        ProfileSectionCard(
            title = "Professional details",
            icon = Icons.Filled.Work,
            actionLabel = "Edit",
            onActionClick = onEditClick
        ) {
            ProfileInfoRow("Service category", categoryLabel)
            ProfileInfoRow("Professional role", roleLabel)
            ProfileInfoRow("Experience", "${profile.experienceYears} ${if (profile.experienceYears == 1) "year" else "years"}")
            ProfileChipsRow("Skills", profile.skills)
            ProfileChipsRow("Preferred areas", profile.preferredAreas)
            ProfileInfoRow("Verification status", profile.status.label, showDivider = false)
        }
        Text(
            "Mobile number, service role, experience, verification status and ratings are managed by CaterHub.",
            color = ProfilePalette.Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        ProfileActionGroup(title = "Work") {
            ProfileActionRow("Find Jobs", Icons.Filled.Search, onFindJobsClick, subtitle = "Browse jobs matching your role")
            ProfileActionRow("My Jobs", Icons.AutoMirrored.Filled.ListAlt, onMyJobsClick, subtitle = "Accepted and completed jobs")
            ProfileActionRow("Help & Support", Icons.AutoMirrored.Filled.HelpOutline, onHelpClick, subtitle = "Call or WhatsApp CaterHub", showDivider = false)
        }

        ProfileActionGroup(title = "Account") {
            ProfileActionRow(
                "Logout", Icons.AutoMirrored.Filled.Logout, { showLogoutDialog = true },
                subtitle = "Sign out of this device", showChevron = false
            )
            ProfileActionRow(
                "Delete Account", Icons.Filled.DeleteOutline, { showDeleteDialog = true },
                subtitle = "Handled by CaterHub support for worker accounts",
                destructive = true, showChevron = false, showDivider = false
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showLogoutDialog) {
        ProfileConfirmDialog(
            title = "Log out of CaterHub?",
            message = "You'll need to verify your mobile number with an OTP to sign in again.",
            confirmLabel = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            onConfirm = { showLogoutDialog = false; onLogoutClick() },
            onDismiss = { showLogoutDialog = false }
        )
    }
    if (showDeleteDialog) {
        // Worker deletion is support-assisted on the backend (jobs and payouts must be settled first).
        ProfileConfirmDialog(
            title = "Delete your worker account?",
            message = "Worker accounts are deleted by CaterHub support so that assigned jobs and payments can be settled first. " +
                "Contact support to request permanent deletion of your account and personal data.",
            confirmLabel = "Contact Support",
            destructive = true,
            icon = Icons.Filled.DeleteOutline,
            onConfirm = { showDeleteDialog = false; onHelpClick() },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

private data class StatusStyle(val container: Color, val content: Color)

private fun workerStatusStyle(status: WorkerStatus): StatusStyle = when (status) {
    WorkerStatus.ACTIVE -> StatusStyle(ProfilePalette.GreenSoft, ProfilePalette.Green)
    WorkerStatus.PENDING_VERIFICATION -> StatusStyle(ProfilePalette.AmberSoft, ProfilePalette.Amber)
    WorkerStatus.REJECTED, WorkerStatus.SUSPENDED -> StatusStyle(ProfilePalette.DangerSoft, ProfilePalette.Danger)
}

@Composable
private fun WorkerStat(value: String, label: String, modifier: Modifier = Modifier, showStar: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showStar) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFC28A12), modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(3.dp))
            }
            Text(value, color = ProfilePalette.Ink, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        }
        Text(label, color = ProfilePalette.Muted, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun VerificationBanner(profile: WorkerProfileResponse, style: StatusStyle, onEditClick: () -> Unit) {
    val (icon, title, message) = when (profile.status) {
        WorkerStatus.ACTIVE -> Triple(
            Icons.Filled.Verified, "Profile verified",
            "Your service profile is active and can be matched to customer bookings."
        )
        WorkerStatus.PENDING_VERIFICATION -> Triple(
            Icons.Filled.HourglassTop, "Verification pending",
            "Your profile will become bookable after CaterHub verifies your details."
        )
        WorkerStatus.REJECTED -> Triple(
            Icons.Filled.WarningAmber, "Profile needs attention",
            profile.rejectionReason?.takeIf { it.isNotBlank() }
                ?: "Please update your profile details and submit again."
        )
        WorkerStatus.SUSPENDED -> Triple(
            Icons.Filled.WarningAmber, "Account suspended",
            "Your profile is temporarily hidden from customers. Please contact CaterHub support."
        )
    }
    Surface(shape = RoundedCornerShape(16.dp), color = style.container, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = style.content, modifier = Modifier.size(22.dp))
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = ProfilePalette.Ink, fontWeight = FontWeight.Bold)
                Text(message, color = ProfilePalette.Ink.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                if (profile.status == WorkerStatus.REJECTED) {
                    TextButton(onClick = onEditClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("Update profile", color = ProfilePalette.Burgundy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
