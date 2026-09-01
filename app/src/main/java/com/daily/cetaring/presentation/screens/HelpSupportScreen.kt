package com.daily.cetaring.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daily.cetaring.config.SupportContact
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSecondaryButton

private const val SUPPORT_EMAIL = "caterhub.support@gmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    fun launchIntent(intent: Intent, fallbackMessage: String) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, fallbackMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Need Help?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Contact CaterHub Support for bookings, worker onboarding, jobs and payments.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Contact CaterHub Support", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                "Email: $SUPPORT_EMAIL\nWhatsApp / Call: ${SupportContact.SUPPORT_PHONE_NATIONAL}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CaterHubPrimaryButton(
                text = "Email Support",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$SUPPORT_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "CaterHub Support Request")
                    }
                    launchIntent(intent, "No email app found. Please email $SUPPORT_EMAIL")
                },
                modifier = Modifier.fillMaxWidth()
            )

            CaterHubSecondaryButton(
                text = "WhatsApp Support",
                onClick = {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=${SupportContact.SUPPORT_PHONE_WHATSAPP}"))
                    try {
                        context.startActivity(appIntent)
                    } catch (_: ActivityNotFoundException) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${SupportContact.SUPPORT_PHONE_WHATSAPP}"))
                        launchIntent(webIntent, "WhatsApp is not installed. Please contact us by phone.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            CaterHubSecondaryButton(
                text = "Call Support",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${SupportContact.SUPPORT_PHONE_NATIONAL}"))
                    launchIntent(intent, "Unable to open dialer. Please call ${SupportContact.SUPPORT_PHONE_NATIONAL}")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "We usually respond fastest on WhatsApp during business hours.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
