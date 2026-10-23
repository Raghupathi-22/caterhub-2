package com.daily.cetaring.features.admin.controller;

import com.daily.cetaring.features.admin.dto.AdminCustomerDtos;
import com.daily.cetaring.features.admin.service.AdminCustomerService;
import com.daily.cetaring.features.admin.service.AdminOperationsService;
import com.daily.cetaring.features.service.service.ServiceRequestService;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.features.worker.controller.WorkerController;
import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.service.WorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the real @PreAuthorize rules (method security proxies) on endpoints that return
 * worker or customer contact details.
 */
@SpringJUnitConfig(AdminContactDetailsAuthorizationTest.Config.class)
class AdminContactDetailsAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        WorkerService workerService() {
            return mock(WorkerService.class);
        }

        @Bean
        AdminCustomerService adminCustomerService() {
            return mock(AdminCustomerService.class);
        }

        @Bean
        AdminOperationsService adminOperationsService() {
            return mock(AdminOperationsService.class);
        }

        @Bean
        AdminOperationsController adminOperationsController(AdminOperationsService adminOperationsService) {
            return new AdminOperationsController(adminOperationsService, mock(ServiceRequestService.class), mock(StaffingRequestRepository.class));
        }

        @Bean
        WorkerController workerController(WorkerService workerService) {
            return new WorkerController(workerService);
        }

        @Bean
        AdminCustomerController adminCustomerController(AdminCustomerService adminCustomerService) {
            return new AdminCustomerController(adminCustomerService);
        }
    }

    @Autowired
    private WorkerController workerController;
    @Autowired
    private AdminCustomerController adminCustomerController;
    @Autowired
    private AdminOperationsController adminOperationsController;
    @Autowired
    private AdminOperationsService adminOperationsService;
    @Autowired
    private WorkerService workerService;
    @Autowired
    private AdminCustomerService adminCustomerService;

    @BeforeEach
    void resetMocks() {
        reset(workerService, adminCustomerService, adminOperationsService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminGetsSelectedWorkersPhoneNumber() {
        when(workerService.getProfile(7L)).thenReturn(
            WorkerDtos.WorkerProfileResponse.builder().id(7L).phoneNumber("+919800000007").build());

        assertEquals("+919800000007", workerController.getProfile(7L).getPhoneNumber());
        verify(workerService).getProfile(7L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_SUPER_ADMIN")
    void superAdminCanListCustomers() {
        AdminCustomerDtos.CustomerPage page = AdminCustomerDtos.CustomerPage.builder().content(List.of()).build();
        when(adminCustomerService.listCustomers(0, 20, null)).thenReturn(page);

        assertEquals(page, adminCustomerController.listCustomers(0, 20, null));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanReadJobsWithAcceptedWorkers() {
        when(adminOperationsService.getAllOrders()).thenReturn(List.of());
        when(adminOperationsService.getServiceRequestDetails(5L)).thenReturn(
            com.daily.cetaring.features.admin.dto.AdminServiceRequestDetailsDTO.builder().id(5L).acceptedWorkerCount(7).build());
        when(adminOperationsService.getStaffingJobDetails(6L)).thenReturn(
            com.daily.cetaring.features.admin.dto.AdminStaffingJobDetailsDTO.builder().id(6L).build());

        assertEquals(List.of(), adminOperationsController.getAllOrders());
        assertEquals(7, adminOperationsController.getServiceRequestDetails(5L).getAcceptedWorkerCount());
        assertEquals(6L, adminOperationsController.getStaffingRequestDetails(6L).getId());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void customerCannotReadWorkerOrCustomerContactDetails() {
        assertDenied();
    }

    @Test
    @WithMockUser(authorities = "ROLE_WORKER")
    void workerCannotReadOtherWorkersOrCustomerContactDetails() {
        assertDenied();
    }

    @Test
    @WithAnonymousUser
    void anonymousCannotReadContactDetails() {
        assertDenied();
    }

    private void assertDenied() {
        assertThrows(AccessDeniedException.class, () -> workerController.getProfile(7L));
        assertThrows(AccessDeniedException.class, () -> workerController.findWorkerProfiles(null, null));
        assertThrows(AccessDeniedException.class, () -> workerController.getProfileByUserId(7L));
        assertThrows(AccessDeniedException.class, () -> adminCustomerController.listCustomers(0, 20, null));
        assertThrows(AccessDeniedException.class, () -> adminOperationsController.getAllOrders());
        assertThrows(AccessDeniedException.class, () -> adminOperationsController.getBookingDetails(1L));
        assertThrows(AccessDeniedException.class, () -> adminOperationsController.getServiceRequestDetails(1L));
        assertThrows(AccessDeniedException.class, () -> adminOperationsController.getStaffingRequestDetails(1L));
        verify(adminOperationsService, never()).getAllOrders();
        verify(adminOperationsService, never()).getServiceRequestDetails(any());
        verify(adminOperationsService, never()).getStaffingJobDetails(any());
        verify(workerService, never()).getProfile(any());
        verify(adminCustomerService, never()).listCustomers(any(), any(), any());
    }
}
