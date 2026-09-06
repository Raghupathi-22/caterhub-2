package com.daily.cetaring.booking

import com.daily.cetaring.data.remote.dto.BookingDraft
import com.daily.cetaring.data.remote.dto.BookingOptions
import com.daily.cetaring.data.remote.dto.CateringMealServiceType
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingMealTypeTest {
    @Test
    fun mealTypeContainsSelectedServicesAndPlan() {
        val draft = BookingDraft(
            selectedFoodServices = setOf(
                CateringMealServiceType.BREAKFAST,
                CateringMealServiceType.LUNCH,
                CateringMealServiceType.DINNER
            ),
            cateringPlan = BookingOptions.planNonVegClassic
        )

        assertEquals(
            "Breakfast + Lunch + Dinner | Non-Veg Classic",
            draft.mealTypeForBackend()
        )
    }
}
