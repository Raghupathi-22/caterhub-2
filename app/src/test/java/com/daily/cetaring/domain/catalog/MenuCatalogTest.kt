package com.daily.cetaring.domain.catalog

import org.junit.Assert.assertTrue
import org.junit.Test

class MenuCatalogTest {
    @Test
    fun biryaniSearchFindsBiryaniItems() {
        val results = MenuCatalog.filteredItems(
            selectedCategoryId = MenuCatalog.FILTER_ALL,
            query = "biryani"
        )
        assertTrue(results.any { it.name.contains("Hyderabadi Chicken Biryani", ignoreCase = true) })
    }

    @Test
    fun categoryFilterLimitsItemsToSelectedCategory() {
        val results = MenuCatalog.filteredItems(
            selectedCategoryId = MenuCatalog.CATEGORY_WATER,
            query = ""
        )
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.categoryId == MenuCatalog.CATEGORY_WATER })
    }
}
