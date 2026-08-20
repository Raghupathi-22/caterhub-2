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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState
import kotlinx.coroutines.delay

private val Cream = Color(0xFFFFFCF6)
private val Maroon = Color(0xFF941820)
private val Green = Color(0xFF086526)
private val Gold = Color(0xFFC98B13)
private val Dark = Color(0xFF292522)
private val Muted = Color(0xFF716A63)
private val Border = Color(0xFFDCD5C8)
private val ErrorBg = Color(0xFFFFEEEE)

@Composable
fun OtpAuthScreen(
    viewModel: AuthViewModel,
    isRegistration: Boolean,
    userType: String,
    onBackClick: () -> Unit,
    onAuthSuccess: (com.daily.cetaring.data.remote.dto.AuthResponse) -> Unit,
    onSwitchMode: () -> Unit
) {
    val otpState by viewModel.otpUiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    var cooldown by remember { mutableIntStateOf(0) }

    val title = if (isRegistration) {
        "Create an account"
    } else {
        "Login with OTP"
    }

    val subtitle = if (isRegistration) {
        "Verify your mobile number to get started."
    } else {
        "Use your mobile number to sign in securely."
    }

    val purpose = when {
        !isRegistration -> "LOGIN"
        userType.equals("WORKER", ignoreCase = true) -> "REGISTER_WORKER"
        else -> "REGISTER_CUSTOMER"
    }

    val normalizedMobile = remember(mobile) {
        mobile.filter { it.isDigit() }.take(10)
    }

    LaunchedEffect(otpState) {
        when (val state = otpState) {
            is OtpUiState.Sent -> {
                cooldown = state.resendCooldownSeconds
            }

            is OtpUiState.Success -> {
                onAuthSuccess(state.response)
            }

            else -> Unit
        }
    }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown--
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Dark
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onSwitchMode) {
                    Text(
                        text = if (isRegistration) "Login instead" else "Create account",
                        color = Color(0xFF8FCDBD),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Security icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Maroon),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(58.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = Maroon,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                color = Muted,
                fontSize = 17.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Registration name
            if (isRegistration) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(80)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Mobile
            OutlinedTextField(
                value = normalizedMobile,
                onValueChange = {
                    mobile = it.filter { char -> char.isDigit() }.take(10)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile number") },
                prefix = {
                    Text(
                        "+91 ",
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Phone,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Send OTP
            Button(
                onClick = {
                    viewModel.sendOtp(
                        mobileNumber = "+91$normalizedMobile",
                        purpose = purpose,
                        userType = userType
                    )
                },
                enabled = normalizedMobile.length == 10 &&
                        otpState !is OtpUiState.Sending &&
                        cooldown == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Maroon
                )
            ) {
                if (otpState is OtpUiState.Sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when {
                            cooldown > 0 -> "Resend OTP in ${cooldown}s"
                            otpState is OtpUiState.Sent -> "Resend OTP"
                            else -> "Send OTP"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // OTP section
            if (
                otpState is OtpUiState.Sent ||
                otpState is OtpUiState.Verifying ||
                otp.isNotEmpty()
            ) {
                Spacer(modifier = Modifier.height(26.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F8F3)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter verification code",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Dark
                        )

                        Text(
                            text = "We sent a 6-digit OTP to +91 $normalizedMobile",
                            color = Muted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 5.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                otp = it.filter { char -> char.isDigit() }.take(6)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("6-digit OTP") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.verifyOtp(
                                    mobileNumber = "+91$normalizedMobile",
                                    otp = otp,
                                    purpose = purpose,
                                    name = if (isRegistration) name.trim() else null
                                )
                            },
                            enabled = otp.length == 6 &&
                                    otpState !is OtpUiState.Verifying &&
                                    (!isRegistration || name.trim().isNotEmpty()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Green
                            )
                        ) {
                            if (otpState is OtpUiState.Verifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(21.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Verify OTP",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (cooldown == 0) {
                            TextButton(
                                onClick = {
                                    viewModel.sendOtp(
                                        mobileNumber = "+91$normalizedMobile",
                                        purpose = purpose,
                                        userType = userType
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.size(5.dp))

                                Text("Resend OTP")
                            }
                        }
                    }
                }
            }

            // Error
            if (otpState is OtpUiState.Error) {
                val message = (otpState as OtpUiState.Error).message

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorBg
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Maroon,
                            modifier = Modifier.size(26.dp)
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Text(
                            text = message,
                            color = Maroon,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "By continuing, you agree to our Terms and Privacy Policy.",
                color = Color(0xFFAAA69F),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}