package com.daily.cetaring.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daily.cetaring.config.SupportContact
import com.daily.cetaring.presentation.components.CaterHubMarketplaceHeader
import com.daily.cetaring.presentation.components.CaterHubSupportCard
import com.daily.cetaring.presentation.components.ComingSoonServiceSection
import com.daily.cetaring.presentation.components.ComingSoonServices
import com.daily.cetaring.presentation.components.MarketplaceQuickActions
import com.daily.cetaring.presentation.components.MarketplaceSectionHeading
import com.daily.cetaring.presentation.components.ServiceCategoryGrid
import com.daily.cetaring.presentation.components.ServiceDiscoverySearchField
import com.daily.cetaring.presentation.components.ServiceDiscoverySearchResults
import com.daily.cetaring.presentation.components.VillageFarmRiceHomeCard
import com.daily.cetaring.presentation.viewmodel.HomeViewModel
import com.daily.cetaring.ui.theme.CaterHubColors

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
    onLogout: () -> Unit,
    onVillageFarmRiceClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
        if (launchIntent(appIntent)) return

        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/${SupportContact.SUPPORT_PHONE_WHATSAPP}")
        )
        if (!launchIntent(webIntent)) {
            Toast.makeText(context, "WhatsApp is not available on this device.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = CaterHubColors.Background,
        bottomBar = { HomeBottomBar(onBookingsClick = onBookingsClick, onProfileClick = onProfileClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CaterHubColors.Background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CaterHubMarketplaceHeader {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        Icons.Filled.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = CaterHubColors.Ink
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.clip(CircleShape).background(CaterHubColors.Green)
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = Color.White)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MarketplaceSectionHeading("What are you planning?")
                Text(
                    "Explore services for your next event.",
                    color = CaterHubColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ServiceDiscoverySearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            if (searchQuery.isBlank()) {
                MarketplaceQuickActions(
                    onBookCatering = onBookCateringClick,
                    onExploreMenu = onMenuClick
                )
                VillageFarmRiceHomeCard(onClick = onVillageFarmRiceClick)
                MarketplaceSectionHeading(
                    title = "Explore services",
                    subtitle = "Choose a category to see available services."
                )
                ServiceCategoryGrid(onCategoryClick = onServiceCategoryClick)
            } else {
                MarketplaceSectionHeading("Search results")
                ServiceDiscoverySearchResults(
                    query = searchQuery,
                    onCategoryClick = onServiceCategoryClick
                )
            }

            CaterHubSupportCard(
                onCallClick = ::openDialer,
                onWhatsAppClick = ::openWhatsApp
            )
            if (searchQuery.isBlank()) {
                ComingSoonServiceSection(ComingSoonServices.FunctionAndBanquetHalls)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeBottomBar(
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = CaterHubColors.Surface,
        tonalElevation = 6.dp
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = CaterHubColors.Green,
            selectedTextColor = CaterHubColors.Green,
            indicatorColor = CaterHubColors.GreenSoft,
            unselectedIconColor = CaterHubColors.Muted,
            unselectedTextColor = CaterHubColors.Muted
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = colors
        )
        NavigationBarItem(
            selected = false,
            onClick = onBookingsClick,
            icon = { Icon(Icons.Filled.Event, contentDescription = "Bookings") },
            label = { Text("Bookings") },
            colors = colors
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = colors
        )
    }
}
