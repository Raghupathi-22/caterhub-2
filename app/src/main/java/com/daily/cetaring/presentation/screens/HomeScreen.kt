package com.daily.cetaring.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF7F1017), Color(0xFFA61F25), Color(0xFF6C1116))),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Gold.copy(alpha = 0.18f)
                    ) {
                        Text("CATERHUB EVENTS", color = Color(0xFFFFE7A3), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text("20–200+ guests", color = Color.White.copy(alpha = 0.86f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "Make your event
memorable.",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Catering, food, professional staff and event services — planned in one simple booking.",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onBookCateringClick),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("BOOK CATERING", color = Red, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(9.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onMenuClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFFFFBF0), Color(0xFFFFF1D0))), RoundedCornerShape(26.dp))
                .padding(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(68.dp).background(Gold.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.RestaurantMenu, "Menu", tint = Gold, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("OUR MENU", color = Gold, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    }
                    Text("Explore the menu", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Breakfast • Lunch • Dinner • Snacks • Beverages", color = Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(3.dp))
                    Surface(
                        modifier = Modifier.clickable(onClick = onMenuClick),
                        shape = RoundedCornerShape(14.dp),
                        color = Red
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("VIEW MENU", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(17.dp))
                        }
                    }
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
