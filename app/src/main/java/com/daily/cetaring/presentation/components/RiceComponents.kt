package com.daily.cetaring.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.daily.cetaring.R
import com.daily.cetaring.data.remote.dto.RiceProductResponse
import com.daily.cetaring.domain.rice.RiceOrderRules
import com.daily.cetaring.presentation.viewmodel.RiceProductState

/** Natural green and warm ivory palette for Village Farm Rice, aligned with the Quality screen. */
object RicePalette {
    val Green = Color(0xFF0D5B22)
    val GreenDeep = Color(0xFF0A3F18)
    val PaleGreen = Color(0xFFEAF4E7)
    val Ivory = Color(0xFFFFFCF6)
    val IvoryDeep = Color(0xFFF6EEDC)
    val Gold = Color(0xFFC88912)
    val Ink = Color(0xFF2B2622)
    val Muted = Color(0xFF6C655D)
    val Border = Color(0xFFC9DDC6)
}

@Composable
fun RiceHeroImage(imageUrl: String?, modifier: Modifier = Modifier, height: Dp = 220.dp) {
    val fallback = painterResource(R.drawable.rice_village_paddy)
    AsyncImage(
        model = imageUrl?.takeIf { it.isNotBlank() },
        contentDescription = "Golden rice paddy fields in a village farm",
        placeholder = fallback,
        error = fallback,
        fallback = fallback,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxWidth().height(height)
    )
}

/** Premium, image-led Village Farm Rice section. All prices and limits come from the backend. */
@Composable
fun VillageFarmRiceSection(
    state: RiceProductState,
    onRetry: () -> Unit,
    onOrderRice: () -> Unit,
    onRequestSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RicePalette.PaleGreen),
        border = BorderStroke(1.dp, RicePalette.Border)
    ) {
        val product = (state as? RiceProductState.Loaded)?.product
        Box {
            RiceHeroImage(imageUrl = product?.imageUrl, height = 240.dp)
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC0A3F18))))
            )
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                Surface(shape = CircleShape, color = RicePalette.Gold) {
                    Text(
                        "FROM OUR VILLAGE FARM",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    product?.name ?: "Village Farm Rice",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 8.dp).semantics { heading() }
                )
                product?.tagline?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = Color(0xFFF6EEDC), fontSize = 14.sp, lineHeight = 19.sp)
                }
            }
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (state) {
                RiceProductState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = RicePalette.Green, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Loading rice prices…", color = RicePalette.Muted, fontSize = 14.sp)
                }
                is RiceProductState.Error -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("We couldn't load rice prices.", color = RicePalette.Ink, fontWeight = FontWeight.Bold)
                    Text(state.message, color = RicePalette.Muted, fontSize = 13.sp)
                    OutlinedButton(onClick = onRetry, border = BorderStroke(1.dp, RicePalette.Green)) {
                        Text("Retry", color = RicePalette.Green, fontWeight = FontWeight.SemiBold)
                    }
                }
                is RiceProductState.Loaded -> RiceProductDetails(state.product, onOrderRice, onRequestSample)
            }
        }
    }
}

@Composable
private fun RiceProductDetails(product: RiceProductResponse, onOrderRice: () -> Unit, onRequestSample: () -> Unit) {
    product.description?.takeIf { it.isNotBlank() }?.let {
        Text(it, color = RicePalette.Muted, fontSize = 14.sp, lineHeight = 20.sp)
    }
    val highlights = product.highlights.orEmpty().filter { it.isNotBlank() }
    if (highlights.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            highlights.forEach { highlight ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Eco, null, tint = RicePalette.Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(highlight, color = RicePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (RiceOrderRules.isOrderable(product)) {
        Surface(shape = RoundedCornerShape(18.dp), color = RicePalette.Ivory, border = BorderStroke(1.dp, RicePalette.IvoryDeep)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        RiceOrderRules.formatInr(product.basePackPrice),
                        color = RicePalette.GreenDeep,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("for ${product.basePackKg} kg", color = RicePalette.Muted, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Minimum order", color = RicePalette.Muted, fontSize = 12.sp)
                    Text("${product.minOrderKg} kg", color = RicePalette.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            onClick = onOrderRice,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RicePalette.Green, contentColor = Color.White)
        ) {
            Text("Order Rice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    } else {
        Text("Rice ordering is paused right now. Please check back soon.", color = RicePalette.Muted, fontSize = 14.sp)
    }

    if (product.sampleEnabled == true && product.active == true) {
        RiceSampleOfferCard(product = product, onRequestSample = onRequestSample)
    }
}

@Composable
fun RiceSampleOfferCard(product: RiceProductResponse, onRequestSample: (() -> Unit)?) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, RicePalette.Border)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(RicePalette.PaleGreen),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Storefront, null, tint = RicePalette.Green, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Free ${RiceOrderRules.formatSampleWeight(product.sampleWeightGrams)} sample",
                        color = RicePalette.Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text("Pickup only · No home delivery", color = RicePalette.Muted, fontSize = 12.sp)
                }
                Surface(shape = CircleShape, color = RicePalette.PaleGreen) {
                    Text(
                        "FREE · ₹0",
                        color = RicePalette.Green,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.CheckCircle, null, tint = RicePalette.Green, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Taste before you order. Our team confirms the pickup location and collection time after you request.",
                    color = RicePalette.Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            if (onRequestSample != null) {
                OutlinedButton(
                    onClick = onRequestSample,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RicePalette.Green)
                ) {
                    Text("Request free sample", color = RicePalette.Green, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Compact Home entry card for signed-in customers, who do not see the public Quality tab. */
@Composable
fun VillageFarmRiceHomeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RicePalette.PaleGreen),
        border = BorderStroke(1.dp, RicePalette.Border)
    ) {
        Box {
            RiceHeroImage(imageUrl = null, height = 132.dp)
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.horizontalGradient(listOf(Color(0xE60A3F18), Color(0x330A3F18))))
            )
            Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 18.dp)) {
                Text("Village Farm Rice", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Order bulk rice or collect a free sample", color = Color(0xFFF6EEDC), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Surface(shape = CircleShape, color = RicePalette.Gold) {
                    Text(
                        "Order Rice",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
