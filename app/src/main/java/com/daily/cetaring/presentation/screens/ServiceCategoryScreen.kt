@file:OptIn(ExperimentalMaterial3Api::class)

package com.daily.cetaring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceListing
import com.daily.cetaring.domain.catalog.ServiceListingAction
import com.daily.cetaring.domain.catalog.ServiceListingCatalog
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.ServiceImage
import com.daily.cetaring.presentation.components.ServiceImagery
import java.text.NumberFormat
import java.util.Locale

private val Cream = Color(0xFFFFFCF5)
private val Maroon = Color(0xFF971B1E)
private val Gold = Color(0xFFC58A16)
private val GoldLight = Color(0xFFFFE7A3)
private val Green = Color(0xFF0A672A)
private val Ink = Color(0xFF292524)
private val Muted = Color(0xFF6B625B)
private val Border = Color(0xFFE4D9C6)

private val CardShape = RoundedCornerShape(22.dp)
private val ImageShape = RoundedCornerShape(18.dp)

@Composable
fun ServiceCategoryScreen(
    categoryId: String,
    onBackClick: () -> Unit,
    /** Opens the service request flow; a non-null id pre-selects that service. */
    onBookService: (serviceId: String?) -> Unit,
    onBookCatering: () -> Unit,
    onExploreMenu: (menuCategoryId: String) -> Unit
) {
    val category = remember(categoryId) { ServiceCatalog.category(categoryId) }
    val listings = remember(categoryId) { ServiceListingCatalog.listingsFor(categoryId) }
    var searchActive by rememberSaveable(categoryId) { mutableStateOf(false) }
    var query by rememberSaveable(categoryId) { mutableStateOf("") }
    val visibleListings = remember(listings, query) { ServiceListingCatalog.search(listings, query) }
    val focusManager = LocalFocusManager.current
    val title = category?.title ?: "Services"

    fun closeSearch() {
        searchActive = false
        query = ""
        focusManager.clearFocus()
    }

    BackHandler(enabled = searchActive) { closeSearch() }

    fun onListingClick(listing: ServiceListing) {
        when (listing.action) {
            ServiceListingAction.BOOK_SERVICE -> onBookService(listing.id)
            ServiceListingAction.BOOK_CATERING -> onBookCatering()
            ServiceListingAction.EXPLORE_MENU -> onExploreMenu(listing.id)
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            CategoryTopBar(
                title = title,
                searchActive = searchActive,
                query = query,
                onQueryChange = { query = it },
                onBackClick = { if (searchActive) closeSearch() else onBackClick() },
                onSearchClick = { searchActive = true },
                onCloseSearch = ::closeSearch,
                onSearchSubmit = { focusManager.clearFocus() }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier.fillMaxSize().background(Cream).padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!searchActive && category != null) {
                item(key = "hero", span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHero(
                        categoryId = categoryId,
                        title = category.title,
                        subtitle = category.subtitle,
                        serviceCount = listings.size
                    )
                }
            }
            item(key = "section", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(top = 4.dp)) {
                    Text(
                        text = if (query.isBlank()) "Choose a service" else "${visibleListings.size} result${if (visibleListings.size == 1) "" else "s"}",
                        color = Maroon,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (query.isBlank() && listings.isNotEmpty()) {
                        Text(
                            text = if (categoryId == ServiceListingCatalog.CATERING_CATEGORY_ID) {
                                "Book event catering or explore the menu by category."
                            } else {
                                "Tap Book Now to start a request. You can add more services in the next step."
                            },
                            color = Muted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(visibleListings, key = { it.id }) { listing ->
                ServiceListingCard(listing = listing, onClick = { onListingClick(listing) })
            }
            if (visibleListings.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    if (listings.isEmpty()) {
                        CaterHubEmptyState(
                            title = "No services available yet",
                            message = "Services for $title will appear here once they are available.",
                            actionText = "Go back",
                            onActionClick = onBackClick
                        )
                    } else {
                        CaterHubEmptyState(
                            title = "No matching services",
                            message = "Nothing in $title matches \"${query.trim()}\". Try a different word.",
                            actionText = "Clear search",
                            onActionClick = { query = "" }
                        )
                    }
                }
            }
            if (query.isBlank() && listings.any { it.action == ServiceListingAction.BOOK_SERVICE } && listings.size > 1) {
                item(key = "multi", span = { GridItemSpan(maxLineSpan) }) {
                    MultiServiceCard(onClick = { onBookService(null) })
                }
            }
        }
    }
}

@Composable
private fun CategoryTopBar(
    title: String,
    searchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCloseSearch: () -> Unit,
    onSearchSubmit: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Maroon)
            }
        },
        title = {
            if (searchActive) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    placeholder = { Text("Search $title", color = Muted) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Maroon,
                        focusedIndicatorColor = Maroon,
                        unfocusedIndicatorColor = Border
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, maxLines = 2)
            }
        },
        actions = {
            if (searchActive) {
                IconButton(onClick = onCloseSearch) {
                    Icon(Icons.Filled.Close, contentDescription = "Close search", tint = Ink)
                }
            } else {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "Search $title", tint = Ink)
                }
            }
        }
    )
}

@Composable
private fun CategoryHero(categoryId: String, title: String, subtitle: String, serviceCount: Int) {
    val visual = remember(categoryId) { ServiceImagery.bannerVisual(categoryId) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 196.dp)) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                iconSize = 48.dp,
                modifier = Modifier.matchParentSize()
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.72f))))
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(top = 64.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = Gold.copy(alpha = 0.92f)) {
                    Text(
                        text = "CATERHUB • $serviceCount option${if (serviceCount == 1) "" else "s"}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.semantics { heading() }
                )
                Text(subtitle, color = GoldLight, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ServiceListingCard(listing: ServiceListing, onClick: () -> Unit) {
    val visual = remember(listing.categoryId, listing.id) { ServiceImagery.serviceVisual(listing.categoryId, listing.id) }
    val ctaText = if (listing.action == ServiceListingAction.EXPLORE_MENU) "Explore" else "Book Now"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                shape = ImageShape,
                iconSize = 30.dp,
                modifier = Modifier.size(104.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(listing.title, color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(listing.description, color = Muted, style = MaterialTheme.typography.bodyMedium)
                priceLabel(listing)?.let { (text, color) ->
                    Text(text, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "$ctaText: ${listing.title}" }
                ) {
                    Text(ctaText, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun priceLabel(listing: ServiceListing): Pair<String, Color>? = when {
    listing.unitPrice != null -> "${formatRupees(listing.unitPrice)} / ${listing.priceUnit ?: "item"}" to Maroon
    listing.action == ServiceListingAction.BOOK_SERVICE -> "Price on request" to Green
    else -> null
}

private fun formatRupees(amount: Int): String =
    "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format(amount)

@Composable
private fun MultiServiceCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4DA)),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Need more than one service?", color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Select several services and quantities in a single request.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Maroon),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Select multiple services", color = Maroon, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
