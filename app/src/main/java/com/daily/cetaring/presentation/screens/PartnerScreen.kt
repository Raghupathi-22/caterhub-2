package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.ServiceImage
import com.daily.cetaring.presentation.components.ServiceImagery

private val PartnerCream = Color(0xFFFFFCF6)
private val PartnerMaroon = Color(0xFF8E171C)
private val PartnerGreen = Color(0xFF0D5B22)
private val PartnerGreenDark = Color(0xFF0A421A)
private val PartnerGold = Color(0xFFC88912)
private val PartnerGoldSoft = Color(0xFFFFF4D9)
private val PartnerPaleGreen = Color(0xFFEAF4E7)
private val PartnerBorder = Color(0xFFE3D9C7)
private val PartnerText = Color(0xFF2B2622)
private val PartnerMuted = Color(0xFF6C655D)

/**
 * Public "Partner" tab. Explains how professionals join CaterHub and hands off to the existing
 * worker registration ([onBecomePartner]) and worker login ([onPartnerLogin]) flows.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerScreenContent(
    padding: PaddingValues,
    onBecomePartner: () -> Unit,
    onPartnerLogin: () -> Unit
) {
    val categories = remember {
        ServiceCatalog.workerRegistrationCategories.map { category ->
            category to ServiceCatalog.workerRegistrationRolesForCategory(category.id).map { it.title }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PartnerCream)
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PartnerHero(onBecomePartner = onBecomePartner)

        SectionHeading("Why join CaterHub?")
        PartnerBenefit(
            Icons.Filled.Work,
            "Jobs that match your skills",
            "You see event jobs for the roles you choose when you register."
        )
        PartnerBenefit(
            Icons.Filled.EventAvailable,
            "Know the details before you accept",
            "Each job shows the event date, time, location and payment so you decide what to take."
        )
        PartnerBenefit(
            Icons.Filled.LocationOn,
            "Work in your preferred areas",
            "Tell us the Hyderabad areas you can travel to."
        )
        PartnerBenefit(
            Icons.Filled.Verified,
            "A verified professional profile",
            "Your experience, skills and languages are shown on a profile reviewed by CaterHub."
        )

        SectionHeading("How it works")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PartnerPaleGreen),
            border = BorderStroke(1.dp, Color(0xFFC9DDC6))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PartnerStep("1", "Register with your mobile number", "Verify it with a one-time password (OTP).")
                PartnerStep("2", "Create your partner profile", "Choose your category, role, experience and preferred areas.")
                PartnerStep("3", "Accept jobs", "Browse matching jobs and accept the ones that suit you.")
            }
        }

        SectionHeading("Professionals we work with")
        categories.forEach { (category, roles) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PartnerBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ServiceImage(
                        visual = remember(category.id) { ServiceImagery.categoryVisual(category.id) },
                        contentDescription = null,
                        shape = RoundedCornerShape(14.dp),
                        iconSize = 22.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(category.title, color = PartnerText, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            roles.forEach { role ->
                                Surface(shape = RoundedCornerShape(10.dp), color = PartnerGoldSoft) {
                                    Text(
                                        role,
                                        color = Color(0xFF7A5408),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBecomePartner,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PartnerGreen, contentColor = Color.White)
        ) {
            Text("Become a Partner", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
        }
        OutlinedButton(
            onClick = onPartnerLogin,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PartnerGreen)
        ) {
            Icon(Icons.Filled.PhoneAndroid, null, tint = PartnerGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Already a partner? Log in", color = PartnerGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(4.dp))
    }
}

@Composable
private fun PartnerHero(onBecomePartner: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(PartnerGreenDark, PartnerGreen, Color(0xFF1F7A3A))))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = PartnerGold.copy(alpha = 0.22f)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Work, null, tint = Color(0xFFFFE7A3), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CATERHUB PARTNERS", color = Color(0xFFFFE7A3), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
                Text(
                    "Become a CaterHub Partner",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 31.sp,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    "Work with CaterHub and earn with your skills. Join as a catering professional, decorator, " +
                        "entertainer, beauty professional, photographer or Purohit.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
                Button(
                    onClick = onBecomePartner,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PartnerGreenDark)
                ) {
                    Text("Become a Partner", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        color = PartnerMaroon,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun PartnerBenefit(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PartnerBorder)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(PartnerGoldSoft, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PartnerGold, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = PartnerText, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(description, color = PartnerMuted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun PartnerStep(number: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(32.dp).background(PartnerGreen, CircleShape), contentAlignment = Alignment.Center) {
            Text(number, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = PartnerText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(description, color = PartnerMuted, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
