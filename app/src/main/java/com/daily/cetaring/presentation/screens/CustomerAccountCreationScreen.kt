package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.OtpUiState

private val AccountCream = Color(0xFFFFFCF6)
private val AccountMaroon = Color(0xFF941820)
private val AccountMaroonDark = Color(0xFF741017)
private val AccountGreen = Color(0xFF086526)
private val AccountGreenTint = Color(0xFFEAF4EC)
private val AccountDark = Color(0xFF292522)
private val AccountMuted = Color(0xFF716A63)
private val AccountBorder = Color(0xFFD6CEC0)
private val AccountErrorTint = Color(0xFFFFEEEE)
private val AccountDisabledButton = Color(0xFFE7E0D6)
private val AccountDisabledText = Color(0xFF9A948C)

/** Formats "+919876543210" as "+91 98765 43210" for display only. */
internal fun formatVerifiedMobile(mobile: String): String {
    val digits = mobile.filter(Char::isDigit)
    val local = if (digits.length == 12 && digits.startsWith("91")) digits.substring(2) else digits
    return if (local.length == 10) "+91 ${local.substring(0, 5)} ${local.substring(5)}" else mobile
}

/**
 * Shared "create account" step of the unified customer sign-in flow. Shown only
 * after the backend has verified the OTP for a number that has no account.
 */
@Composable
fun CustomerAccountCreationScreen(
    state: OtpUiState.RegistrationRequired,
    onContinue: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var fullName by rememberSaveable(state.registrationToken) { mutableStateOf("") }
    var attempted by rememberSaveable(state.registrationToken) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val localError = if (attempted) AuthViewModel.validateFullName(fullName) else null
    val errorMessage = localError ?: state.errorMessage

    fun submit() {
        attempted = true
        focusManager.clearFocus()
        if (AuthViewModel.validateFullName(fullName) == null) onContinue(fullName)
    }

    BackHandler(enabled = !state.isSubmitting, onBack = onBackClick)

    Surface(modifier = Modifier.fillMaxSize(), color = AccountCream) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, enabled = !state.isSubmitting) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AccountDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp))
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

            Text(
                text = "Welcome to CaterHub!",
                modifier = Modifier.fillMaxWidth(),
                color = AccountMaroon,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your account to continue.",
                modifier = Modifier.fillMaxWidth(),
                color = AccountMuted,
                fontSize = 17.sp,
                lineHeight = 25.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                enabled = !state.isSubmitting,
                isError = localError != null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                textStyle = LocalTextStyle.current.copy(color = AccountDark, fontSize = 17.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AccountDark,
                    unfocusedTextColor = AccountDark,
                    focusedBorderColor = AccountMaroon,
                    unfocusedBorderColor = AccountBorder,
                    focusedLabelColor = AccountMaroon,
                    unfocusedLabelColor = AccountMuted,
                    focusedLeadingIconColor = AccountMaroon,
                    unfocusedLeadingIconColor = AccountMuted,
                    cursorColor = AccountMaroon
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val displayMobile = formatVerifiedMobile(state.verifiedMobileNumber)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccountGreenTint)
                    .border(1.dp, AccountBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .semantics { contentDescription = "Verified mobile number $displayMobile, read only" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = AccountMuted, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mobile number", color = AccountMuted, fontSize = 12.sp)
                    Text(displayMobile, color = AccountDark, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Filled.Verified, contentDescription = null, tint = AccountGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Verified", color = AccountGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccountErrorTint)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = AccountMaroon, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage,
                        color = AccountMaroonDark,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                enabled = !state.isSubmitting && fullName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccountMaroon,
                    contentColor = Color.White,
                    disabledContainerColor = AccountDisabledButton,
                    disabledContentColor = AccountDisabledText
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creating your account...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
