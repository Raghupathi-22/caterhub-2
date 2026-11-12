package com.daily.cetaring.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.config.SupportContact

private object ContactPalette {
    val Ivory = Color(0xFFFBF8F1)
    val Champagne = Color(0xFFD9B979)
    val ChampagneDeep = Color(0xFF8C6A2F)
    val Emerald = Color(0xFF173F32)
    val EmeraldSoft = Color(0xFFE8EFEA)
    val Muted = Color(0xFF686C66)
    val Ink = Color(0xFF242824)
}

/** Home screen "We're here to help" block with real Call, WhatsApp and Email support actions. */
@Composable
fun CaterHubContactSupportSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ContactPalette.Ivory)
            .border(1.dp, ContactPalette.Champagne.copy(alpha = 0.55f), shape)
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp)
    ) {
        Text(
            "CONTACT & SUPPORT",
            color = ContactPalette.ChampagneDeep,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "We're here to help",
            color = ContactPalette.Emerald,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Have questions about bookings, catering or services? Contact CaterHub.",
            color = ContactPalette.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .width(36.dp)
                .height(2.dp)
                .background(ContactPalette.Champagne)
        )
        Spacer(Modifier.height(6.dp))

        ContactActionRow(
            icon = Icons.Filled.Call,
            label = "Call us",
            value = SupportContact.SUPPORT_PHONE_NATIONAL,
            onClick = {
                context.openSupportIntent(
                    Intent(Intent.ACTION_DIAL, Uri.parse(SupportContact.dialUri)),
                    "Unable to open the phone dialer. Please call ${SupportContact.SUPPORT_PHONE_NATIONAL}."
                )
            }
        )
        HorizontalDivider(color = ContactPalette.Champagne.copy(alpha = 0.3f))
        ContactActionRow(
            icon = Icons.AutoMirrored.Filled.Chat,
            label = "WhatsApp",
            value = SupportContact.SUPPORT_PHONE_NATIONAL,
            onClick = {
                context.openSupportIntent(
                    Intent(Intent.ACTION_VIEW, Uri.parse(SupportContact.whatsAppUrl)),
                    "WhatsApp is not available. Please call ${SupportContact.SUPPORT_PHONE_NATIONAL}."
                )
            }
        )
        HorizontalDivider(color = ContactPalette.Champagne.copy(alpha = 0.3f))
        ContactActionRow(
            icon = Icons.Filled.MailOutline,
            label = "Email",
            value = SupportContact.SUPPORT_EMAIL,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(SupportContact.emailUri)).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportContact.SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, SupportContact.EMAIL_SUBJECT)
                }
                context.openSupportIntent(
                    intent,
                    "No email app found. Please email ${SupportContact.SUPPORT_EMAIL}."
                )
            }
        )
    }
}

@Composable
private fun ContactActionRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClickLabel = "Contact CaterHub by $label", role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(ContactPalette.EmeraldSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ContactPalette.Emerald, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                color = ContactPalette.Ink,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                value,
                color = ContactPalette.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ContactPalette.ChampagneDeep
        )
    }
}

private fun Context.openSupportIntent(intent: Intent, fallbackMessage: String) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, fallbackMessage, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, fallbackMessage, Toast.LENGTH_LONG).show()
    }
}
