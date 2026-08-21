package com.daily.cetaring.presentation.screens

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.presentation.components.CaterHubBookingCard
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubErrorState
import com.daily.cetaring.presentation.components.CaterHubLoadingState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton
import com.daily.cetaring.presentation.viewmodel.HomeUiState
import com.daily.cetaring.presentation.viewmodel.HomeViewModel
import java.util.Calendar

private val CaterHubCream = Color(0xFFFFFCF5)
private val CaterHubMaroon = Color(0xFF971B1E)
private val CaterHubDarkMaroon = Color(0xFF721316)
private val CaterHubGold = Color(0xFFC58A16)
private val CaterHubGreen = Color(0xFF0A672A)
private val CaterHubDarkGreen = Color(0xFF07501F)
private val CaterHubLightGreen = Color(0xFFEAF4E7)
private val CaterHubSoftGold = Color(0xFFFFF3D6)
private val CaterHubText = Color(0xFF292524)
private val CaterHubMuted = Color(0xFF6B625B)
private val CaterHubBorder = Color(0xFFE4D9C6)

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

    LaunchedEffect(Unit) {
        viewModel.loadHome()
    }

    Scaffold(
        containerColor = CaterHubCream,
        bottomBar = {
            CaterHubBottomBar(
                onBookingsClick = onBookingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->

        when (val state = uiState) {

            HomeUiState.Loading -> {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    color = CaterHubCream
                ) {
                    CaterHubLoadingState("Preparing your CaterHub home...")
                }
            }

            is HomeUiState.Error -> {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    color = CaterHubCream
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        PremiumHeader(
                            onNotificationsClick = onNotificationsClick,
                            onProfileClick = onProfileClick,
                            onLogout = onLogout
                        )

                        CaterHubErrorState(
                            state.message,
                            { viewModel.loadHome() }
                        )

                        CaterHubPrimaryButton(
                            "Start Booking",
                            onBookCateringClick,
                            Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            is HomeUiState.Loaded -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(CaterHubCream)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            top = 10.dp,
                            bottom = 24.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    PremiumHeader(
                        onNotificationsClick = onNotificationsClick,
                        onProfileClick = onProfileClick,
                        onLogout = onLogout
                    )

                    BrandSection()

                    SearchSection()

                    HeroSection(
                        onBookCateringClick = onBookCateringClick
                    )

                    PrimaryActions(
                        onBookCateringClick = onBookCateringClick,
                        onWorkerRegisterClick = onWorkerRegisterClick
                    )

                    OffersSection()

                    SpecialitiesSection()

                    PackagesSection(
                        onBookCateringClick = onBookCateringClick
                    )

                    WhyChooseCaterHub()

                    HelpSection()

                    EventTypesSection(
                        onEventTypeClick = onEventTypeClick
                    )

                    GuestSizesSection(
                        onGuestSizeClick = onGuestSizeClick
                    )

                    MyBookingsSection(
                        state = state,
                        onBookingsClick = onBookingsClick,
                        onBookingClick = onBookingClick,
                        onBookCateringClick = onBookCateringClick
                    )

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* HEADER                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun PremiumHeader(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = CaterHubMaroon,
                modifier = Modifier.size(23.dp)
            )

            Spacer(Modifier.width(3.dp))

            Text(
                text = "Hyderabad",
                color = CaterHubText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onNotificationsClick
        ) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = "Notifications",
                tint = CaterHubText,
                modifier = Modifier.size(27.dp)
            )
        }

        IconButton(
            onClick = onProfileClick
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = CaterHubMaroon
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* BRAND                                                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun BrandSection() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = CaterHubSoftGold,
            border = BorderStroke(1.dp, CaterHubGold)
        ) {
            Icon(
                imageVector = Icons.Filled.RestaurantMenu,
                contentDescription = "CaterHub",
                tint = CaterHubMaroon,
                modifier = Modifier.padding(21.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cater",
                color = CaterHubMaroon,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Hub",
                color = CaterHubGold,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Text(
            text = "BOOK CATERING • FIND CATERING WORK",
            color = CaterHubMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

/* -------------------------------------------------------------------------- */
/* SEARCH                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun SearchSection() {

    var query by remember {
        mutableStateOf("")
    }

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = CaterHubMaroon
            )
        },
        placeholder = {
            Text(
                "Search food, catering or services",
                color = CaterHubMuted
            )
        },
        shape = RoundedCornerShape(17.dp)
    )
}

/* -------------------------------------------------------------------------- */
/* HERO                                                                       */
/* -------------------------------------------------------------------------- */

@Composable
private fun HeroSection(
    onBookCateringClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, CaterHubBorder),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Text(
                text = "Delicious Food.",
                color = CaterHubMaroon,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Memorable Moments.",
                color = CaterHubGold,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "From weddings to small get-togethers, we make every occasion special.",
                color = CaterHubText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 25.sp
            )

            Spacer(Modifier.height(4.dp))

            ButtonLike(
                text = "Start Booking",
                background = CaterHubMaroon,
                onClick = onBookCateringClick
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* PRIMARY ACTIONS                                                            */
/* -------------------------------------------------------------------------- */

@Composable
private fun PrimaryActions(
    onBookCateringClick: () -> Unit,
    onWorkerRegisterClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        PremiumActionCard(
            title = "Book Catering",
            description = "Plan your event in a few easy steps.",
            icon = Icons.Filled.RestaurantMenu,
            background = CaterHubMaroon,
            onClick = onBookCateringClick
        )

        PremiumActionCard(
            title = "Join as a Worker",
            description = "Find catering jobs and event opportunities near you.",
            icon = Icons.Filled.Work,
            background = CaterHubGreen,
            onClick = onWorkerRegisterClick
        )
    }
}

@Composable
private fun PremiumActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = background,
                    modifier = Modifier.padding(17.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = background,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* OFFERS                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun OffersSection() {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        SectionTitle(
            title = "Today's Offers",
            icon = Icons.Filled.Star
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OfferCard(
                title = "Wedding Special",
                description = "Catering packages from ₹499 / person",
                color = CaterHubSoftGold
            )

            OfferCard(
                title = "Family Function",
                description = "Special pricing for 100+ guests",
                color = Color(0xFFF7E9E5)
            )

            OfferCard(
                title = "Festival Special",
                description = "Special catering packages for celebrations",
                color = CaterHubLightGreen
            )
        }
    }
}

@Composable
private fun OfferCard(
    title: String,
    description: String,
    color: Color
) {

    Card(
        modifier = Modifier.width(270.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        border = BorderStroke(1.dp, CaterHubBorder)
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                title,
                color = CaterHubMaroon,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                description,
                color = CaterHubText,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                "View Offer  →",
                color = CaterHubMaroon,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* SPECIALITIES                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun SpecialitiesSection() {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        SectionTitle(
            title = "Our Specialities",
            icon = Icons.Filled.Restaurant
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            val items = listOf(
                "Biryani" to Icons.Filled.Restaurant,
                "Starters" to Icons.Filled.RestaurantMenu,
                "Main Course" to Icons.Filled.Restaurant,
                "Veg Specials" to Icons.Filled.Restaurant,
                "Desserts" to Icons.Filled.Celebration,
                "Beverages" to Icons.Filled.Restaurant
            )

            items.forEach { (title, icon) ->
                SpecialityCard(title, icon)
            }
        }
    }
}

@Composable
private fun SpecialityCard(
    title: String,
    icon: ImageVector
) {

    Card(
        modifier = Modifier.width(125.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, CaterHubBorder)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                modifier = Modifier.size(55.dp),
                shape = CircleShape,
                color = CaterHubSoftGold
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = CaterHubMaroon,
                    modifier = Modifier.padding(15.dp)
                )
            }

            Spacer(Modifier.height(9.dp))

            Text(
                text = title,
                color = CaterHubText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* PACKAGES                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun PackagesSection(
    onBookCateringClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        SectionTitle(
            title = "Popular Catering Packages",
            icon = Icons.Filled.Star
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            PackageCard(
                title = "BASIC PACKAGE",
                price = "₹499",
                color = CaterHubGreen,
                items = listOf(
                    "Biryani",
                    "2 Curries",
                    "Rice",
                    "Raita",
                    "Sweet"
                ),
                onClick = onBookCateringClick
            )

            PackageCard(
                title = "CLASSIC PACKAGE",
                price = "₹699",
                color = CaterHubMaroon,
                items = listOf(
                    "2 Starters",
                    "Biryani",
                    "3 Curries",
                    "Dal",
                    "Raita",
                    "Sweet"
                ),
                onClick = onBookCateringClick
            )

            PackageCard(
                title = "PREMIUM PACKAGE",
                price = "₹999",
                color = CaterHubGold,
                items = listOf(
                    "2 Starters",
                    "Biryani",
                    "4 Curries",
                    "Dal",
                    "Raita",
                    "2 Desserts"
                ),
                onClick = onBookCateringClick
            )
        }
    }
}

@Composable
private fun PackageCard(
    title: String,
    price: String,
    color: Color,
    items: List<String>,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.width(285.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, CaterHubBorder)
    ) {

        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color,
                        RoundedCornerShape(
                            topStart = 22.dp,
                            topEnd = 22.dp
                        )
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                Text(
                    "100+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = CaterHubText
                )

                Text(
                    "People",
                    color = CaterHubMuted
                )

                items.forEach { item ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            item,
                            color = CaterHubText
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            price,
                            color = color,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            "/ Person",
                            color = CaterHubMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clickable(onClick = onClick)
                            .clip(RoundedCornerShape(12.dp)),
                        color = color,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "View Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* WHY CATERHUB                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun WhyChooseCaterHub() {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            "Why Choose CaterHub?",
            modifier = Modifier.fillMaxWidth(),
            color = CaterHubMaroon,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TrustItem(
                Icons.Filled.Verified,
                "Quality",
                "Ingredients"
            )

            TrustItem(
                Icons.Filled.Groups,
                "Experienced",
                "Team"
            )

            TrustItem(
                Icons.Filled.CheckCircle,
                "Hygienic",
                "Preparation"
            )

            TrustItem(
                Icons.Filled.Celebration,
                "On-time",
                "Service"
            )
        }
    }
}

@Composable
private fun TrustItem(
    icon: ImageVector,
    lineOne: String,
    lineTwo: String
) {

    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            icon,
            contentDescription = null,
            tint = CaterHubMaroon,
            modifier = Modifier.size(29.dp)
        )

        Spacer(Modifier.height(5.dp))

        Text(
            lineOne,
            color = CaterHubText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            lineTwo,
            color = CaterHubText,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

/* -------------------------------------------------------------------------- */
/* HELP                                                                       */
/* -------------------------------------------------------------------------- */

@Composable
private fun HelpSection() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CaterHubLightGreen
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    "Need Help?",
                    color = CaterHubDarkGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "We're here for you!",
                    color = CaterHubText
                )
            }

            ContactButton(
                icon = Icons.Filled.Call,
                label = "WhatsApp"
            )

            Spacer(Modifier.width(10.dp))

            ContactButton(
                icon = Icons.Filled.Call,
                label = "Call Us"
            )
        }
    }
}

@Composable
private fun ContactButton(
    icon: ImageVector,
    label: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier.size(45.dp),
            shape = CircleShape,
            color = CaterHubGreen
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.padding(11.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            label,
            color = CaterHubText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/* -------------------------------------------------------------------------- */
/* EVENT TYPES                                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun EventTypesSection(
    onEventTypeClick: (String) -> Unit
) {

    Column {
        SectionTitle(
            "Popular Events",
            Icons.Filled.Celebration
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            BookingOptions.eventTypes.forEach { event ->
                SmallPill(
                    text = event,
                    onClick = {
                        onEventTypeClick(event)
                    }
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* GUEST SIZE                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun GuestSizesSection(
    onGuestSizeClick: (Int) -> Unit
) {

    Column {
        SectionTitle(
            "Popular Guest Sizes",
            Icons.Filled.Groups
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            BookingOptions.guestQuickOptions.forEach { guests ->

                SmallPill(
                    text = "$guests guests",
                    onClick = {
                        onGuestSizeClick(guests)
                    }
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* BOOKINGS                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun MyBookingsSection(
    state: HomeUiState.Loaded,
    onBookingsClick: () -> Unit,
    onBookingClick: (Long) -> Unit,
    onBookCateringClick: () -> Unit
) {

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "My Bookings",
                modifier = Modifier.weight(1f),
                color = CaterHubText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            if (state.bookingCount > 0) {
                Text(
                    "View all →",
                    color = CaterHubMaroon,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        onClick = onBookingsClick
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.upcomingBooking != null) {

            CaterHubBookingCard(
                state.upcomingBooking,
                {
                    onBookingClick(state.upcomingBooking.id)
                },
                compact = true
            )

        } else {

            CaterHubEmptyState(
                "No bookings yet",
                "Plan your next event with CaterHub.",
                actionText = "Start Booking",
                onActionClick = onBookCateringClick
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* COMMON UI                                                                  */
/* -------------------------------------------------------------------------- */

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            icon,
            contentDescription = null,
            tint = CaterHubGold,
            modifier = Modifier.size(25.dp)
        )

        Spacer(Modifier.width(7.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            color = CaterHubMaroon,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            "View All  →",
            color = CaterHubText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallPill(
    text: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = Color.White,
        border = BorderStroke(1.dp, CaterHubBorder)
    ) {

        Text(
            text,
            color = CaterHubText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            )
        )
    }
}

@Composable
private fun ButtonLike(
    text: String,
    background: Color,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = background
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* BOTTOM NAVIGATION                                                          */
/* -------------------------------------------------------------------------- */

@Composable
private fun CaterHubBottomBar(
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onBookingsClick,
            icon = {
                Icon(
                    Icons.Filled.Celebration,
                    contentDescription = "Bookings"
                )
            },
            label = {
                Text("Bookings")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text("Profile")
            }
        )
    }
}