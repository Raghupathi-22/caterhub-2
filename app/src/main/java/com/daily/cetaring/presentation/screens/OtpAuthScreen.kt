package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState

@Composable
fun OtpAuthScreen(
    viewModel: AuthViewModel,
    isRegistration: Boolean,
    userType: String,
    onBackClick: () -> Unit,
    onAuthSuccess: (AuthResponse) -> Unit,
    onSwitchMode: () -> Unit
) {
    val otpUiState by viewModel.otpUiState.collectAsState()
    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val titleText = if (isRegistration) "Create an account" else "Login with OTP"
    val subtitleText = if (isRegistration) "Verify your phone number to get started." else "Use your mobile number to sign in securely."
    val purpose = if (isRegistration) "register" else "login"

    LaunchedEffect(otpUiState) {
        val state = otpUiState
        if (state is OtpUiState.Success) {
            onAuthSuccess(state.response)
        }
    }

    Scaffold(containerColor = Color(0xFFFFFCF6)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFCF6))
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onSwitchMode) {
                    Text(if (isRegistration) "Login instead" else "Create account")
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8E171C)),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Text(titleText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8E171C))
            Text(subtitleText, fontSize = 15.sp, color = Color(0xFF6C655D))

            if (isRegistration) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it.filter { ch -> ch.isDigit() || ch == '+' } },
                label = { Text("Mobile number") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (mobileNumber.isNotBlank()) {
                        viewModel.sendOtp(mobileNumber.trim(), purpose, userType)
                    }
                },
                enabled = otpUiState !is OtpUiState.Sending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E171C)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (otpUiState is OtpUiState.Sending) "Sending OTP..." else "Send OTP")
            }

            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it.filter { ch -> ch.isDigit() } },
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (mobileNumber.isNotBlank() && otpCode.isNotBlank()) {
                        viewModel.verifyOtp(mobileNumber.trim(), otpCode.trim(), purpose, name.takeIf { it.isNotBlank() })
                    }
                },
                enabled = otpUiState !is OtpUiState.Verifying,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5B22)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (otpUiState is OtpUiState.Verifying) "Verifying..." else "Verify OTP")
            }

            when (val state = otpUiState) {
                is OtpUiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFFB3261E))
                            Spacer(Modifier.size(8.dp))
                            Text(state.message, color = Color(0xFFB3261E), fontSize = 13.sp)
                        }
                    }
                }
                is OtpUiState.Sent -> {
                    Text("OTP sent. Code expires in ${state.resendCooldownSeconds}s.", color = Color(0xFF0D5B22), fontWeight = FontWeight.Medium)
                }
                else -> Unit
            }

            Text(
                text = "By continuing, you agree to our terms and privacy policy.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
