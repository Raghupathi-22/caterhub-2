package com.daily.cetaring.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Celebration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.config.SupportContact
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.components.CaterHubSupportCard
import com.daily.cetaring.presentation.components.categoryUiMeta
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
    onMenuClick: () -> Unit,
    onServiceCategoryClick: (String) -> Unit,
    onBookingsClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGuestSizeClick: (Int) -> Unit,
    onEventTypeClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    fun launchIntent(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    fun openDialer() {
        val opened = launchIntent(
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${SupportContact.SUPPORT_PHONE_NATIONAL}")
            )
        )
        if (!opened) {
            Toast.makeText(context, "Unable to open phone dialer.", Toast.LENGTH_LONG).show()
        }
    }

    fun openWhatsApp() {
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("whatsapp://send?phone=${SupportContact.SUPPORT_PHONE_WHATSAPP}")
        )
        val appOpened = launchIntent(appIntent)
        if (appOpened) return

        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/${SupportContact.SUPPORT_PHONE_WHATSAPP}")
        )
        val webOpened = launchIntent(webIntent)
        if (!webOpened) {
            Toast.makeText(context, "WhatsApp is not available on this device.", Toast.LENGTH_LONG).show()
        }
    }

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
            MenuDiscoveryCard(onMenuClick = onMenuClick)
            HomeCategories(onCategoryClick = onServiceCategoryClick)
            CaterHubSupportCard(
                onCallClick = ::openDialer,
                onWhatsAppClick = ::openWhatsApp
            )
            Spacer(Modifier.height(8.dp))
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
        ServiceCatalog.customerCategories.forEach { category ->
            val visual = categoryUiMeta(category)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 92.dp)
                    .clickable { onCategoryClick(category.id) },
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
                        modifier = Modifier.size(58.dp).background(visual.accent.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(visual.icon, contentDescription = null, tint = visual.accent, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category.title, color = Ink, fontWeight = FontWeight.ExtraBold)
                        Text(category.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = visual.accent)
                }
            }
        }
    }
}

@Composable
private fun MenuDiscoveryCard(onMenuClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clickable(onClick = onMenuClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Gold.copy(alpha = 0.16f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RestaurantMenu,
                    contentDescription = "Menu",
                    tint = Gold,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Menu", color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Explore our delicious catering options",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Red,
                modifier = Modifier.clickable(onClick = onMenuClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Menu", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
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
