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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.daily.cetaring.BuildConfig
import com.daily.cetaring.R
import com.daily.cetaring.auth.OtpAutoStatus
import com.daily.cetaring.auth.OtpEntryRules
import com.daily.cetaring.auth.OtpSource
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.presentation.components.PremiumOtpInput
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.delay

private val Ivory = Color(0xFFFBF8F1)
private val Emerald = Color(0xFF173F32)
private val EmeraldTint = Color(0xFFE7F0EB)
private val Maroon = Color(0xFF941B27)
private val MaroonDark = Color(0xFF6E141D)
private val Gold = Color(0xFFD9B979)
private val Ink = Color(0xFF26231F)
private val Muted = Color(0xFF716D65)
private val Border = Color(0xFFDDD5C6)
private val LightMaroon = Color(0xFFFBECEC)
private val DisabledButton = Color(0xFFE9E3D8)
private val DisabledText = Color(0xFF9A948C)
private const val OTP_LOG_TAG = "CaterHubOtpAuto"
private const val SLOW_SMS_HINT_MS = 25_000L

@Composable
fun OtpAuthScreen(
    viewModel: AuthViewModel,
    isRegistration: Boolean,
    userType: String,
    onBackClick: () -> Unit,
    onAuthSuccess: (AuthResponse) -> Unit,
    onSwitchMode: () -> Unit,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    showModeSwitch: Boolean = true,
    customerAccess: Boolean = false
) {
    val context = LocalContext.current
    val smsRetrieverClient = remember(context) { SmsRetriever.getClient(context) }
    val otpState by viewModel.otpUiState.collectAsState()
    val entry by viewModel.otpEntry.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var cooldown by rememberSaveable { mutableIntStateOf(0) }
    // Phase 2 (code entry) is shown once the backend confirms an OTP was sent.
    var codeRequested by rememberSaveable { mutableStateOf(false) }
    // Session id for which the SMS receiver/Retriever should be active (0 = none).
    var listeningSession by rememberSaveable { mutableIntStateOf(0) }
    var handledSentSession by rememberSaveable { mutableIntStateOf(-1) }
    var authSuccessHandled by rememberSaveable { mutableStateOf(false) }
    var errorCodeSnapshot by rememberSaveable { mutableStateOf<String?>(null) }
    var slowSmsHint by rememberSaveable { mutableStateOf(false) }
    var voiceRequested by rememberSaveable { mutableStateOf(false) }
    val otpFocus = remember { FocusRequester() }

    val title = titleOverride ?: if (isRegistration) "Create an account" else "Login with OTP"
    val subtitle = subtitleOverride ?: if (isRegistration) {
        "Verify your mobile number to get started."
    } else {
        "Use your mobile number to sign in securely."
    }
    val purpose = when {
        customerAccess -> AuthViewModel.CUSTOMER_ACCESS_PURPOSE
        !isRegistration -> "LOGIN"
        userType.equals("WORKER", ignoreCase = true) -> "REGISTER_WORKER"
        else -> "REGISTER_CUSTOMER"
    }
    val showNameField = isRegistration && !customerAccess
    val normalizedMobile = remember(mobile) { mobile.filter(Char::isDigit).take(10) }
    val registrationName = remember(name, showNameField) { if (showNameField) name.trim() else null }
    val isVerifying = otpState is OtpUiState.Verifying
    val isSending = otpState is OtpUiState.Sending
    val nameValid = !showNameField || name.trim().isNotEmpty()
    val canVerify = OtpEntryRules.canVerify(entry, isVerifying) && nameValid

    fun submitOtp() {
        if (!OtpEntryRules.canVerify(entry, isVerifying) || !nameValid) return
        authSuccessHandled = false
        if (customerAccess) {
            viewModel.verifyCustomerAccessOtp(mobileNumber = "+91$normalizedMobile", otp = entry.code)
        } else {
            viewModel.verifyOtp(
                mobileNumber = "+91$normalizedMobile",
                otp = entry.code,
                purpose = purpose,
                name = registrationName
            )
        }
    }

    fun requestOtp(channel: String? = null) {
        val listen = channel != "VOICE"
        val session = viewModel.beginOtpSession(listenForSms = listen)
        // Start listening before the request so an SMS that arrives quickly is not missed.
        listeningSession = if (listen) session else 0
        voiceRequested = !listen
        slowSmsHint = false
        errorCodeSnapshot = null
        authSuccessHandled = false
        viewModel.sendOtp(
            mobileNumber = "+91$normalizedMobile",
            purpose = purpose,
            userType = userType,
            channel = channel
        )
        logOtpEvent(if (listen) "Requested OTP via SMS channel" else "Requested OTP via voice channel")
    }

    fun changeNumber() {
        codeRequested = false
        listeningSession = 0
        errorCodeSnapshot = null
        viewModel.clearOtpEntry()
        viewModel.resetOtpState()
    }

    val currentSession by rememberUpdatedState(entry.sessionId)
    val onSmsMessage by rememberUpdatedState(newValue = { message: String, session: Int ->
        logOtpEvent("SMS message received for active session")
        viewModel.onOtpSmsReceived(message, session)
    })
    val onAutoEnded by rememberUpdatedState(newValue = { session: Int, status: OtpAutoStatus ->
        viewModel.onOtpAutoDetectionEnded(session, status)
        if (listeningSession == session) listeningSession = 0
    })

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val session = currentSession
        if (result.resultCode != Activity.RESULT_OK) {
            logOtpEvent("SMS User Consent dismissed")
            onAutoEnded(session, OtpAutoStatus.UNAVAILABLE)
            return@rememberLauncherForActivityResult
        }
        val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).orEmpty()
        onSmsMessage(message, session)
    }

    LaunchedEffect(otpState) {
        when (val state = otpState) {
            is OtpUiState.Sent -> {
                if (handledSentSession == entry.sessionId) return@LaunchedEffect
                handledSentSession = entry.sessionId
                codeRequested = true
                cooldown = state.resendCooldownSeconds
                if (!state.deliveryChannel.equals("SMS", ignoreCase = true)) {
                    onAutoEnded(entry.sessionId, OtpAutoStatus.UNAVAILABLE)
                }
                logOtpEvent("Send OTP succeeded")
            }
            is OtpUiState.Success -> {
                if (authSuccessHandled) return@LaunchedEffect
                authSuccessHandled = true
                listeningSession = 0
                onAuthSuccess(state.response)
            }
            is OtpUiState.Error -> {
                state.retryAfterSeconds?.let { cooldown = it }
                errorCodeSnapshot = entry.code
                // A failed send means no SMS is coming for this session.
                if (!codeRequested) onAutoEnded(entry.sessionId, OtpAutoStatus.UNAVAILABLE)
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

    // Gentle hint only; the receiver keeps listening and the input is always editable.
    LaunchedEffect(entry.sessionId, entry.autoStatus) {
        if (entry.autoStatus != OtpAutoStatus.LISTENING) return@LaunchedEffect
        delay(SLOW_SMS_HINT_MS)
        slowSmsHint = true
    }

    LaunchedEffect(codeRequested) {
        if (codeRequested) {
            delay(250)
            runCatching { otpFocus.requestFocus() }
        }
    }

    DisposableEffect(listeningSession, context) {
        val session = listeningSession
        if (session == 0) return@DisposableEffect onDispose { }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val extras = intent.extras ?: return
                val status = smsStatusFromExtras(extras) ?: return
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val smsMessage = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                        if (!smsMessage.isNullOrBlank()) {
                            onSmsMessage(smsMessage, session)
                            return
                        }
                        val consentIntent = consentIntentFromExtras(extras)
                        val current = viewModel.otpEntry.value
                        val userTyping = current.manuallyEdited && current.code.isNotEmpty()
                        if (consentIntent != null && !userTyping) {
                            logOtpEvent("Launching SMS User Consent prompt")
                            runCatching { consentLauncher.launch(consentIntent) }
                                .onFailure { onAutoEnded(session, OtpAutoStatus.UNAVAILABLE) }
                        } else if (consentIntent == null) {
                            onAutoEnded(session, OtpAutoStatus.UNAVAILABLE)
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        logOtpEvent("SMS Retriever timeout")
                        onAutoEnded(session, OtpAutoStatus.TIMED_OUT)
                    }
                }
            }
        }

        val registered = runCatching {
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                ContextCompat.RECEIVER_EXPORTED
            )
        }.isSuccess
        if (registered) {
            smsRetrieverClient.startSmsRetriever()
                .addOnFailureListener { logOtpEvent("SMS Retriever failed to start") }
            smsRetrieverClient.startSmsUserConsent(null)
                .addOnFailureListener {
                    logOtpEvent("SMS User Consent failed to start")
                    onAutoEnded(session, OtpAutoStatus.UNAVAILABLE)
                }
        } else {
            onAutoEnded(session, OtpAutoStatus.UNAVAILABLE)
        }

        onDispose {
            if (registered) runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val registrationState = otpState as? OtpUiState.RegistrationRequired
    if (registrationState != null) {
        CustomerAccountCreationScreen(
            state = registrationState,
            onContinue = viewModel::completeCustomerRegistration,
            onBackClick = { changeNumber() }
        )
        return
    }

    val errorMessage = (otpState as? OtpUiState.Error)?.message
    // Hide a verification error as soon as the customer edits the code.
    val showCodeError = codeRequested && errorMessage != null && errorCodeSnapshot == entry.code

    Surface(modifier = Modifier.fillMaxSize(), color = Ivory) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (codeRequested) changeNumber() else onBackClick() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            OtpBrandMark()
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = codeRequested,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "otp-phase"
            ) { verifyPhase ->
                if (!verifyPhase) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(text = title, color = Emerald, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = subtitle, color = Muted, fontSize = 16.sp, lineHeight = 23.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        if (showNameField) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(80) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full name") },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = Ink, fontSize = 17.sp),
                                shape = RoundedCornerShape(14.dp),
                                colors = otpFieldColors()
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        OutlinedTextField(
                            value = normalizedMobile,
                            onValueChange = { mobile = it.filter(Char::isDigit).take(10) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mobile number") },
                            prefix = { Text("+91 ", color = Ink, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (normalizedMobile.length == 10 && !isSending && cooldown == 0 && nameValid) requestOtp()
                            }),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Ink, fontSize = 17.sp),
                            shape = RoundedCornerShape(14.dp),
                            colors = otpFieldColors()
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        PrimaryOtpButton(
                            text = when {
                                cooldown > 0 -> "Try again in ${cooldown}s"
                                else -> "Send OTP"
                            },
                            loading = isSending,
                            enabled = normalizedMobile.length == 10 && nameValid && !isSending && cooldown == 0,
                            container = Maroon,
                            onClick = { requestOtp() }
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OtpErrorBanner(errorMessage)
                        }

                        if (showModeSwitch) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onSwitchMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(
                                    text = if (isRegistration) "Already have an account? Login" else "New to CaterHub? Create an account",
                                    color = Emerald,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Verify your mobile number",
                            color = Emerald,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (voiceRequested) "We're calling you with a 6-digit code on" else "Enter the 6-digit code sent to",
                            color = Muted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                OtpEntryRules.maskMobile(normalizedMobile),
                                color = Ink,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { changeNumber() }, enabled = !isVerifying) {
                                Text("Change", color = Maroon, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        PremiumOtpInput(
                            code = entry.code,
                            onCodeChange = viewModel::onOtpInput,
                            isError = showCodeError,
                            readOnly = isVerifying,
                            autoFilled = entry.source == OtpSource.AUTO,
                            focusRequester = otpFocus,
                            onDone = { submitOtp() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OtpStatusLine(
                            status = entry.autoStatus,
                            source = entry.source,
                            verifying = isVerifying,
                            slowHint = slowSmsHint,
                            voice = voiceRequested
                        )

                        if (showCodeError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OtpErrorBanner(errorMessage.orEmpty())
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        PrimaryOtpButton(
                            text = "Verify & continue",
                            loading = isVerifying,
                            enabled = canVerify,
                            container = Emerald,
                            onClick = { submitOtp() }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (cooldown > 0) {
                            Text(
                                "Resend code in ${cooldown / 60}:${(cooldown % 60).toString().padStart(2, '0')}",
                                color = Muted,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { requestOtp() }, enabled = !isSending && !isVerifying) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Emerald, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Resend OTP", color = Emerald, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { requestOtp(channel = "VOICE") }, enabled = !isSending && !isVerifying) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = Maroon, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Call me", color = Maroon, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (isSending) {
                            Text("Sending a new code…", color = Muted, fontSize = 13.sp)
                        }
                        if (!showCodeError && errorMessage != null && entry.code.isEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OtpErrorBanner(errorMessage)
                        }
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

@Composable
private fun OtpBrandMark() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(1.dp, Gold.copy(alpha = 0.6f), RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.public_logo),
            contentDescription = "CaterHub",
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun OtpStatusLine(status: OtpAutoStatus, source: OtpSource, verifying: Boolean, slowHint: Boolean, voice: Boolean) {
    val (icon, text, tint) = when {
        verifying -> Triple(null, "Verifying your code…", Emerald)
        source == OtpSource.AUTO -> Triple(Icons.Filled.CheckCircle, "Code detected from SMS. Check it and tap Verify.", Emerald)
        voice -> Triple(Icons.Filled.Phone, "Type the code you hear on the call.", Muted)
        status == OtpAutoStatus.LISTENING && slowHint ->
            Triple(Icons.Filled.Sms, "Still waiting for the SMS. You can type the code yourself.", Muted)
        status == OtpAutoStatus.LISTENING -> Triple(Icons.Filled.Sms, "Waiting for SMS… you can also type the code.", Muted)
        else -> Triple(Icons.Filled.Sms, "Type the 6-digit code from your SMS.", Muted)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        if (icon == null) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = tint, strokeWidth = 2.dp)
        } else {
            OtpStatusIcon(icon, tint)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = tint, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OtpStatusIcon(icon: ImageVector, tint: Color) {
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (tint == Emerald) EmeraldTint else Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun PrimaryOtpButton(text: String, loading: Boolean, enabled: Boolean, container: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = Color.White,
            disabledContainerColor = DisabledButton,
            disabledContentColor = DisabledText
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OtpErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightMaroon)
            .padding(14.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Maroon, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.size(10.dp))
        Text(text = message, color = MaroonDark, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun otpFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedBorderColor = Emerald,
    unfocusedBorderColor = Border,
    focusedLabelColor = Emerald,
    unfocusedLabelColor = Muted,
    focusedLeadingIconColor = Emerald,
    unfocusedLeadingIconColor = Muted,
    cursorColor = Emerald
)

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
