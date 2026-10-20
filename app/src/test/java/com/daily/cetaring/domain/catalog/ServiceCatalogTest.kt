package com.daily.cetaring.domain.catalog

import com.daily.cetaring.data.remote.dto.WorkerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceCatalogTest {
    @Test
    fun cateringStaffCategoryIsCustomerVisible() {
        val category = ServiceCatalog.category("catering-staff")
        requireNotNull(category)
        assertEquals("Catering Staff", category.title)
        assertEquals("CATERING_STAFF", category.serviceType)
        assertTrue(ServiceCatalog.customerCategories.any { it.id == "catering-staff" })
    }

    @Test
    fun cateringStaffContainsExactlyRequestedWorkerRoles() {
        val roles = ServiceCatalog.customerRolesForCategory("catering-staff")
        assertEquals(
            setOf(WorkerType.CATERING_BOY, WorkerType.CATERING_GIRL, WorkerType.CHEF, WorkerType.KITCHEN_HELPER),
            roles.mapNotNull { it.workerType }.toSet()
        )
    }
}
