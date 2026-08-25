package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Cake
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.presentation.viewmodel.HomeViewModel

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val DarkMaroon = Color(0xFF721316)
private val Gold = Color(0xFFC58A16)
private val Green = Color(0xFF0A672A)
private val DarkGreen = Color(0xFF07501F)
private val LightGreen = Color(0xFFEAF4E7)
private val SoftGold = Color(0xFFFFF3D6)
private val TextDark = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)

/**
 * Public/customer home.  The screen intentionally does not call the Home API:
 * the catalogue, offers and quality information are public and must work
 * before authentication.
 */
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
        bottomBar = {
            PublicBottomBar(
                onBookingsClick = onBookingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Cream)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PublicHeader(
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )

            BrandSection()

            SearchSection()

            HeroSection(onBookCateringClick)

            ServiceCategoriesSection(
                onCategoryClick = onServiceCategoryClick
            )

            OffersSection()

            SpecialitiesSection()

            PackagesSection(onBookCateringClick)

            WhyChooseCaterHub()

            HelpSection()

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PublicHeader(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Maroon,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Hyderabad",
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onNotificationsClick) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = "Notifications",
                tint = TextDark,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(onClick = onProfileClick) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Maroon
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

@Composable
private fun BrandSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.public_logo),
            contentDescription = "CaterHub logo",
            modifier = Modifier
                .size(width = 245.dp, height = 155.dp)
                .offset(y = (-8).dp)
                .padding(top = 0.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Delicious Food.",
            color = Maroon,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Memorable Moments.",
            color = Gold,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        Text(
            text = "From weddings to small get-togethers, we make every occasion special.",
            color = TextDark,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        )
    }
}

@Composable
private fun SearchSection() {
    // Kept deliberately simple; the public catalogue is static at this stage.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.RestaurantMenu,
                contentDescription = null,
                tint = Maroon,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Search food, catering or services",
                color = Muted,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun HeroSection(onBookCateringClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Start your event with CaterHub",
                color = DarkMaroon,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Choose your event, guests and food package. We take care of the catering.",
                color = TextDark,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            PrimaryButton(
                "Book Catering & Food",
                Maroon,
                onBookCateringClick
            )
        }
    }
}

@Composable
private fun ServiceActionCard(
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
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = background,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    description,
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = background,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceCategoriesSection(
    onCategoryClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Book Event Services", Icons.Filled.Celebration)
        Text(
            "Choose exactly what you need for your event. Each category has its own booking flow.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        ServiceCatalog.categories.forEachIndexed { index, category ->
            ServiceActionCard(
                title = category.title,
                description = category.subtitle,
                icon = categoryIcon(category.id),
                background = if (index % 2 == 0) Maroon else Green,
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
}

private fun categoryIcon(categoryId: String): ImageVector = when (categoryId) {
    "catering-food" -> Icons.Filled.Restaurant
    "decoration" -> Icons.Filled.Celebration
    "entertainment" -> Icons.Filled.Star
    "beauty" -> Icons.Filled.Star
    "photography-video" -> Icons.Filled.Cake
    "religious-ceremony" -> Icons.Filled.Verified
    "event-support" -> Icons.Filled.Work
    "rentals" -> Icons.Filled.Home
    "transport-logistics" -> Icons.Filled.LocationOn
    else -> Icons.Filled.Groups
}

@Composable
private fun OffersSection() {
    Column {
        SectionHeader("Today's Offers", Icons.Filled.Star)
        Spacer(Modifier.height(9.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OfferCard(
                "Wedding Special",
                "Catering packages from ₹499 / person",
                SoftGold
            )
            OfferCard(
                "Family Function",
                "Special pricing for 100+ guests",
                Color(0xFFF8E9E9)
            )
        }
    }
}

@Composable
private fun OfferCard(
    title: String,
    description: String,
    background: Color
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                color = Maroon,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(description, color = TextDark)
            Text(
                "View Offer  →",
                color = Maroon,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SpecialitiesSection() {
    val items = listOf(
        "Biryani" to R.drawable.public_biryani,
        "Starters" to R.drawable.public_starters,
        "Main Course" to R.drawable.public_main_course,
        "Veg Specials" to R.drawable.public_veg_specials,
        "Desserts" to R.drawable.public_desserts,
        "Beverages" to R.drawable.public_beverages
    )

    Column {
        SectionHeader("Our Specialities", Icons.Filled.Restaurant)
        Spacer(Modifier.height(9.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { (title, image) ->
                Card(
                    modifier = Modifier.width(125.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(image),
                            contentDescription = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp
                                    )
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            title,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackagesSection(onBookCateringClick: () -> Unit) {
    Column {
        SectionHeader("Popular Catering Packages", Icons.Filled.Star)
        Spacer(Modifier.height(9.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PackageCard(
                "BASIC PACKAGE", "₹499", Green,
                listOf("Biryani", "2 Curries", "Rice", "Raita", "Sweet"),
                onBookCateringClick
            )
            PackageCard(
                "CLASSIC PACKAGE", "₹699", Maroon,
                listOf("2 Starters", "Biryani", "3 Curries", "Dal", "Raita", "Sweet"),
                onBookCateringClick
            )
            PackageCard(
                "PREMIUM PACKAGE", "₹999", Gold,
                listOf("2 Starters", "Biryani", "4 Curries", "Dal", "Raita", "2 Desserts", "Beverages"),
                onBookCateringClick
            )
        }
    }
}

@Composable
private fun PackageCard(
    title: String,
    price: String,
    accent: Color,
    items: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(285.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        accent,
                        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }

            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    "100+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text("People", color = Muted)

                items.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(item, color = TextDark)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            price,
                            color = accent,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("/ Person", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }

                    Surface(
                        modifier = Modifier
                            .clickable(onClick = onClick)
                            .clip(RoundedCornerShape(12.dp)),
                        color = accent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "View Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyChooseCaterHub() {
    Column {
        Text(
            "Why Choose CaterHub?",
            modifier = Modifier.fillMaxWidth(),
            color = Maroon,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrustItem(Icons.Filled.Verified, "Quality", "Ingredients")
            TrustItem(Icons.Filled.Groups, "Experienced", "Team")
            TrustItem(Icons.Filled.CheckCircle, "Hygienic", "Preparation")
            TrustItem(Icons.Filled.Celebration, "On-time", "Service")
            TrustItem(Icons.Filled.Star, "Customer", "Satisfaction")
        }
    }
}

@Composable
private fun TrustItem(icon: ImageVector, first: String, second: String) {
    Column(
        modifier = Modifier.width(66.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Maroon, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            first,
            color = TextDark,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            second,
            color = TextDark,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HelpSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Need Help?",
                    color = DarkGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("We're here for you!", color = TextDark)
            }
            ContactButton("WhatsApp")
            Spacer(Modifier.width(10.dp))
            ContactButton("Call Us")
        }
    }
}

@Composable
private fun ContactButton(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(45.dp),
            shape = CircleShape,
            color = Green
        ) {
            Icon(
                Icons.Filled.Call,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.padding(11.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = Maroon,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text("View All  →", color = TextDark, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrimaryButton(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PublicBottomBar(
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.Home, "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onBookingsClick,
            icon = { Icon(Icons.Filled.Celebration, "Bookings") },
            label = { Text("Bookings") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.AccountCircle, "Profile") },
            label = { Text("Profile") }
        )
    }
}
