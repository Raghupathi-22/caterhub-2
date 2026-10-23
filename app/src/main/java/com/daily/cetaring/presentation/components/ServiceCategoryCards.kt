package com.daily.cetaring.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceCategoryDefinition
import com.daily.cetaring.domain.catalog.ServiceListingCatalog

private val CardMaroon = Color(0xFF971B1E)
private val CardGold = Color(0xFF9A6A0B)
private val CardGoldSoft = Color(0xFFFFF4D9)
private val CardInk = Color(0xFF292524)
private val CardMuted = Color(0xFF6B625B)
private val CardBorder = Color(0xFFE4D9C6)

/**
 * Image-rich grid of every customer-facing service category. Shared by the public (guest) home
 * page and the signed-in home page so both always show the same catalogue categories.
 */
@Composable
fun ServiceCategoryGrid(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<ServiceCategoryDefinition> = ServiceCatalog.customerCategories
) {
    val uniqueCategories = remember(categories) { categories.distinctBy { it.id } }
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        // Image tiles in a grid; fall back to full-width rows when tiles would get too narrow to
        // keep titles and descriptions readable (small phones or large accessibility font sizes).
        val columns = when {
            maxWidth < 330.dp || fontScale >= 1.35f -> 1
            maxWidth < 600.dp -> 2
            else -> 3
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            uniqueCategories.chunked(columns).forEach { rowCategories ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCategories.forEach { category ->
                        if (columns == 1) {
                            ServiceCategoryCard(
                                category = category,
                                onClick = { onCategoryClick(category.id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            ServiceCategoryTile(
                                category = category,
                                onClick = { onCategoryClick(category.id) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                    repeat(columns - rowCategories.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun ServiceCategoryTile(
    category: ServiceCategoryDefinition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = remember(category.id) { ServiceImagery.categoryVisual(category.id) }
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                iconSize = 30.dp,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
            )
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    category.title,
                    color = CardInk,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(category.subtitle, color = CardMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.heightIn(min = 24.dp).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Explore", color = CardMaroon, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = CardMaroon, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceCategoryCard(
    category: ServiceCategoryDefinition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = remember(category.id) { ServiceImagery.categoryVisual(category.id) }
    val optionCount = remember(category.id) { ServiceListingCatalog.listingsFor(category.id).size }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServiceImage(
                visual = visual,
                contentDescription = null,
                shape = RoundedCornerShape(18.dp),
                iconSize = 28.dp,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    category.title,
                    color = CardInk,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(category.subtitle, color = CardMuted, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (optionCount > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = CardGoldSoft) {
                            Text(
                                if (optionCount == 1) "1 option" else "$optionCount options",
                                color = CardGold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Text("Explore", color = CardMaroon, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = CardMaroon, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
