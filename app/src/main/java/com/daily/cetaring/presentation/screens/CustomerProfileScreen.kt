package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.UserDTO
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.ProfileActionGroup
import com.daily.cetaring.presentation.components.ProfileActionRow
import com.daily.cetaring.presentation.components.ProfileConfirmDialog
import com.daily.cetaring.presentation.components.ProfileHeaderCard
import com.daily.cetaring.presentation.components.ProfileInfoRow
import com.daily.cetaring.presentation.components.ProfilePalette
import com.daily.cetaring.presentation.components.ProfileSectionCard
import com.daily.cetaring.presentation.components.ProfileTextField
import com.daily.cetaring.presentation.viewmodel.CustomerProfileEditRules
import com.daily.cetaring.presentation.viewmodel.CustomerProfileEditRules.Field
import com.daily.cetaring.presentation.viewmodel.CustomerProfileUiState
import com.daily.cetaring.presentation.viewmodel.CustomerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: CustomerProfileViewModel,
    onBackClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLoggedOut: () -> Unit,
    onAccountDeleted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            CustomerProfileUiState.LoggedOut -> onLoggedOut()
            is CustomerProfileUiState.AccountDeleted -> onAccountDeleted(state.message)
            else -> Unit
        }
    }
    val loaded = uiState as? CustomerProfileUiState.Loaded
    LaunchedEffect(loaded?.saved) {
        if (loaded?.saved == true) {
            viewModel.consumeSaved()
            snackbarHostState.showSnackbar("Profile updated successfully")
        }
    }
    LaunchedEffect(loaded?.actionError) {
        val message = loaded?.actionError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearErrors()
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
            CustomerProfileUiState.Idle, CustomerProfileUiState.Loading ->
                Column(Modifier.padding(padding)) { CaterHubLoadingState("Loading your profile...") }
            is CustomerProfileUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadProfile() })
            }
            is CustomerProfileUiState.Loaded -> CustomerProfileContent(
                state = state,
                onSave = viewModel::saveProfile,
                onClearSaveError = viewModel::clearErrors,
                onBookingsClick = onBookingsClick,
                onHelpClick = onHelpClick,
                onLogout = viewModel::logout,
                onDeleteAccount = viewModel::deleteAccount,
                modifier = Modifier.padding(padding)
            )
            CustomerProfileUiState.LoggedOut, is CustomerProfileUiState.AccountDeleted -> Unit
        }
    }
}

