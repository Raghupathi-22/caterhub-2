package com.daily.cetaring.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R

/**
 * Promotional content for a service that is announced but not yet bookable. It deliberately has
 * no click action, listings, prices, availability or ratings. When a real catalogue and booking
 * API exist, the service should move into the regular catalogue instead of extending this model.
 */
@Immutable
data class ComingSoonServicePromotion(
    val id: String,
    val eyebrow: String,
    val title: String,
    val description: String,
    val occasions: List<String>,
    val launchMessage: String,
    @DrawableRes val imageRes: Int,
    val imageDescription: String
)

object ComingSoonServices {
    /** Planned venue service; future scope: halls, banquets, convention centres, party and corporate venues. */
    val FunctionAndBanquetHalls = ComingSoonServicePromotion(
        id = "function-banquet-halls",
        eyebrow = "Exclusive venues",
        title = "Function & Banquet Halls",
        description = "Your celebrations deserve a beautiful venue. Discover elegant function halls and " +
            "banquet venues for weddings, receptions, birthday parties and special occasions.",
        occasions = listOf("Weddings", "Receptions", "Birthdays", "Special occasions"),
        launchMessage = "Venue booking launching soon on CaterHub",
        imageRes = R.drawable.svc_venue_banquet_hall,
        imageDescription = "Elegant Indian banquet hall with crystal chandeliers and ivory seating"
    )
}

private object ComingSoonPalette {
    val Ivory = Color(0xFFFBF8F1)
    val IvoryDeep = Color(0xFFF4ECDC)
    val Champagne = Color(0xFFD9B979)
    val ChampagneDeep = Color(0xFF8C6A2F)
    val Emerald = Color(0xFF173F32)
    val EmeraldSoft = Color(0xFFE8EFEA)
    val Muted = Color(0xFF686C66)
    val Ink = Color(0xFF242824)
}

private const val VENUE_IMAGE_ASPECT_RATIO = 3f / 2f
private val MAX_VENUE_IMAGE_HEIGHT = 360.dp

/**
 * Image-led "Coming Soon" feature for an upcoming service. Non-interactive by design so customers
 * are never sent to an empty booking flow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComingSoonServiceSection(
    promotion: ComingSoonServicePromotion,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "${promotion.title}, coming soon. ${promotion.description} " +
                    promotion.launchMessage
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                promotion.eyebrow.uppercase(),
                color = ComingSoonPalette.ChampagneDeep,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Text(
                "Coming to CaterHub",
                color = ComingSoonPalette.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ComingSoonPalette.Ivory)
                .border(1.dp, ComingSoonPalette.Champagne.copy(alpha = 0.55f), shape)
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                // Native 3:2 on phones; capped on tablets with a centre crop instead of distortion.
                val imageHeight = (maxWidth / VENUE_IMAGE_ASPECT_RATIO).coerceAtMost(MAX_VENUE_IMAGE_HEIGHT)
                Image(
                    painter = painterResource(promotion.imageRes),
                    contentDescription = promotion.imageDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                )
                ComingSoonBadge(Modifier.align(Alignment.TopStart).padding(14.dp))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(2.dp)
                        .background(ComingSoonPalette.Champagne)
                )
                Text(
                    promotion.title,
                    color = ComingSoonPalette.Emerald,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    promotion.description,
                    color = ComingSoonPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    promotion.occasions.forEach { occasion ->
                        Text(
                            occasion,
                            color = ComingSoonPalette.ChampagneDeep,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ComingSoonPalette.IvoryDeep)
                                .border(1.dp, ComingSoonPalette.Champagne.copy(alpha = 0.45f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ComingSoonPalette.EmeraldSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = ComingSoonPalette.Emerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        promotion.launchMessage,
                        color = ComingSoonPalette.Emerald,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonBadge(modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(ComingSoonPalette.Champagne)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ComingSoonPalette.Emerald)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "COMING SOON",
            color = ComingSoonPalette.Emerald,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
    }
}
