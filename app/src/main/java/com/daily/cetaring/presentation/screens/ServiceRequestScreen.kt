@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)

@Composable
fun ServiceRequestScreen(
    serviceType: String,
    onBackClick: () -> Unit
) {
    val staffMode = serviceType == "staff"
    val selected = remember { mutableStateMapOf<String, Int>() }

    val items = if (staffMode) {
        listOf(
            "Chef" to Icons.Filled.Restaurant,
            "Catering Boys" to Icons.Filled.Groups,
            "Catering Girls" to Icons.Filled.Groups,
            "Kitchen Helpers" to Icons.Filled.Restaurant,
            "Cleaning Staff" to Icons.Filled.CleaningServices
        )
    } else {
        listOf(
            "Chairs" to Icons.Filled.Groups,
            "Tables" to Icons.Filled.Restaurant,
            "Stage & Decoration" to Icons.Filled.Cake,
            "Lighting" to Icons.Filled.Star
        )
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (staffMode) "Book Catering Staff"
                        else "Decorations & Equipment",
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Maroon
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (staffMode)
                    "Choose the people you need for your event."
                else
                    "Choose the equipment and decoration services you need.",
                color = Muted,
                style = MaterialTheme.typography.bodyLarge
            )

            items.forEach { (title, icon) ->
                val count = selected[title] ?: 0

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (staffMode) Maroon.copy(alpha = .10f)
                                else Green.copy(alpha = .10f)
                            )
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (staffMode) Maroon else Green,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp)
                            )
                        }

                        Spacer(Modifier.size(13.dp))

                        Text(
                            title,
                            modifier = Modifier.weight(1f),
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                selected[title] = (count - 1).coerceAtLeast(0)
                            },
                            enabled = count > 0
                        ) {
                            Text("−")
                        }

                        Text(
                            count.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                selected[title] = count + 1
                            }
                        ) {
                            Icon(Icons.Filled.Add, "Add")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (staffMode) Maroon else Green
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Request service",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Date, location and pricing will be collected in the next service-booking step.",
                        color = Color.White.copy(alpha = .9f)
                    )
                }
            }

            Text(
                "This screen is the new service entry point. The existing catering booking flow does not create staff jobs.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
