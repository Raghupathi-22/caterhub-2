package com.daily.cetaring.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.daily.cetaring.domain.catalog.CategoryVisualTone
import com.daily.cetaring.domain.catalog.ServiceCategoryDefinition

data class CategoryUiMeta(
    val icon: ImageVector,
    val accent: Color
)

private val CategoryRed = Color(0xFF971B1E)
private val CategoryGreen = Color(0xFF0A672A)
private val CategoryGold = Color(0xFFC58A16)

fun categoryUiMeta(category: ServiceCategoryDefinition): CategoryUiMeta =
    when (category.visualTone) {
        CategoryVisualTone.FOOD -> CategoryUiMeta(Icons.Filled.Restaurant, CategoryGold)
        CategoryVisualTone.DECORATION -> CategoryUiMeta(Icons.Filled.Celebration, CategoryGreen)
        CategoryVisualTone.ENTERTAINMENT -> CategoryUiMeta(Icons.Filled.MusicNote, CategoryRed)
        CategoryVisualTone.BEAUTY -> CategoryUiMeta(Icons.Filled.ContentCut, Color(0xFFA24A7A))
        CategoryVisualTone.PHOTOGRAPHY -> CategoryUiMeta(Icons.Filled.CameraAlt, CategoryGreen)
        CategoryVisualTone.RELIGIOUS -> CategoryUiMeta(Icons.Filled.TempleHindu, CategoryGold)
        CategoryVisualTone.SUPPORT -> CategoryUiMeta(Icons.Filled.Event, CategoryGreen)
        CategoryVisualTone.RENTALS -> CategoryUiMeta(Icons.Filled.Inventory2, Color(0xFF7A6B45))
        CategoryVisualTone.TRANSPORT -> CategoryUiMeta(Icons.Filled.DirectionsCar, CategoryGreen)
        CategoryVisualTone.OTHER -> CategoryUiMeta(Icons.Filled.MoreHoriz, CategoryGold)
    }
