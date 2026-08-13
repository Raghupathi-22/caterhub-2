package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
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
    onFoodTypeClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHome() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = onBookingsClick, icon = { Icon(Icons.Filled.Event, null) }, label = { Text("Bookings") })
                NavigationBarItem(selected = false, onClick = onNotificationsClick, icon = { Icon(Icons.Filled.Notifications, null) }, label = { Text("Notifications") })
                NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        when (val state = uiState) {
            HomeUiState.Loading -> CaterHubLoadingState("Preparing your CaterHub home...")
            is HomeUiState.Error -> Surface(Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    HomeHeader(state.firstName, onLogout, onProfileClick)
                    CaterHubErrorState(message = state.message, onRetry = { viewModel.loadHome() })
                    CaterHubPrimaryButton(text = "Book Catering", onClick = onBookCateringClick, modifier = Modifier.fillMaxWidth())
                }
            }
            is HomeUiState.Loaded -> HomeContent(
                state = state,
                modifier = Modifier.padding(padding),
                onBookCateringClick = onBookCateringClick,
                onWorkerRegisterClick = onWorkerRegisterClick,
                onBookingsClick = onBookingsClick,
                onBookingClick = onBookingClick,
                onGuestSizeClick = onGuestSizeClick,
                onEventTypeClick = onEventTypeClick,
                onFoodTypeClick = onFoodTypeClick,
                onLogout = onLogout,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Loaded,
    modifier: Modifier,
    onBookCateringClick: () -> Unit,
    onWorkerRegisterClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onGuestSizeClick: (Int) -> Unit,
    onEventTypeClick: (String) -> Unit,
    onFoodTypeClick: (String) -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        HomeHeader(state.firstName, onLogout, onProfileClick)
        SearchField()
        PrimaryBookingCard(onBookCateringClick)
        ServiceCategories(onBookCateringClick, onWorkerRegisterClick)
        CompactChipSection("Event Types", BookingOptions.eventTypes, Icons.Filled.Celebration, onEventTypeClick)
        CompactChipSection("Food Types", listOf("Breakfast", "Tiffin", "Lunch", "Dinner", "Snacks", "Beverages", "Full Catering"), Icons.Filled.Restaurant, onFoodTypeClick)
        GuestSizeSection(onGuestSizeClick)
        CaterHubSectionHeader("My Bookings", action = if (state.bookingCount > 0) "View all" else null, onActionClick = onBookingsClick)
        if (state.upcomingBooking != null) {
            Text("Upcoming Booking", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            CaterHubBookingCard(booking = state.upcomingBooking, onClick = { onBookingClick(state.upcomingBooking.id) }, compact = true)
        } else {
            CaterHubEmptyState("No bookings yet", "Book catering for your next event.", actionText = "Book Catering", onActionClick = onBookCateringClick)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeHeader(firstName: String, onLogout: () -> Unit, onProfileClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("${greeting()}, ${firstName.ifBlank { "there" }}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Planning an event?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Hyderabad", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = { }) { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") }
        IconButton(onClick = onProfileClick) { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") }
        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") }
    }
}

@Composable
private fun SearchField() {
    OutlinedTextField(
        value = "",
        onValueChange = { },
        readOnly = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text("Search catering, food, chef or staff") },
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PrimaryBookingCard(onBookCateringClick: () -> Unit) {
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer))).padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                Text("Book Catering", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("Food, chefs & serving staff for your event", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                CaterHubPrimaryButton(text = "Start Booking", onClick = onBookCateringClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ServiceCategories(onBookCateringClick: () -> Unit, onWorkerRegisterClick: () -> Unit) {
    CaterHubSectionHeader("Services")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ServicePill("Catering", Icons.Filled.RestaurantMenu, onBookCateringClick)
        ServicePill("Chef", Icons.Filled.Restaurant, onBookCateringClick)
        ServicePill("Serving Staff", Icons.Filled.Groups, onBookCateringClick)
        ServicePill("Cleaning Staff", Icons.Filled.CleaningServices, onBookCateringClick)
        ServicePill("Helpers", Icons.Filled.Handyman, onWorkerRegisterClick)
    }
}

@Composable
private fun ServicePill(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.width(132.dp).clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(9.dp).size(24.dp)) }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CompactChipSection(title: String, values: List<String>, icon: ImageVector, onClick: (String) -> Unit) {
    CaterHubSectionHeader(title)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value -> CaterHubCategoryChip(text = value, selected = false, onClick = { onClick(value) }, icon = icon) }
    }
}

@Composable
private fun GuestSizeSection(onGuestSizeClick: (Int) -> Unit) {
    CaterHubSectionHeader("Popular Guest Sizes")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingOptions.guestQuickOptions.forEach { guests -> CaterHubCategoryChip(text = "$guests Guests", selected = false, onClick = { onGuestSizeClick(guests) }, icon = Icons.Filled.Work) }
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}
