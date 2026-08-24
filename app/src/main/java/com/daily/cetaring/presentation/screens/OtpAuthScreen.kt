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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R
import com.daily.cetaring.data.remote.dto.AuthResponse
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState
import kotlinx.coroutines.delay

private val Cream = Color(0xFFFFFCF6)
private val Maroon = Color(0xFF941820)
private val MaroonDark = Color(0xFF741017)
private val Green = Color(0xFF086526)
private val Gold = Color(0xFFC98B13)
private val Dark = Color(0xFF292522)
private val Muted = Color(0xFF716A63)
private val Border = Color(0xFFD6CEC0)
private val LightGreen = Color(0xFFEAF5EE)
private val LightMaroon = Color(0xFFFFEEEE)
private val DisabledButton = Color(0xFFE7E0D6)
private val DisabledText = Color(0xFF9A948C)

@Composable
fun OtpAuthScreen(
    viewModel: AuthViewModel,
    isRegistration: Boolean,
    userType: String,
    onBackClick: () -> Unit,
    onAuthSuccess: (AuthResponse) -> Unit,
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

        userType.equals("WORKER", ignoreCase = true) ->
            "REGISTER_WORKER"

        else ->
            "REGISTER_CUSTOMER"
    }

    val normalizedMobile = remember(mobile) {
        mobile.filter { it.isDigit() }.take(10)
    }

    /*
     * When OTP is successfully sent, start the resend timer.
     * When authentication succeeds, continue to the next screen.
     */
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

    /*
     * Resend countdown.
     */
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
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /*
             * TOP BAR
             *
             * Only back button here.
             *
             * Login/Create account switch is intentionally
             * NOT placed in the top-right anymore.
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Dark,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            /*
             * CATERHUB LOGO
             */
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                androidx.compose.foundation.Image(
                    painter = painterResource(
                        id = R.drawable.public_logo
                    ),
                    contentDescription = "CaterHub",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            /*
             * TITLE
             */
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = Maroon,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                color = Muted,
                fontSize = 17.sp,
                lineHeight = 25.sp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            /*
             * NAME
             *
             * Registration only.
             */
            if (isRegistration) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(80)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Full name")
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Dark,
                        fontSize = 17.sp
                    ),
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

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            /*
             * MOBILE NUMBER
             */
            OutlinedTextField(
                value = normalizedMobile,
                onValueChange = {
                    mobile = it
                        .filter { char -> char.isDigit() }
                        .take(10)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Mobile number")
                },
                prefix = {
                    Text(
                        text = "+91 ",
                        color = Dark,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = Maroon
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Dark,
                    fontSize = 17.sp
                ),
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

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            /*
             * SEND OTP
             *
             * Strong visible disabled state.
             */
            Button(
                onClick = {
                    viewModel.sendOtp(
                        mobileNumber = "+91$normalizedMobile",
                        purpose = purpose,
                        userType = userType
                    )
                },
                enabled =
                    normalizedMobile.length == 10 &&
                            otpState !is OtpUiState.Sending &&
                            cooldown == 0,
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
                            cooldown > 0 ->
                                "OTP Sent • ${cooldown}s"

                            otpState is OtpUiState.Sent ->
                                "Send OTP Again"

                            else ->
                                "Send OTP"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            /*
             * LOGIN <-> REGISTER
             *
             * THIS IS NOW BELOW SEND OTP.
             */
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextButton(
                onClick = onSwitchMode
            ) {
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

            /*
             * OTP AREA
             *
             * Only appears after Send OTP.
             */
            if (
                otpState is OtpUiState.Sent ||
                otpState is OtpUiState.Verifying ||
                otp.isNotEmpty()
            ) {

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGreen
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Enter your OTP",
                            color = Dark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        val sentState = otpState as? OtpUiState.Sent
                        Text(
                            text = sentState?.message?.takeIf { it.isNotBlank() }
                                ?: "Enter the 6-digit code sent to\n+91 $normalizedMobile",
                            color = Muted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        /*
                         * OTP INPUT
                         *
                         * Dark text is explicitly configured.
                         * This fixes the "OTP numbers not visible"
                         * problem from the screenshot.
                         */
                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                otp = it
                                    .filter { char ->
                                        char.isDigit()
                                    }
                                    .take(6)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("6-digit OTP")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
                            singleLine = true,
                            textStyle =
                                LocalTextStyle.current.copy(
                                    color = Dark,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 5.sp,
                                    textAlign = TextAlign.Center
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Dark,
                                    unfocusedTextColor = Dark,
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = Border,
                                    focusedLabelColor = Green,
                                    unfocusedLabelColor = Muted,
                                    cursorColor = Green
                                )
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        /*
                         * VERIFY OTP
                         */
                        Button(
                            onClick = {

                                viewModel.verifyOtp(
                                    mobileNumber =
                                        "+91$normalizedMobile",
                                    otp = otp,
                                    purpose = purpose,
                                    name =
                                        if (isRegistration) {
                                            name.trim()
                                        } else {
                                            null
                                        }
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
                                disabledContainerColor =
                                    DisabledButton,
                                disabledContentColor =
                                    DisabledText
                            )
                        ) {

                            if (
                                otpState is OtpUiState.Verifying
                            ) {

                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )

                            } else {

                                Text(
                                    text = "Verify OTP",
                                    fontSize = 16.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                viewModel.sendOtp(
                                    mobileNumber = "+91$normalizedMobile",
                                    purpose = purpose,
                                    userType = userType,
                                    channel = "VOICE"
                                )
                            },
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

                        /*
                         * RESEND
                         */
                        if (cooldown == 0) {

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            TextButton(
                                onClick = {

                                    viewModel.sendOtp(
                                        mobileNumber =
                                            "+91$normalizedMobile",
                                        purpose = purpose,
                                        userType = userType
                                    )
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Filled.Refresh,
                                    contentDescription =
                                        null,
                                    tint = Green,
                                    modifier =
                                        Modifier.size(18.dp)
                                )

                                Spacer(
                                    modifier =
                                        Modifier.size(5.dp)
                                )

                                Text(
                                    text = "Resend OTP",
                                    color = Green,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        } else {

                            Text(
                                text =
                                    "You can resend OTP in ${cooldown}s",
                                color = Muted,
                                fontSize = 13.sp,
                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )
                        }
                    }
                }
            }

            /*
             * ERROR
             */
            if (otpState is OtpUiState.Error) {

                val message =
                    (otpState as OtpUiState.Error).message

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightMaroon
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = Maroon,
                            modifier =
                                Modifier.size(28.dp)
                        )

                        Spacer(
                            modifier = Modifier.size(12.dp)
                        )

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

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            /*
             * TERMS
             */
            Text(
                text =
                    "By continuing, you agree to our Terms and Privacy Policy.",
                color = Color(0xFF8D8881),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}