@Composable
private fun CustomerProfileContent(
    state: CustomerProfileUiState.Loaded,
    onSave: (String, String, String) -> Unit,
    onClearSaveError: () -> Unit,
    onBookingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = state.user
    var editMode by rememberSaveable(user.id) { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.saved) { if (state.saved) editMode = false }
    BackHandler(enabled = editMode && !state.isSaving) {
        onClearSaveError()
        editMode = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeaderCard(
            name = customerDisplayName(user),
            mobileNumber = user.phoneNumber.takeIf { it.isNotBlank() },
            mobileVerified = user.isVerified,
            accountType = accountType(user.roles),
            imageUrl = user.profileImageUrl,
            onEditClick = if (editMode) null else ({ editMode = true })
        )

        if (editMode) {
            CustomerEditCard(
                user = user,
                isSaving = state.isSaving,
                saveError = state.saveError,
                onSave = onSave,
                onCancel = {
                    onClearSaveError()
                    editMode = false
                }
            )
        } else {
            ProfileSectionCard(
                title = "Personal information",
                icon = Icons.Filled.Person,
                actionLabel = "Edit",
                onActionClick = { editMode = true }
            ) {
                ProfileInfoRow("First name", user.firstName)
                ProfileInfoRow("Last name", user.lastName)
                ProfileInfoRow("Email", user.email)
                ProfileInfoRow("Mobile number", user.phoneNumber, trailing = if (user.isVerified) {
                    { Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = ProfilePalette.Green, modifier = Modifier.size(20.dp)) }
                } else null)
                ProfileInfoRow("Username", user.username)
                ProfileInfoRow("Account type", accountType(user.roles), showDivider = false)
            }
        }

        ProfileActionGroup(title = "Activity") {
            ProfileActionRow("My Bookings", Icons.AutoMirrored.Filled.EventNote, onBookingsClick, subtitle = "View and track your orders")
            ProfileActionRow("Help & Support", Icons.AutoMirrored.Filled.HelpOutline, onHelpClick, subtitle = "Call or WhatsApp CaterHub", showDivider = false)
        }

        ProfileActionGroup(title = "Account") {
            ProfileActionRow(
                "Logout", Icons.AutoMirrored.Filled.Logout, { showLogoutDialog = true },
                subtitle = "Sign out of this device", showChevron = false, enabled = !state.isDeleting
            )
            ProfileActionRow(
                "Delete Account", Icons.Filled.DeleteOutline, { showDeleteDialog = true },
                subtitle = "Permanently remove your account",
                destructive = true, showChevron = false, showDivider = false,
                enabled = !state.isSaving,
                loading = state.isDeleting
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
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }
    if (showDeleteDialog && !state.isSaving) {
        ProfileConfirmDialog(
            title = "Delete your CaterHub account?",
            message = "Deleting your account will permanently remove your account and associated personal data. This action cannot be undone.",
            confirmLabel = "Delete Account",
            destructive = true,
            icon = Icons.Filled.DeleteOutline,
            onConfirm = { showDeleteDialog = false; onDeleteAccount() },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun CustomerEditCard(
    user: UserDTO,
    isSaving: Boolean,
    saveError: String?,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var firstName by rememberSaveable(user.id) { mutableStateOf(user.firstName.orEmpty()) }
    var lastName by rememberSaveable(user.id) { mutableStateOf(user.lastName.orEmpty()) }
    var email by rememberSaveable(user.id) { mutableStateOf(user.email.orEmpty()) }
    var showErrors by rememberSaveable(user.id) { mutableStateOf(false) }
    val errors = CustomerProfileEditRules.validate(firstName, lastName, email)
    val visibleErrors = if (showErrors) errors else emptyMap()

    ProfileSectionCard(title = "Edit personal information", icon = Icons.Filled.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
            ProfileTextField(
                value = firstName,
                onValueChange = { firstName = it.take(CustomerProfileEditRules.MAX_NAME_LENGTH) },
                label = "First name",
                error = visibleErrors[Field.FIRST_NAME],
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            ProfileTextField(
                value = lastName,
                onValueChange = { lastName = it.take(CustomerProfileEditRules.MAX_NAME_LENGTH) },
                label = "Last name (optional)",
                error = visibleErrors[Field.LAST_NAME],
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            ProfileTextField(
                value = email,
                onValueChange = { email = it.trim().take(CustomerProfileEditRules.MAX_EMAIL_LENGTH) },
                label = "Email (optional)",
                error = visibleErrors[Field.EMAIL],
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
            )
            ProfileTextField(
                value = user.phoneNumber,
                onValueChange = {},
                label = "Mobile number",
                enabled = false,
                readOnly = true,
                supporting = "Verified by OTP. Contact support to change your mobile number.",
                trailingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = ProfilePalette.Muted) }
            )

            if (saveError != null) {
                Text(saveError, color = ProfilePalette.Danger, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfilePalette.Ink),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                Button(
                    onClick = {
                        showErrors = true
                        if (errors.isEmpty()) onSave(firstName, lastName, email)
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfilePalette.Burgundy, contentColor = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.White)
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun customerDisplayName(user: UserDTO): String = listOf(user.firstName, user.lastName)
    .filterNot { it.isNullOrBlank() }
    .joinToString(" ")
    .ifBlank { user.username }

private fun accountType(roles: List<String>): String = when {
    roles.any { it.contains("ADMIN") } -> "Admin"
    roles.any { it.contains("WORKER") } -> "Worker"
    else -> "Customer"
}

