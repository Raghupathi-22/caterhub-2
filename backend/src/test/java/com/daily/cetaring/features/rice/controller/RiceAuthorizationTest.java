package com.daily.cetaring.features.rice.controller;

import com.daily.cetaring.features.rice.dto.RiceDtos;
import com.daily.cetaring.features.rice.service.RiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Exercises the real @PreAuthorize rules on the rice customer and admin endpoints. */
@SpringJUnitConfig(RiceAuthorizationTest.Config.class)
class RiceAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        RiceService riceService() { return mock(RiceService.class); }

        @Bean
        RiceController riceController(RiceService service) { return new RiceController(service); }

        @Bean
        AdminRiceController adminRiceController(RiceService service) { return new AdminRiceController(service); }
    }

    @Autowired private RiceService service;
    @Autowired private RiceController riceController;
    @Autowired private AdminRiceController adminController;

    @BeforeEach
    void resetMocks() { reset(service); }

    @Test
    @WithAnonymousUser
    void anonymousCanReadProductAndQuoteButNotOrder() {
        riceController.product();
        riceController.quote(50);
        verify(service).publicProduct();
        verify(service).quote(50);
        assertThrows(AccessDeniedException.class, () -> riceController.createOrder(null, new RiceDtos.CreateOrderRequest()));
        assertThrows(AccessDeniedException.class, () -> riceController.requestSample(null, new RiceDtos.CreateSampleRequest()));
        assertThrows(AccessDeniedException.class, () -> riceController.myOrders(null));
        assertAdminDenied();
    }

    @Test
    @WithMockUser(username = "cust", authorities = "ROLE_CUSTOMER")
    void customerCanOrderAndViewHistoryButNotAdminEndpoints() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        when(service.myOrders("cust")).thenReturn(List.of());
        riceController.createOrder(auth, new RiceDtos.CreateOrderRequest());
        riceController.requestSample(auth, new RiceDtos.CreateSampleRequest());
        assertEquals(List.of(), riceController.myOrders(auth));
        riceController.mySamples(auth);
        verify(service).createOrder(any(), any());
        verify(service).requestSample(any(), any());
        assertAdminDenied();
    }

    @Test
    @WithMockUser(username = "worker", authorities = "ROLE_WORKER")
    void workerCannotPlaceRiceOrdersOrManageThem() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThrows(AccessDeniedException.class, () -> riceController.createOrder(auth, new RiceDtos.CreateOrderRequest()));
        assertThrows(AccessDeniedException.class, () -> riceController.requestSample(auth, new RiceDtos.CreateSampleRequest()));
        assertAdminDenied();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanManageOrdersSamplesAndProduct() {
        when(service.adminOrders(null, null, null, null, "asha")).thenReturn(List.of());
        assertEquals(List.of(), adminController.orders(null, null, null, null, "asha"));
        adminController.samples(null, null, null, null);
        adminController.updateOrder(1L, new RiceDtos.UpdateOrderStatusRequest());
        adminController.updateSample(1L, new RiceDtos.UpdateSampleRequest());
        adminController.product();
        adminController.updateProduct(new RiceDtos.UpdateProductRequest());
        verify(service).updateProduct(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_SUPER_ADMIN")
    void superAdminCanListOrders() {
        adminController.orders(null, null, null, null, null);
        verify(service).adminOrders(null, null, null, null, null);
    }

    private void assertAdminDenied() {
        assertThrows(AccessDeniedException.class, () -> adminController.orders(null, null, null, null, null));
        assertThrows(AccessDeniedException.class, () -> adminController.samples(null, null, null, null));
        assertThrows(AccessDeniedException.class, () -> adminController.updateOrder(1L, new RiceDtos.UpdateOrderStatusRequest()));
        assertThrows(AccessDeniedException.class, () -> adminController.updateSample(1L, new RiceDtos.UpdateSampleRequest()));
        assertThrows(AccessDeniedException.class, () -> adminController.updateProduct(new RiceDtos.UpdateProductRequest()));
        verify(service, never()).adminOrders(any(), any(), any(), any(), any());
        verify(service, never()).updateOrder(any(), any());
        verify(service, never()).updateProduct(any());
    }
}
