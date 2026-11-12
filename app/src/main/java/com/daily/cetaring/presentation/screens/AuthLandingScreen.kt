package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.presentation.components.CaterHubContactSupportSection
import com.daily.cetaring.presentation.components.CaterHubMarketplaceHeader
import com.daily.cetaring.presentation.components.ComingSoonServiceSection
import com.daily.cetaring.presentation.components.ComingSoonServices
import com.daily.cetaring.presentation.components.MarketplaceQuickActions
import com.daily.cetaring.presentation.components.MarketplaceSectionHeading
import com.daily.cetaring.presentation.components.ServiceCategoryGrid
import com.daily.cetaring.presentation.components.ServiceDiscoverySearchField
import com.daily.cetaring.presentation.components.ServiceDiscoverySearchResults
import com.daily.cetaring.ui.theme.CaterHubColors
import com.daily.cetaring.presentation.viewmodel.RiceProductState

@Composable
fun AuthLandingScreen(
    onCreateAccountClick: () -> Unit,
    onCustomerLoginClick: () -> Unit,
    onWorkerRegisterClick: () -> Unit,
    onWorkerLoginClick: () -> Unit,
    onMenuClick: () -> Unit,
    onServiceCategoryClick: (String) -> Unit = {},
    riceState: RiceProductState? = null,
    onLoadRice: () -> Unit = {},
    onOrderRice: () -> Unit = {},
    onRequestRiceSample: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = selectedTab != 0) { selectedTab = 0 }

    Scaffold(
        containerColor = CaterHubColors.Background,
        bottomBar = { PublicBottomBar(selectedTab) { selectedTab = it } }
    ) { padding ->
        when (selectedTab) {
            0 -> PublicHomeContent(
                padding = padding,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBook = onCreateAccountClick,
                onCustomerLogin = onCustomerLoginClick,
                onWorkerLogin = onWorkerLoginClick,
                onMenuClick = onMenuClick,
                onServiceCategoryClick = onServiceCategoryClick
            )
            1 -> OffersScreenContent(padding = padding, onBook = onCreateAccountClick)
            2 -> QualityScreenContent(
                padding = padding,
                onBook = onCreateAccountClick,
                riceState = riceState,
                onLoadRice = onLoadRice,
                onOrderRice = onOrderRice,
                onRequestRiceSample = onRequestRiceSample
            )
            else -> PartnerScreenContent(
                padding = padding,
                onBecomePartner = onWorkerRegisterClick,
                onPartnerLogin = onWorkerLoginClick
            )
        }
    }
}

@Composable
private fun PublicHomeContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    query: String,
    onQueryChange: (String) -> Unit,
    onBook: () -> Unit,
    onCustomerLogin: () -> Unit,
    onWorkerLogin: () -> Unit,
    onMenuClick: () -> Unit,
    onServiceCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CaterHubColors.Background)
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PublicTopBar(onCustomerLogin, onWorkerLogin)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            MarketplaceSectionHeading("What are you planning?")
            Text(
                "Browse event services before you book.",
                color = CaterHubColors.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        ServiceDiscoverySearchField(query = query, onQueryChange = onQueryChange)

        if (query.isBlank()) {
            MarketplaceQuickActions(onBookCatering = onBook, onExploreMenu = onMenuClick)
            MarketplaceSectionHeading(
                title = "Explore services",
                subtitle = "Browse every category. Sign in only when you are ready to book."
            )
            ServiceCategoryGrid(onCategoryClick = onServiceCategoryClick)
            ComingSoonServiceSection(ComingSoonServices.FunctionAndBanquetHalls)
        } else {
            MarketplaceSectionHeading("Search results")
            ServiceDiscoverySearchResults(query = query, onCategoryClick = onServiceCategoryClick)
        }
        CaterHubContactSupportSection()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PublicTopBar(onCustomerLogin: () -> Unit, onWorkerLogin: () -> Unit) {
    var showLoginOptions by rememberSaveable { mutableStateOf(false) }

    CaterHubMarketplaceHeader {
        IconButton(
            onClick = { showLoginOptions = true },
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(50)).background(CaterHubColors.Green)
        ) {
            Icon(Icons.Filled.Person, contentDescription = "Sign in", tint = CaterHubColors.Surface)
        }
    }

    if (showLoginOptions) {
        AlertDialog(
            onDismissRequest = { showLoginOptions = false },
            title = { Text("Sign in to CaterHub", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LoginChoiceCard(
                        title = "Customer login",
                        description = "Book services and manage your events",
                        icon = { Icon(Icons.Filled.Person, contentDescription = null, tint = CaterHubColors.Green) },
                        onClick = { showLoginOptions = false; onCustomerLogin() }
                    )
                    LoginChoiceCard(
                        title = "Partner login",
                        description = "View and manage your work",
                        icon = { Icon(Icons.Filled.Groups, contentDescription = null, tint = CaterHubColors.Green) },
                        onClick = { showLoginOptions = false; onWorkerLogin() }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoginOptions = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LoginChoiceCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CaterHubColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CaterHubColors.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(
                Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = CaterHubColors.Ink)
                Text(description, color = CaterHubColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CaterHubColors.Green)
        }
    }
}

@Composable
fun PublicBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
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
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = colors
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Filled.Celebration, contentDescription = "Offers") },
            label = { Text("Offers") },
            colors = colors
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Quality") },
            label = { Text("Quality") },
            colors = colors
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Filled.Work, contentDescription = "Partner") },
            label = { Text("Partner") },
            colors = colors
        )
    }
}
