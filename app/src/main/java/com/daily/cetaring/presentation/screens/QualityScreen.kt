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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R

private val Cream = Color(0xFFFFFCF6)
private val Maroon = Color(0xFF8E171C)
private val Gold = Color(0xFFC88912)
private val Green = Color(0xFF0D5B22)
private val PaleGreen = Color(0xFFEAF4E7)
private val TextDark = Color(0xFF2B2622)
private val Muted = Color(0xFF6C655D)
private val Border = Color(0xFFE3D9C7)

@Composable
fun QualityScreenContent(padding: PaddingValues, onBook: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Text("Our Quality", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
        Text("From our farm to your plate", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
        Text("We care about what goes into every meal served by CaterHub.", color = Muted, fontSize = 15.sp, lineHeight = 21.sp)

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PaleGreen), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC9DDC6))) {
            Column {
                Image(painterResource(R.drawable.public_hero_food), null, Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Crop)
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌾", fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("20-acre farm", color = Green, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("Natural farming approach", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 3.dp))
                    Text("We grow and source rice with a focus on natural farming practices, then bring that care into our catering kitchen.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        QualityStep("1", "Farm", "Carefully grown rice and ingredients from our farming journey.", Icons.Filled.Agriculture, Green)
        QualityStep("2", "Clean & Prepare", "Ingredients are cleaned and prepared with food safety in mind.", Icons.Filled.HealthAndSafety, Maroon)
        QualityStep("3", "Cook", "Our chefs prepare dishes with consistent taste, freshness and presentation.", Icons.Filled.Restaurant, Gold)
        QualityStep("4", "Serve", "Trained staff serve your guests professionally and on time.", Icons.Filled.Verified, Green)

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
            Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CheckCircle, null, tint = Green, modifier = Modifier.size(38.dp))
                Text("Quality is part of every event", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 7.dp))
                Text("From ingredients to final service, CaterHub is built around quality, hygiene and customer satisfaction.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QualityStep(number: String, title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.size(54.dp)) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(25.dp))
            }
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text("$number. $title", color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text(body, color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
