@file:OptIn(ExperimentalMaterial3Api::class)

package com.daily.cetaring.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceListingCatalog
import com.daily.cetaring.ui.theme.CaterHubColors
import java.util.Locale

@Composable
fun CaterHubMarketplaceHeader(actions: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                "CaterHub",
                color = CaterHubColors.Green,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Event services, all in one place",
                color = CaterHubColors.Muted,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun MarketplaceQuickActions(
    onBookCatering: () -> Unit,
    onExploreMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
        val buttonModifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBookCatering,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CaterHubColors.Green,
                        contentColor = CaterHubColors.Surface,
                        disabledContainerColor = CaterHubColors.GreenSoft,
                        disabledContentColor = CaterHubColors.Muted
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Filled.RoomService, contentDescription = null, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Book catering", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
                }
                OutlinedButton(
                    onClick = onExploreMenu,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CaterHubColors.Gold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CaterHubColors.Green,
                        disabledContentColor = CaterHubColors.Muted
                    )
                ) {
                    Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Explore menu", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onBookCatering,
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CaterHubColors.Green,
                        contentColor = CaterHubColors.Surface,
                        disabledContainerColor = CaterHubColors.GreenSoft,
                        disabledContentColor = CaterHubColors.Muted
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Filled.RoomService, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Book catering", fontWeight = FontWeight.Bold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onExploreMenu,
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CaterHubColors.Gold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CaterHubColors.Green,
                        disabledContentColor = CaterHubColors.Muted
                    )
                ) {
                    Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Explore menu", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun MarketplaceSectionHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            title,
            color = CaterHubColors.Ink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        subtitle?.let {
            Text(it, color = CaterHubColors.Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ServiceDiscoverySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().semantics {
            contentDescription = "Search service categories and services"
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Search catering, decoration...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = CaterHubColors.Green)
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = CaterHubColors.Muted)
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = CaterHubColors.Ink,
            unfocusedTextColor = CaterHubColors.Ink,
            cursorColor = CaterHubColors.Green,
            focusedBorderColor = CaterHubColors.Green,
            unfocusedBorderColor = CaterHubColors.Border,
            focusedLabelColor = CaterHubColors.Green,
            unfocusedLabelColor = CaterHubColors.Muted,
            focusedPlaceholderColor = CaterHubColors.Muted,
            unfocusedPlaceholderColor = CaterHubColors.Muted,
            focusedContainerColor = CaterHubColors.Surface,
            unfocusedContainerColor = CaterHubColors.Surface
        )
    )
}

@Composable
fun ServiceDiscoverySearchResults(
    query: String,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val results = remember(query) { ServiceListingCatalog.searchCustomerCatalogue(query).take(8) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (results.isEmpty()) {
            Text(
                "No matching categories or services. Try a different search.",
                color = CaterHubColors.Muted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            results.forEach { result ->
                val visual = remember(result.categoryId, result.listingId) {
                    if (result.listingId == null) {
                        ServiceImagery.categoryVisual(result.categoryId)
                    } else {
                        ServiceImagery.serviceVisual(result.categoryId, result.listingId)
                    }
                }
                Card(
                    onClick = { onCategoryClick(result.categoryId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CaterHubColors.Surface),
                    border = BorderStroke(1.dp, CaterHubColors.Border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ServiceImage(
                            visual = visual,
                            contentDescription = "${result.title} service",
                            shape = RoundedCornerShape(14.dp),
                            iconSize = 20.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                result.categoryTitle.uppercase(Locale.ROOT),
                                color = CaterHubColors.Green,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                result.title,
                                color = CaterHubColors.Ink,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                result.description,
                                color = CaterHubColors.Muted,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open ${result.categoryTitle}",
                            tint = CaterHubColors.Green,
                            modifier = Modifier.padding(start = 8.dp).size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
