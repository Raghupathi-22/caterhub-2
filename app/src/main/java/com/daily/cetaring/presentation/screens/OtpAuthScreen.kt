package com.daily.cetaring.presentation.screens

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.daily.cetaring.BuildConfig
import com.daily.cetaring.R
import com.daily.cetaring.auth.OtpMessageParser
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.delay

private val Cream = Color(0xFFFFFCF6)
private val Maroon = Color(0xFF941820)
private val MaroonDark = Color(0xFF741017)
private val Green = Color(0xFF086526)
private val Dark = Color(0xFF292522)
private val Muted = Color(0xFF716A63)
private val Border = Color(0xFFD6CEC0)
private val LightGreen = Color(0xFFEAF5EE)
private val LightMaroon = Color(0xFFFFEEEE)
private val DisabledButton = Color(0xFFE7E0D6)
private val DisabledText = Color(0xFF9A948C)
private const val OTP_LOG_TAG = "CaterHubOtpAuto"

@Composable
fun OtpAuthScreen(
    viewModel: AuthViewModel,
    isRegistration: Boolean,
    userType: String,
    onBackClick: () -> Unit,
    onAuthSuccess: (AuthResponse) -> Unit,
    onSwitchMode: () -> Unit
) {
    val context = LocalContext.current
    val smsRetrieverClient = remember(context) { SmsRetriever.getClient(context) }
    val otpState by viewModel.otpUiState.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var cooldown by rememberSaveable { mutableIntStateOf(0) }

    var waitingForAutoOtp by rememberSaveable { mutableStateOf(false) }
    var allowManualFallback by rememberSaveable { mutableStateOf(false) }
    var isAutoFilledOtp by rememberSaveable { mutableStateOf(false) }
    var autoOtpStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var otpSessionId by rememberSaveable { mutableIntStateOf(0) }
    var lastSubmittedOtpKey by rememberSaveable { mutableStateOf("") }
    var authSuccessHandled by rememberSaveable { mutableStateOf(false) }

    val title = if (isRegistration) "Create an account" else "Login with OTP"
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
    val normalizedMobile = remember(mobile) { mobile.filter(Char::isDigit).take(10) }
    val registrationName = remember(name, isRegistration) { if (isRegistration) name.trim() else null }
    val hasSmsSession = otpState is OtpUiState.Sent || otpState is OtpUiState.Verifying || otp.isNotEmpty()

    val onSmsMessageReceived by rememberUpdatedState(newValue = { smsMessage: String ->
        logOtpEvent("OTP message received from retriever")
        val detectedOtp = OtpMessageParser.extractOtp(smsMessage) ?: return@rememberUpdatedState
        if (!waitingForAutoOtp || otpState !is OtpUiState.Sent) {
            logOtpEvent("OTP ignored due to inactive session/state")
            return@rememberUpdatedState
        }
        otp = detectedOtp
        isAutoFilledOtp = true
        waitingForAutoOtp = false
        allowManualFallback = false
        autoOtpStatus = "OTP detected"
        logOtpEvent("6-digit OTP detected and state updated")
    })

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            logOtpEvent("SMS User Consent dismissed/cancelled")
            waitingForAutoOtp = false
            allowManualFallback = true
            if (otp.isBlank()) autoOtpStatus = "Didn't receive OTP? Enter it manually"
            return@rememberLauncherForActivityResult
        }
        logOtpEvent("SMS User Consent returned message")
        val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).orEmpty()
        onSmsMessageReceived(message)
    }

    fun requestOtp(channel: String? = null) {
        otp = ""
        isAutoFilledOtp = false
        waitingForAutoOtp = false
        allowManualFallback = false
        autoOtpStatus = if (channel == "VOICE") {
            "We are calling you with the OTP."
        } else {
            "Requesting OTP..."
        }
        lastSubmittedOtpKey = ""
        authSuccessHandled = false
        viewModel.sendOtp(
            mobileNumber = "+91$normalizedMobile",
            purpose = purpose,
            userType = userType,
            channel = channel
        )
        logOtpEvent(if (channel == "VOICE") "Requested OTP via voice channel" else "Requested OTP via SMS channel")
    }

    LaunchedEffect(otpState) {
        when (val state = otpState) {
            is OtpUiState.Sent -> {
                logOtpEvent("Send OTP succeeded")
                cooldown = state.resendCooldownSeconds
                otp = ""
                isAutoFilledOtp = false
                lastSubmittedOtpKey = ""
                authSuccessHandled = false
                if (state.deliveryChannel.equals("SMS", ignoreCase = true)) {
                    waitingForAutoOtp = true
                    allowManualFallback = false
                    autoOtpStatus = "Waiting for OTP..."
                    otpSessionId++
                    logOtpEvent("Waiting for OTP and starting retriever session")
                } else {
                    waitingForAutoOtp = false
                    allowManualFallback = true
                    autoOtpStatus = state.message
                    logOtpEvent("SMS not active; switched to manual/voice flow")
                }
            }

            is OtpUiState.Verifying -> logOtpEvent("Starting automatic OTP verification")
            is OtpUiState.Success -> {
                if (authSuccessHandled) return@LaunchedEffect
                authSuccessHandled = true
                logOtpEvent("OTP verification completed successfully")
                onAuthSuccess(state.response)
            }
            is OtpUiState.Error -> {
                authSuccessHandled = false
                logOtpEvent("OTP flow received error state")
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

    LaunchedEffect(waitingForAutoOtp, otpSessionId) {
        if (!waitingForAutoOtp) return@LaunchedEffect
        delay(25000)
        if (waitingForAutoOtp && otpState is OtpUiState.Sent && otp.isBlank()) {
            waitingForAutoOtp = false
            allowManualFallback = true
            autoOtpStatus = "Didn't detect OTP. You can enter it manually."
            logOtpEvent("Auto OTP wait timed out; switched to manual fallback")
        }
    }

    LaunchedEffect(otp, isAutoFilledOtp, otpState, normalizedMobile, purpose, registrationName, otpSessionId) {
        if (!isAutoFilledOtp || otp.length != 6 || otpState !is OtpUiState.Sent) return@LaunchedEffect
        val requestKey = listOf(normalizedMobile, purpose, otp, registrationName.orEmpty(), otpSessionId).joinToString("|")
        if (lastSubmittedOtpKey == requestKey) return@LaunchedEffect
        lastSubmittedOtpKey = requestKey
        autoOtpStatus = "Verifying OTP..."
        logOtpEvent("Starting automatic OTP verification")
        viewModel.verifyOtp(
            mobileNumber = "+91$normalizedMobile",
            otp = otp,
            purpose = purpose,
            name = registrationName
        )
    }

    DisposableEffect(otpSessionId, waitingForAutoOtp, context) {
        if (!waitingForAutoOtp) return@DisposableEffect onDispose { }
        logOtpEvent("Registering OTP receiver")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                logOtpEvent("SMS Retriever broadcast received")
                val extras = intent.extras ?: return
                val status = smsStatusFromExtras(extras) ?: return
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        logOtpEvent("SMS Retriever status: SUCCESS")
                        val smsMessage = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                        if (!smsMessage.isNullOrBlank()) {
                            onSmsMessageReceived(smsMessage)
                        } else {
                            val consentIntent = consentIntentFromExtras(extras)
                            if (consentIntent != null) {
                                logOtpEvent("Launching SMS User Consent prompt")
                                consentLauncher.launch(consentIntent)
                            } else {
                                waitingForAutoOtp = false
                                allowManualFallback = true
                                if (otp.isBlank()) autoOtpStatus = "Didn't detect OTP. You can enter it manually."
                                logOtpEvent("No SMS message/consent intent in SUCCESS broadcast")
                            }
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        logOtpEvent("SMS Retriever status: TIMEOUT")
                        waitingForAutoOtp = false
                        allowManualFallback = true
                        if (otp.isBlank()) autoOtpStatus = "Didn't detect OTP. You can enter it manually."
                    }
                }
            }
        }

        runCatching {
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                ContextCompat.RECEIVER_EXPORTED
            )
        }.onSuccess {
            logOtpEvent("OTP receiver registered")
        }.onFailure {
            waitingForAutoOtp = false
            allowManualFallback = true
            if (otp.isBlank()) autoOtpStatus = "Auto-detect unavailable. Enter OTP manually."
            logOtpEvent("OTP receiver registration failed")
        }

        smsRetrieverClient.startSmsRetriever()
            .addOnSuccessListener { logOtpEvent("OTP retriever started") }
            .addOnFailureListener {
                logOtpEvent("OTP retriever failed to start")
                if (waitingForAutoOtp) {
                    waitingForAutoOtp = false
                    allowManualFallback = true
                    if (otp.isBlank()) autoOtpStatus = "Auto-detect unavailable. Enter OTP manually."
                }
            }
        smsRetrieverClient.startSmsUserConsent(null)
            .addOnSuccessListener { logOtpEvent("SMS User Consent listener started") }
            .addOnFailureListener { logOtpEvent("SMS User Consent listener failed to start") }

        onDispose {
            logOtpEvent("Unregistering OTP receiver")
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Dark, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.public_logo),
                    contentDescription = "CaterHub",
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(text = title, modifier = Modifier.fillMaxWidth(), color = Maroon, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 38.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subtitle, modifier = Modifier.fillMaxWidth(), color = Muted, fontSize = 17.sp, lineHeight = 25.sp)
            Spacer(modifier = Modifier.height(28.dp))

            if (isRegistration) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = Dark, fontSize = 17.sp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        focusedBorderColor = Maroon,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Maroon,
                        unfocusedLabelColor = Muted,
                        cursorColor = Maroon
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = normalizedMobile,
                onValueChange = { mobile = it.filter(Char::isDigit).take(10) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile number") },
                prefix = { Text("+91 ", color = Dark, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Maroon) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Dark, fontSize = 17.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Dark,
                    unfocusedTextColor = Dark,
                    focusedBorderColor = Maroon,
                    unfocusedBorderColor = Border,
                    focusedLabelColor = Maroon,
                    unfocusedLabelColor = Muted,
                    focusedLeadingIconColor = Maroon,
                    unfocusedLeadingIconColor = Muted,
                    cursorColor = Maroon
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { requestOtp() },
                enabled = normalizedMobile.length == 10 && otpState !is OtpUiState.Sending && cooldown == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Maroon,
                    contentColor = Color.White,
                    disabledContainerColor = DisabledButton,
                    disabledContentColor = DisabledText
                )
            ) {
                if (otpState is OtpUiState.Sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = when {
                            cooldown > 0 -> "OTP Sent • ${cooldown}s"
                            otpState is OtpUiState.Sent -> "Send OTP Again"
                            else -> "Send OTP"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSwitchMode) {
                Text(
                    text = if (isRegistration) {
                        "Already have an account? Login"
                    } else {
                        "New to CaterHub? Create an account"
                    },
                    color = Green,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (hasSmsSession) {
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when {
                                otpState is OtpUiState.Verifying -> "Verifying OTP..."
                                waitingForAutoOtp -> "Waiting for OTP..."
                                isAutoFilledOtp -> "OTP detected"
                                else -> "Enter your OTP"
                            },
                            color = Dark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val sentState = otpState as? OtpUiState.Sent
                        Text(
                            text = autoOtpStatus?.takeIf { it.isNotBlank() }
                                ?: sentState?.message?.takeIf { it.isNotBlank() }
                                ?: "Enter the 6-digit code sent to\n+91 $normalizedMobile",
                            color = Muted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                if (allowManualFallback) {
                                    otp = it.filter(Char::isDigit).take(6)
                                    isAutoFilledOtp = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("6-digit OTP") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            enabled = allowManualFallback && otpState !is OtpUiState.Verifying,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                color = Dark,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 5.sp,
                                textAlign = TextAlign.Center
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Dark,
                                unfocusedTextColor = Dark,
                                focusedBorderColor = Green,
                                unfocusedBorderColor = Border,
                                focusedLabelColor = Green,
                                unfocusedLabelColor = Muted,
                                cursorColor = Green
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (allowManualFallback) {
                            Button(
                                onClick = {
                                    lastSubmittedOtpKey = ""
                                    viewModel.verifyOtp(
                                        mobileNumber = "+91$normalizedMobile",
                                        otp = otp,
                                        purpose = purpose,
                                        name = registrationName
                                    )
                                },
                                enabled =
                                    otp.length == 6 &&
                                        otpState !is OtpUiState.Verifying &&
                                        (
                                            !isRegistration ||
                                                name.trim().isNotEmpty()
                                            ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green,
                                    contentColor = Color.White,
                                    disabledContainerColor = DisabledButton,
                                    disabledContentColor = DisabledText
                                )
                            ) {
                                if (otpState is OtpUiState.Verifying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = "Verify OTP",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = { requestOtp(channel = "VOICE") },
                            enabled = otpState !is OtpUiState.Sending
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = Maroon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Didn't get SMS? Call me with OTP",
                                color = Maroon,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (cooldown == 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = { requestOtp() }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.size(5.dp))

                                Text(
                                    text = "Resend OTP",
                                    color = Green,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "You can resend OTP in ${cooldown}s",
                                color = Muted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            if (otpState is OtpUiState.Error) {
                val message = (otpState as OtpUiState.Error).message

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightMaroon)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = Maroon,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = message,
                            color = MaroonDark,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "By continuing, you agree to our Terms and Privacy Policy.",
                color = Color(0xFF8D8881),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun consentIntentFromExtras(extras: Bundle): Intent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java)
    } else {
        @Suppress("DEPRECATION")
        extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
    }
}

private fun smsStatusFromExtras(extras: Bundle): Status? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
    } else {
        @Suppress("DEPRECATION")
        extras.getParcelable(SmsRetriever.EXTRA_STATUS)
    }
}

private fun logOtpEvent(event: String) {
    if (BuildConfig.DEBUG) {
        Log.d(OTP_LOG_TAG, event)
    }
}
