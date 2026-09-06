package com.daily.cetaring.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.MenuCatalog
import com.daily.cetaring.domain.catalog.MenuFoodType
import com.daily.cetaring.domain.catalog.MenuItem
import com.daily.cetaring.presentation.components.CaterHubCategoryChip
import com.daily.cetaring.presentation.components.CaterHubEmptyState
import com.daily.cetaring.presentation.components.CaterHubPrimaryButton

private val MenuCream = Color(0xFFFFFCF5)
private val MenuRed = Color(0xFF971B1E)
private val MenuGreen = Color(0xFF0A672A)
private val MenuGold = Color(0xFFC58A16)
private val MenuInk = Color(0xFF292524)
private val MenuMuted = Color(0xFF6B625B)
private val MenuBorder = Color(0xFFE4D9C6)

private data class MenuSection(
    val categoryId: String,
    val subCategoryId: String,
    val items: List<MenuItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onBackClick: () -> Unit,
    onBookCateringClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(MenuCatalog.FILTER_ALL) }

    val visibleItems = remember(selectedCategoryId, query) {
        MenuCatalog.filteredItems(selectedCategoryId = selectedCategoryId, query = query)
    }
    val sections = remember(visibleItems) {
        buildList {
            var currentKey: Pair<String, String>? = null
            var bucket = mutableListOf<MenuItem>()
            visibleItems.forEach { item ->
                val key = item.categoryId to item.subCategoryId
                if (currentKey == null) {
                    currentKey = key
                }
                if (key != currentKey) {
                    add(
                        MenuSection(
                            categoryId = currentKey!!.first,
                            subCategoryId = currentKey!!.second,
                            items = bucket.toList()
                        )
                    )
                    bucket = mutableListOf()
                    currentKey = key
                }
                bucket.add(item)
            }
            if (currentKey != null && bucket.isNotEmpty()) {
                add(
                    MenuSection(
                        categoryId = currentKey!!.first,
                        subCategoryId = currentKey!!.second,
                        items = bucket.toList()
                    )
                )
            }
        }
    }

    Scaffold(
        containerColor = MenuCream,
        topBar = {
            TopAppBar(
                title = { Text("CaterHub Menu", color = MenuInk, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MenuRed)
                    }
                },
                actions = {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Search, contentDescription = "Clear search", tint = MenuInk)
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    CaterHubPrimaryButton(
                        text = "Book Catering",
                        onClick = onBookCateringClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MenuBorder),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Search menu items") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MenuMuted
                                )
                            }
                        )
                        Text(
                            text = "Search by item, description, category, type, or subcategory.",
                            color = MenuMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MenuCatalog.filterCategories) { category ->
                        CaterHubCategoryChip(
                            text = category.displayName,
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            icon = iconForCategory(category.iconKey)
                        )
                    }
                }
            }

            if (sections.isEmpty()) {
                item {
                    CaterHubEmptyState(
                        title = "No menu items found",
                        message = "Try a different search or category."
                    )
                }
            } else {
                items(sections) { section ->
                    val category = MenuCatalog.category(section.categoryId)
                    val subCategory = MenuCatalog.subCategory(section.subCategoryId)
                    val showCategoryTitle = selectedCategoryId == MenuCatalog.FILTER_ALL
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MenuBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (showCategoryTitle && category != null) {
                                Text(
                                    text = category.displayName,
                                    color = MenuRed,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = subCategory?.name ?: "Menu Items",
                                color = MenuInk,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            section.items.forEach { item ->
                                MenuItemRow(item = item)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        FoodTypeIndicator(type = item.type)
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.name,
                color = MenuInk,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.description,
                color = MenuMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (item.price != null) {
            Text(
                text = if (item.unit.isNullOrBlank()) item.price else "${item.price} / ${item.unit}",
                color = MenuGreen,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FoodTypeIndicator(type: MenuFoodType) {
    val tint = when (type) {
        MenuFoodType.VEG -> MenuGreen
        MenuFoodType.NON_VEG -> MenuRed
        MenuFoodType.EGG -> MenuGold
        MenuFoodType.OTHER -> MenuMuted
    }
    val label = when (type) {
        MenuFoodType.VEG -> "VEG"
        MenuFoodType.NON_VEG -> "NON-VEG"
        MenuFoodType.EGG -> "EGG"
        MenuFoodType.OTHER -> "OTHER"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(tint, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun iconForCategory(iconKey: String): ImageVector = when (iconKey) {
    "breakfast" -> Icons.Filled.FreeBreakfast
    "lunch" -> Icons.Filled.LunchDining
    "nonveg" -> Icons.Filled.DinnerDining
    "biryani" -> Icons.Filled.Restaurant
    "snacks" -> Icons.Filled.Fastfood
    "sweets" -> Icons.Filled.Cake
    "beverages" -> Icons.Filled.LocalCafe
    "water" -> Icons.Filled.LocalDrink
    "packages" -> Icons.Filled.RestaurantMenu
    else -> Icons.Filled.Home
}
