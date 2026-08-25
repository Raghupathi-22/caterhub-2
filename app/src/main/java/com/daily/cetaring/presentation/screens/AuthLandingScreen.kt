package com.daily.cetaring.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.R

private val Cream = Color(0xFFFFFCF6)
private val Cream2 = Color(0xFFFFF8E8)
private val Maroon = Color(0xFF8E171C)
private val MaroonDark = Color(0xFF6F1015)
private val Gold = Color(0xFFC88912)
private val Green = Color(0xFF0D5B22)
private val GreenDark = Color(0xFF0A421A)
private val PaleGreen = Color(0xFFEAF4E7)
private val Border = Color(0xFFE3D9C7)
private val TextDark = Color(0xFF2B2622)
private val Muted = Color(0xFF6C655D)

@Composable
fun AuthLandingScreen(
    onCreateAccountClick: () -> Unit,
    onCustomerLoginClick: () -> Unit,
    onWorkerRegisterClick: () -> Unit,
    onWorkerLoginClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            PublicBottomBar(selectedTab) { selectedTab = it }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> PublicHomeContent(
                padding = padding,
                onBook = onCreateAccountClick,
                onCustomerLogin = onCustomerLoginClick,
                onWorkerRegister = onWorkerRegisterClick,
                onWorkerLogin = onWorkerLoginClick
            )
            1 -> OffersScreenContent(
                padding = padding,
                onBook = onCreateAccountClick
            )
            else -> QualityScreenContent(padding = padding, onBook = onCreateAccountClick)
        }
    }
}

@Composable
private fun PublicHomeContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onBook: () -> Unit,
    onCustomerLogin: () -> Unit,
    onWorkerRegister: () -> Unit,
    onWorkerLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PublicTopBar(onCustomerLogin, onWorkerLogin)
        LogoBlock()

        Text("Delicious Food.", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, color = Maroon)
        Text("Memorable Moments.", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
        Text(
            "From weddings to small get-togethers,\nwe make every occasion special.",
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = TextDark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "What would you like to do?",
                color = TextDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        ActionCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Book Catering",
            subtitle = "Plan your event\nin a few easy steps",
            color = Maroon,
            icon = Icons.Filled.RestaurantMenu,
            onClick = onBook
        )
        PublicJoinCtaSection(onWorkerRegister = onWorkerRegister)

        OffersPreview(onBook)
        SpecialitiesPreview()
        PackagesPreview(onBook)
        WhyChooseUs()
        ContactCard()
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun PublicTopBar(onCustomerLogin: () -> Unit, onWorkerLogin: () -> Unit) {
    var showLoginOptions by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, null, tint = Maroon, modifier = Modifier.size(22.dp))
            Text("Hyderabad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("⌄", color = Maroon, fontSize = 20.sp, modifier = Modifier.padding(start = 3.dp))
        }
        Icon(Icons.Filled.NotificationsNone, "Notifications", tint = TextDark, modifier = Modifier.size(27.dp))
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = { showLoginOptions = true }, modifier = Modifier.size(42.dp).clip(CircleShape).background(Maroon)) {
            Icon(Icons.Filled.Person, "Login", tint = Color.White)
        }
    }

    if (showLoginOptions) {
        AlertDialog(
            onDismissRequest = { showLoginOptions = false },
            title = { Text("Login to CaterHub", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showLoginOptions = false; onCustomerLogin() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RestaurantMenu, null, tint = Maroon)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Customer Login", fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Book catering and manage bookings", fontSize = 11.sp, color = Muted)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showLoginOptions = false; onWorkerLogin() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7EF))
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Groups, null, tint = Green)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Partner Login", fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Find flexible catering opportunities", fontSize = 11.sp, color = Muted)
                            }
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
private fun LogoBlock() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(R.drawable.public_logo),
            contentDescription = "CaterHub logo",
            modifier = Modifier.height(178.dp).fillMaxWidth(0.72f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(178.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.96f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun PublicJoinCtaSection(onWorkerRegister: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Join CaterHub", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Text("Work with us and earn with your skills", color = Green, fontWeight = FontWeight.Bold)
            Text(
                "Join as a catering professional, decorator,\nDJ, singer, photographer, beauty professional,\nor other event service provider.",
                color = TextDark,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onWorkerRegister),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Green)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Join CaterHub →", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun OffersPreview(onBook: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream2), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Campaign, null, tint = Gold, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("TODAY'S OFFERS", color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                Text("View All →", color = Maroon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OfferMiniCard("Wedding Special", "Catering packages\nfrom ₹499 / person", R.drawable.public_offer_wedding, onBook)
                OfferMiniCard("Family Function", "Special pricing for\n100+ guests", R.drawable.public_offer_family, onBook)
            }
        }
    }
}

