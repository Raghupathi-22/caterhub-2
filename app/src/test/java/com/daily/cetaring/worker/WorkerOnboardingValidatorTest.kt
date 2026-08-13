package com.daily.cetaring.worker

import com.daily.cetaring.data.remote.dto.WorkerOnboardingValidator
import com.daily.cetaring.data.remote.dto.WorkerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerOnboardingValidatorTest {
    @Test
    fun roleIsRequired() {
        assertEquals("Please select a worker role.", WorkerOnboardingValidator.validateRole(null))
        assertNull(WorkerOnboardingValidator.validateRole(WorkerType.CHEF))
    }

    @Test
    fun experienceMustBeRealistic() {
        assertEquals("Please enter experience in years.", WorkerOnboardingValidator.validateExperience(null))
        assertEquals("Experience cannot be negative.", WorkerOnboardingValidator.validateExperience(-1))
        assertEquals("Please enter a valid experience value.", WorkerOnboardingValidator.validateExperience(61))
        assertNull(WorkerOnboardingValidator.validateExperience(5))
    }

    @Test
    fun requiredTextFieldsReturnFriendlyMessages() {
        assertEquals("Please enter area.", WorkerOnboardingValidator.validateRequired("", "area"))
        assertNull(WorkerOnboardingValidator.validateRequired("Kondapur", "area"))
    }
}

