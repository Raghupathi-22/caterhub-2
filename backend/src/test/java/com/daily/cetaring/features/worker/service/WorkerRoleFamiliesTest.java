package com.daily.cetaring.features.worker.service;

import com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerRoleFamiliesTest {

    @Test
    void everyFamilyContainsItsCanonicalType() {
        for (Map.Entry<WorkerType, Set<WorkerType>> family : WorkerRoleFamilies.families().entrySet()) {
            assertTrue(family.getValue().contains(family.getKey()), family.getKey().name());
        }
    }

    @Test
    void nonCanonicalTypesKeepExactMatching() {
        for (WorkerType type : WorkerType.values()) {
            if (!WorkerRoleFamilies.families().containsKey(type)) {
                assertEquals(Set.of(type), WorkerRoleFamilies.matchingJobTypes(type), type.name());
            }
        }
        assertEquals(Set.of(WorkerType.CATERING_BOY), WorkerRoleFamilies.matchingJobTypes(WorkerType.CATERING_BOY));
        assertEquals(Set.of(WorkerType.TANDOOR_CHEF), WorkerRoleFamilies.matchingJobTypes(WorkerType.TANDOOR_CHEF));
        assertEquals(Set.of(WorkerType.SUPERVISOR), WorkerRoleFamilies.matchingJobTypes(WorkerType.SUPERVISOR));
    }

    @Test
    void cateringServiceStaffCoversAgreedServingRoles() {
        assertEquals(
            Set.of(WorkerType.SERVING_STAFF, WorkerType.CATERING_BOY, WorkerType.CATERING_GIRL,
                WorkerType.WAITER, WorkerType.SERVING_BOY, WorkerType.SERVING_GIRL),
            WorkerRoleFamilies.matchingJobTypes(WorkerType.SERVING_STAFF)
        );
    }

    @Test
    void broadRolesCoverTheirSpecialisationsButNotUnrelatedJobs() {
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.CHEF, WorkerType.COOK));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.CHEF, WorkerType.HEAD_CHEF));
        assertFalse(WorkerRoleFamilies.canWorkJob(WorkerType.CHEF, WorkerType.SERVING_BOY));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.BIRYANI_CHEF, WorkerType.TANDOOR_CHEF));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.DJ, WorkerType.SOUND_TECHNICIAN));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.SINGER, WorkerType.FEMALE_SINGER));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.BAND_MELAM_ARTIST, WorkerType.MELAM_ARTIST));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.ANCHOR, WorkerType.MC));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.PUJARI, WorkerType.PANDIT));
        assertFalse(WorkerRoleFamilies.canWorkJob(WorkerType.POOJA_SPECIALIST, WorkerType.PANDIT));
        assertFalse(WorkerRoleFamilies.canWorkJob(WorkerType.KITCHEN_HELPER, WorkerType.CHEF));
    }

    @Test
    void consolidatedCustomerServicesReachRelatedWorkers() {
        // "Balloon & Floral Decoration" is booked as BALLOON_DECORATOR.
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.BALLOON_DECORATOR, WorkerType.BALLOON_DECORATOR));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.FLOWER_DECORATOR, WorkerType.BALLOON_DECORATOR));
        // "Purohit" is booked as PUJARI.
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.PUJARI, WorkerType.PUJARI));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.HOMAM_SPECIALIST, WorkerType.PUJARI));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.WEDDING_RITUAL_SPECIALIST, WorkerType.PUJARI));
        assertFalse(WorkerRoleFamilies.canWorkJob(WorkerType.POOJA_SPECIALIST, WorkerType.PUJARI));
        // Event, Stage & Mandap, and Entrance & Backdrop decoration jobs.
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.EVENT_DECORATOR, WorkerType.ENTRANCE_DECORATOR));
        assertTrue(WorkerRoleFamilies.canWorkJob(WorkerType.MANDAP_DECORATOR, WorkerType.MANDAP_DECORATOR));
    }

    @Test
    void nullTypesNeverMatch() {
        assertTrue(WorkerRoleFamilies.matchingJobTypes(null).isEmpty());
        assertFalse(WorkerRoleFamilies.canWorkJob(WorkerType.CHEF, null));
        assertFalse(WorkerRoleFamilies.canWorkJob(null, WorkerType.CHEF));
    }
}
