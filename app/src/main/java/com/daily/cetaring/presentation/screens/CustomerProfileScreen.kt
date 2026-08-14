package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.UserDTO
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton
import com.daily.cetaring.presentation.components.CaterHubStatusChip
import com.daily.cetaring.presentation.components.SummaryRow
import com.daily.cetaring.presentation.viewmodel.CustomerProfileUiState
import com.daily.cetaring.presentation.viewmodel.CustomerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: CustomerProfileViewModel,
    onBackClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(uiState) { if (uiState is CustomerProfileUiState.LoggedOut) onLoggedOut() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            CustomerProfileUiState.Idle, CustomerProfileUiState.Loading -> CaterHubLoadingState("Loading your profile...")
            is CustomerProfileUiState.Error -> Column(Modifier.padding(padding).padding(20.dp)) {
                CaterHubErrorState(message = state.message, onRetry = { viewModel.loadProfile() })
            }
            is CustomerProfileUiState.Loaded -> ProfileContent(
                user = state.user,
                saved = state.saved,
                onSave = viewModel::saveProfile,
                onBookingsClick = onBookingsClick,
                onNotificationsClick = onNotificationsClick,
                onHelpClick = onHelpClick,
                onLogout = { viewModel.logout() },
                modifier = Modifier.padding(padding)
            )
            CustomerProfileUiState.LoggedOut -> Unit
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserDTO,
    saved: Boolean,
    onSave: (String, String, String, String) -> Unit,
    onBookingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editMode by remember(user.id) { mutableStateOf(false) }
    var firstName by remember(user.id) { mutableStateOf(user.firstName.orEmpty()) }
    var lastName by remember(user.id) { mutableStateOf(user.lastName.orEmpty()) }
    var email by remember(user.id) { mutableStateOf(user.email.orEmpty()) }
    var phone by remember(user.id) { mutableStateOf(user.phoneNumber) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(fullName(user), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("@${user.username}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                CaterHubStatusChip(accountType(user.roles))
            }
        }

        if (saved) Text("Profile saved successfully.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Personal information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    TextButton(onClick = { editMode = !editMode }) { Text(if (editMode) "Cancel" else "Edit") }
                }

                if (editMode) {
                    OutlinedTextField(firstName, { firstName = it }, label = { Text("First name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(lastName, { lastName = it }, label = { Text("Last name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(email, { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(phone, { phone = it.filter { char -> char.isDigit() || char == '+' }.take(20) }, label = { Text("Mobile number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                    CaterHubPrimaryButton("Save changes", onClick = { onSave(firstName, lastName, email, phone); editMode = false }, modifier = Modifier.fillMaxWidth())
                } else {
                    SummaryRow("First name", user.firstName.orEmpty())
                    SummaryRow("Last name", user.lastName.orEmpty())
                    SummaryRow("Email", user.email.orEmpty())
                    SummaryRow("Mobile number", user.phoneNumber)
                    SummaryRow("Username", user.username)
                    SummaryRow("Account type", accountType(user.roles))
                }
            }
        }

        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                CaterHubSecondaryButton("My bookings", onBookingsClick, Modifier.fillMaxWidth())
                CaterHubSecondaryButton("Notifications", onNotificationsClick, Modifier.fillMaxWidth())
                CaterHubSecondaryButton("Help & Support", onHelpClick, Modifier.fillMaxWidth())
                CaterHubSecondaryButton("Logout", onLogout, Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Securely signs out of this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun fullName(user: UserDTO): String = listOf(user.firstName, user.lastName)
    .filterNot { it.isNullOrBlank() }
    .joinToString(" ")
    .ifBlank { user.username }

private fun accountType(roles: List<String>): String = when {
    roles.any { it.contains("ADMIN") } -> "Admin"
    roles.any { it.contains("WORKER") } -> "Worker"
    else -> "Customer"
}
