package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.presentation.components.CaterHubBookingCard
import com.daily.cetaring.presentation.components.CaterHubCategoryChip
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.components.CaterHubSectionHeader
import com.daily.cetaring.presentation.viewmodel.HomeUiState
import com.daily.cetaring.presentation.viewmodel.HomeViewModel
import java.util.Calendar

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookCateringClick: () -> Unit,
    onWorkerRegisterClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGuestSizeClick: (Int) -> Unit,
    onEventTypeClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHome() }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(true, {}, { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                NavigationBarItem(false, onBookingsClick, { Icon(Icons.Filled.Celebration, null) }, label = { Text("Bookings") })
                NavigationBarItem(false, onProfileClick, { Icon(Icons.Filled.AccountCircle, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        when (val state = uiState) {
            HomeUiState.Loading -> CaterHubLoadingState("Preparing your CaterHub home...")
            is HomeUiState.Error -> Surface(Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    HomeHeader(state.firstName, onProfileClick, onLogout)
                    CaterHubErrorState(state.message, { viewModel.loadHome() })
                    CaterHubPrimaryButton("Start Booking", onBookCateringClick, Modifier.fillMaxWidth())
                }
            }
            is HomeUiState.Loaded -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HomeHeader(state.firstName, onProfileClick, onLogout)
                SearchField()
                Hero(onBookCateringClick)
                Specialities()
                CaterHubMenu(onBookCateringClick)
                Offers()
                Services(onBookCateringClick, onWorkerRegisterClick)
                EventTypes(onEventTypeClick)
                GuestSizes(onGuestSizeClick)
                CaterHubSectionHeader("My bookings", if (state.bookingCount > 0) "View all" else null, onBookingsClick)
                if (state.upcomingBooking != null) {
                    CaterHubBookingCard(state.upcomingBooking, { onBookingClick(state.upcomingBooking.id) }, compact = true)
                } else {
                    CaterHubEmptyState("No bookings yet", "Plan your next event with CaterHub.", actionText = "Start Booking", onActionClick = onBookCateringClick)
                }
                Spacer(Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(firstName: String, onProfileClick: () -> Unit, onLogout: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("${greeting()}, ${firstName.ifBlank { "there" }}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Plan your next event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text("Hyderabad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        IconButton(onClick = onProfileClick) { Icon(Icons.Filled.AccountCircle, "Profile") }
        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") }
    }
}

@Composable
private fun SearchField() {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        leadingIcon = { Icon(Icons.Filled.Search, null) },
        placeholder = { Text("Search catering, food, chef or staff") },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Hero(onBook: () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.RestaurantMenu, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Text("Plan your event with CaterHub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Reliable catering, trained staff, and a simpler way to host.", color = MaterialTheme.colorScheme.onPrimaryContainer)
            CaterHubPrimaryButton("Start Booking", onBook, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Specialities() {
    CaterHubSectionHeader("Our specialities")
    val values = listOf(
        "Quality Catering" to Icons.Filled.Restaurant,
        "Experienced Chefs" to Icons.Filled.RestaurantMenu,
        "Trained Serving Staff" to Icons.Filled.Groups,
        "Kitchen Helpers" to Icons.Filled.Handyman,
        "Event Support" to Icons.Filled.Celebration,
        "Verified Workers" to Icons.Filled.Verified
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (title, icon) ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Icon(icon, null, Modifier.padding(8.dp).size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CaterHubMenu(onBook: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onBook), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("CaterHub Menu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Browse meal and menu categories for every occasion.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BookingOptions.menuCategories.forEach { CaterHubCategoryChip(it, false, onBook) }
            }
        }
    }
}

@Composable
private fun Offers() {
    CaterHubSectionHeader("Offers")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Special Event Packages", "Bulk Guest Offers", "Catering + Staff Packages", "Festival & Event Specials").forEachIndexed { index, title ->
            Card(Modifier.width(210.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (index % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    Text("Packages tailored to your event requirements.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Services(onBook: () -> Unit, onWorkerRegister: () -> Unit) {
    CaterHubSectionHeader("Services")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            Triple("Full Catering", Icons.Filled.RestaurantMenu, onBook),
            Triple("Food Only", Icons.Filled.Restaurant, onBook),
            Triple("Chef", Icons.Filled.Restaurant, onBook),
            Triple("Serving Staff", Icons.Filled.Groups, onBook),
            Triple("Kitchen Helper", Icons.Filled.Handyman, onBook),
            Triple("Cleaning Staff", Icons.Filled.CleaningServices, onBook)
        ).forEach { (title, icon, action) -> ServiceTile(title, icon, action) }
    }
}

@Composable
private fun ServiceTile(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(Modifier.width(142.dp).clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EventTypes(onEventTypeClick: (String) -> Unit) {
    CaterHubSectionHeader("Event types")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.eventTypes.forEach { CaterHubCategoryChip(it, false, { onEventTypeClick(it) }) }
    }
}

@Composable
private fun GuestSizes(onGuestSizeClick: (Int) -> Unit) {
    CaterHubSectionHeader("Popular guest sizes")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.guestQuickOptions.forEach { guests -> CaterHubCategoryChip("$guests guests", false, { onGuestSizeClick(guests) }) }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}
