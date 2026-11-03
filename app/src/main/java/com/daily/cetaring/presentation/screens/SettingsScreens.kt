package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.ProfileActionGroup
import com.daily.cetaring.presentation.components.ProfileActionRow
import com.daily.cetaring.presentation.components.ProfileConfirmDialog
import com.daily.cetaring.presentation.components.ProfilePalette
import com.daily.cetaring.presentation.components.ProfileSectionCard
import com.daily.cetaring.presentation.viewmodel.CustomerProfileUiState
import com.daily.cetaring.presentation.viewmodel.CustomerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isWorker: Boolean,
    onBackClick: () -> Unit,
    onAccountPrivacyClick: () -> Unit
) {
    SettingsScaffold(
        title = "Settings",
        onBackClick = onBackClick
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (isWorker) "Manage your worker account and privacy." else "Manage your account and privacy.",
                color = ProfilePalette.Muted,
                style = MaterialTheme.typography.bodyLarge
            )
            ProfileActionGroup(title = "Account") {
                ProfileActionRow(
                    title = "Account & Privacy",
                    icon = Icons.Filled.Lock,
                    onClick = onAccountPrivacyClick,
                    subtitle = "Manage personal data and account deletion",
                    showDivider = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAccountPrivacyScreen(
    customerProfileViewModel: CustomerProfileViewModel,
    onBackClick: () -> Unit,
    onAccountDeleted: (String) -> Unit
) {
    val uiState by customerProfileViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { customerProfileViewModel.loadProfile() }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CustomerProfileUiState.AccountDeleted -> onAccountDeleted(state.message)
            else -> Unit
        }
    }
    SettingsScaffold(
        title = "Account & Privacy",
        onBackClick = onBackClick
    ) { padding ->
        when (val state = uiState) {
            CustomerProfileUiState.Idle, CustomerProfileUiState.Loading ->
                Column(Modifier.padding(padding)) { CaterHubLoadingState("Loading account settings...") }
            is CustomerProfileUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(
                    message = state.message,
                    onRetry = customerProfileViewModel::loadProfile
                )
            }
            is CustomerProfileUiState.Loaded -> CustomerAccountPrivacyContent(
                state = state,
                onDeleteAccount = customerProfileViewModel::deleteAccount,
                modifier = Modifier.padding(padding)
            )
            CustomerProfileUiState.LoggedOut, is CustomerProfileUiState.AccountDeleted -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerAccountPrivacyScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    SettingsScaffold(
        title = "Account & Privacy",
        onBackClick = onBackClick
    ) { padding ->
        WorkerAccountPrivacyContent(
            onHelpClick = onHelpClick,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = ProfilePalette.Background,
        topBar = {
            TopAppBar(
                title = { Text(title, color = ProfilePalette.Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ProfilePalette.Burgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfilePalette.Background)
            )
        },
        content = content
    )
}

@Composable
private fun CustomerAccountPrivacyContent(
    state: CustomerProfileUiState.Loaded,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileSectionCard(
            title = "Delete account",
            icon = Icons.Filled.DeleteOutline
        ) {
            Column(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Permanently remove your CaterHub account and associated personal data. This action cannot be undone.",
                    color = ProfilePalette.Ink,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Your account is deleted only after the server confirms the request. If the request fails, you will remain signed in.",
                    color = ProfilePalette.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                state.actionError?.let {
                    Text(it, color = ProfilePalette.Danger, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    enabled = !state.isDeleting && !state.isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfilePalette.Danger,
                        contentColor = Color.White,
                        disabledContainerColor = ProfilePalette.Danger,
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Deleting account...", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Delete Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }

    if (showDeleteDialog && !state.isDeleting && !state.isSaving) {
        ProfileConfirmDialog(
            title = "Delete your CaterHub account?",
            message = "Deleting your account will permanently remove your account and associated personal data. This action cannot be undone.",
            confirmLabel = "Delete Account",
            destructive = true,
            icon = Icons.Filled.DeleteOutline,
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun WorkerAccountPrivacyContent(
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileSectionCard(
            title = "Delete worker account",
            icon = Icons.Filled.DeleteOutline
        ) {
            Column(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Worker account deletion is handled by CaterHub support so assigned jobs and payments can be settled first.",
                    color = ProfilePalette.Ink,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { showDeleteDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfilePalette.Danger,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Request account deletion", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }

    if (showDeleteDialog) {
        ProfileConfirmDialog(
            title = "Delete your worker account?",
            message = "Worker accounts are deleted by CaterHub support so that assigned jobs and payments can be settled first. Contact support to request permanent deletion of your account and personal data.",
            confirmLabel = "Contact Support",
            destructive = true,
            icon = Icons.Filled.DeleteOutline,
            onConfirm = {
                showDeleteDialog = false
                onHelpClick()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
