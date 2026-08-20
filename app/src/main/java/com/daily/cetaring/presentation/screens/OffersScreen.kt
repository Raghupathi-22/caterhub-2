package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R

private val Cream = Color(0xFFFFFCF6)
private val Maroon = Color(0xFF8E171C)
private val Gold = Color(0xFFC88912)
private val Green = Color(0xFF0D5B22)
private val TextDark = Color(0xFF2B2622)
private val Muted = Color(0xFF6C655D)
private val Border = Color(0xFFE3D9C7)

@Composable
fun OffersScreenContent(padding: PaddingValues, onBook: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Today's Offers", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
        Text("Special catering packages and event offers from CaterHub.", color = Muted, fontSize = 15.sp, lineHeight = 21.sp)
        OfferHero(onBook)
        FullOfferCard("Wedding Special", "Catering packages from ₹499 / person", R.drawable.public_offer_wedding, Maroon, listOf("Menu customization", "Experienced catering team", "Serving staff available"), onBook)
        FullOfferCard("Family Function", "Special pricing for 100+ guests", R.drawable.public_offer_family, Green, listOf("Flexible guest count", "Food + service packages", "Easy event planning"), onBook)
        OfferFeature("Bulk Guest Offer", "Book for 200+ guests and ask us for a customized package.", Icons.Filled.Groups, Gold)
        OfferFeature("Catering + Staff", "Combine food, chefs and serving staff in one event package.", Icons.Filled.Restaurant, Green)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OfferHero(onBook: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0C7)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C977))) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalOffer, null, tint = Gold, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(10.dp))
                Text("CaterHub Special Offers", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Wedding • Family Function • Festival • Bulk Events", color = TextDark, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBook, colors = ButtonDefaults.buttonColors(containerColor = Maroon), shape = RoundedCornerShape(10.dp)) {
                Text("Book with an Offer →", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FullOfferCard(title: String, subtitle: String, image: Int, color: Color, benefits: List<String>, onBook: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(Modifier.fillMaxWidth().height(190.dp)) {
            Column(Modifier.weight(1f).padding(15.dp), verticalArrangement = Arrangement.Center) {
                Text(title, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text(subtitle, color = TextDark, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
                Spacer(Modifier.height(8.dp))
                benefits.forEach { Text("✓  $it", color = TextDark, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp)) }
                Button(onClick = onBook, colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(9.dp), modifier = Modifier.padding(top = 7.dp)) {
                    Text("View Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Image(painterResource(image), null, Modifier.width(130.dp).fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun OfferFeature(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(body, color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Icon(Icons.Filled.ArrowForward, null, tint = color)
        }
    }
}