@Composable
private fun OfferMiniCard(title: String, subtitle: String, image: Int, onClick: () -> Unit) {
    Card(Modifier.width(270.dp).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(Modifier.height(105.dp)) {
            Column(Modifier.weight(1f).padding(10.dp), verticalArrangement = Arrangement.Center) {
                Text(title, color = Maroon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextDark, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 4.dp))
                Text("View Offer →", color = Maroon, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
            }
            Image(painterResource(image), null, Modifier.width(92.dp).fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun SpecialitiesPreview() {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("🍲 Our Specialities", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.weight(1f))
            Text("View Full Menu  ›", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(9.dp))
        val items = listOf(
            "Biryani" to R.drawable.public_biryani,
            "Starters" to R.drawable.public_starters,
            "Main Course" to R.drawable.public_main_course,
            "Veg Specials" to R.drawable.public_veg_specials,
            "Desserts" to R.drawable.public_desserts,
            "Beverages" to R.drawable.public_beverages
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (name, image) ->
                Card(Modifier.width(100.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(image), null, Modifier.fillMaxWidth().height(82.dp), contentScale = ContentScale.Crop)
                        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 7.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PackagesPreview(onBook: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("⭐ Popular Catering Packages", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            Spacer(Modifier.weight(1f))
            Text("View All ›", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PackageCard("BASIC PACKAGE", "₹499", Green, listOf("Biryani", "2 Curries", "Rice", "Raita", "Sweet"), onBook)
            PackageCard("CLASSIC PACKAGE", "₹699", Maroon, listOf("2 Starters", "Biryani", "3 Curries", "Dal", "Raita", "Sweet"), onBook)
            PackageCard("PREMIUM PACKAGE", "₹999", Gold, listOf("2 Starters", "Biryani", "4 Curries", "Dal", "Raita", "2 Desserts", "Beverages"), onBook)
        }
    }
}

@Composable
private fun PackageCard(title: String, price: String, color: Color, items: List<String>, onBook: () -> Unit) {
    Card(Modifier.width(190.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column {
            Box(Modifier.fillMaxWidth().background(color).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("100+", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextDark)
                Text("People", fontSize = 10.sp, color = Muted)
                items.forEach { Text("✓  $it", fontSize = 11.sp, color = TextDark) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(price, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
                        Text("/ Person", fontSize = 9.sp, color = Muted)
                    }
                    Card(Modifier.clickable(onClick = onBook), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = color)) {
                        Text("View Details", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyChooseUs() {
    Column {
        Text("Why Choose CaterHub?", color = Maroon, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            WhyItem("Quality\nIngredients", "♧")
            WhyItem("Experienced\nTeam", "♙")
            WhyItem("Hygienic\nPreparation", "✓")
            WhyItem("On-time\nService", "◷")
            WhyItem("Customer\nSatisfaction", "♡")
        }
    }
}

@Composable
private fun WhyItem(text: String, symbol: String) {
    Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, fontSize = 26.sp, color = Maroon)
        Text(text, fontSize = 9.sp, lineHeight = 12.sp, color = TextDark, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ContactCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val phone = "9959095202"
    val internationalPhone = "919959095202"

    fun openWhatsApp() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$internationalPhone")))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$internationalPhone")))
        }
    }

    fun openDialer() {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PaleGreen)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Need Help?", color = GreenDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("We're here for you!", color = TextDark, fontSize = 11.sp)
            }
            ContactButton("WhatsApp", "Chat with us", Icons.Filled.Campaign, Green, ::openWhatsApp)
            Spacer(Modifier.width(10.dp))
            ContactButton("Call Us", "Click to call", Icons.Filled.Phone, Green, ::openDialer)
        }
    }
}

@Composable
private fun ContactButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
        Text(subtitle, fontSize = 8.sp, color = Muted)
    }
}

@Composable
fun PublicBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") }, colors = androidx.compose.material3.NavigationBarItemDefaults.colors(selectedIconColor = Maroon, selectedTextColor = Maroon, indicatorColor = Color(0xFFFFE8E8)))
        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(Icons.Filled.Celebration, null) }, label = { Text("Offers") }, colors = androidx.compose.material3.NavigationBarItemDefaults.colors(selectedIconColor = Maroon, selectedTextColor = Maroon, indicatorColor = Color(0xFFFFE8E8)))
        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(Icons.Filled.CheckCircle, null) }, label = { Text("Quality") }, colors = androidx.compose.material3.NavigationBarItemDefaults.colors(selectedIconColor = Maroon, selectedTextColor = Maroon, indicatorColor = Color(0xFFFFE8E8)))
    }
}
