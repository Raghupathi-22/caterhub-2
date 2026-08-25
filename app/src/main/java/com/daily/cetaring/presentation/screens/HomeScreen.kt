package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.viewmodel.HomeViewModel

private val Cream = Color(0xFFFFFCF5)
private val Red = Color(0xFF971B1E)
private val Green = Color(0xFF0A672A)
private val Gold = Color(0xFFC58A16)
private val Ink = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookCateringClick: () -> Unit,
    onServiceCategoryClick: (String) -> Unit,
    onWorkerRegisterClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGuestSizeClick: (Int) -> Unit,
    onEventTypeClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = Cream,
        bottomBar = { HomeBottomBar(onBookingsClick = onBookingsClick, onProfileClick = onProfileClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HomeHeader(onNotificationsClick = onNotificationsClick, onProfileClick = onProfileClick)
            HomeHero(onBookCateringClick = onBookCateringClick)
            HomeCategories(onCategoryClick = onServiceCategoryClick)
            HomeOffers()
            HomeSpecialities()
            HomeWhyCaterHub()
            HomeWorkWithUs(onWorkerRegisterClick = onWorkerRegisterClick)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("CaterHub", color = Red, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Hyderabad", color = Ink, fontWeight = FontWeight.SemiBold)
            }
        }
        IconButton(onClick = onNotificationsClick) {
            Icon(Icons.Filled.NotificationsNone, "Notifications", tint = Ink)
        }
        Surface(modifier = Modifier.size(40.dp).clickable(onClick = onProfileClick), shape = CircleShape, color = Red) {
            Icon(Icons.Filled.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.padding(6.dp))
        }
    }
}

@Composable
private fun HomeHero(onBookCateringClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Everything you need for your event", color = Red, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(
                "Book catering, decoration, entertainment and more — all in one place.",
                color = Ink,
                style = MaterialTheme.typography.bodyLarge
            )
            ActionButton("START BOOKING", Red, onBookCateringClick)
        }
    }
}

@Composable
private fun HomeCategories(onCategoryClick: (String) -> Unit) {
    SectionTitle("Service Categories")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ServiceCatalog.categories.filter { it.id != "other-event-services" }.forEachIndexed { index, category ->
            val accent = if (index % 2 == 0) Red else Green
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onCategoryClick(category.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).background(accent.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(categoryEmoji(category.id), color = accent, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category.title, color = Ink, fontWeight = FontWeight.ExtraBold)
                        Text("Choose the services you need", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = accent)
                }
            }
        }
    }
}

@Composable
private fun HomeOffers() {
    SectionTitle("Today's Offers")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OfferCard("Wedding catering packages", "Starting from ₹499 per person", Gold)
        OfferCard("Decoration packages", "Stage + flower + lighting combos", Green)
        OfferCard("Photography packages", "Photo + video + highlights bundles", Red)
        OfferCard("Complete event packages", "Catering + decor + entertainment", Gold)
    }
}

@Composable
private fun OfferCard(title: String, subtitle: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Ink)
        }
    }
}

@Composable
private fun HomeSpecialities() {
    SectionTitle("Our Specialities")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("• Authentic biryani and live counters", color = Ink)
            Text("• Professional decorators and setup teams", color = Ink)
            Text("• Verified entertainment and event hosts", color = Ink)
            Text("• Complete event coordination support", color = Ink)
        }
    }
}

@Composable
private fun HomeWhyCaterHub() {
    SectionTitle("Why CaterHub")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WhyItem("Verified professionals", Icons.Filled.Celebration, Red)
        WhyItem("Trusted services", Icons.Filled.Celebration, Green)
        WhyItem("Easy booking", Icons.Filled.Celebration, Gold)
        WhyItem("Transparent pricing", Icons.Filled.Celebration, Red)
        WhyItem("Complete event support", Icons.Filled.Celebration, Green)
    }
}

@Composable
private fun WhyItem(text: String, icon: ImageVector, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = Ink, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeWorkWithUs(onWorkerRegisterClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Join CaterHub", color = Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Work with us and earn with your skills", color = Green, fontWeight = FontWeight.SemiBold)
            Text(
                "Join as a catering professional, decorator, DJ, singer, photographer, beauty professional or other event service provider.",
                color = Ink
            )
            ActionButton("Join CaterHub \u2192", Green, onWorkerRegisterClick, showTrailingArrow = false)
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    showTrailingArrow: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            if (showTrailingArrow) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
}

private fun categoryEmoji(categoryId: String): String = when (categoryId) {
    "catering-food" -> "🍽"
    "decoration" -> "🎀"
    "entertainment" -> "🎵"
    "beauty" -> "💄"
    "photography-video" -> "📷"
    "religious-ceremony" -> "🙏"
    "event-support" -> "🎤"
    "rentals" -> "🪑"
    "transport-logistics" -> "🚗"
    else -> "⭐"
}

@Composable
private fun HomeBottomBar(
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = onBookingsClick, icon = { Icon(Icons.Filled.Celebration, "Bookings") }, label = { Text("Bookings") })
        NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Filled.AccountCircle, "Profile") }, label = { Text("Profile") })
    }
}
