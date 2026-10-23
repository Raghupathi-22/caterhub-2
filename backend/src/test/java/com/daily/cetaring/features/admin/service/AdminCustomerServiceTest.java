package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminCustomerDtos;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCustomerServiceTest {

    private UserRepository userRepository;
    private AdminCustomerService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new AdminCustomerService(userRepository);
    }

    @Test
    void returnsActualCustomerNamesAndMobileNumbersWithoutSecrets() {
        LocalDateTime registered = LocalDateTime.of(2026, 3, 1, 10, 0);
        User customer = User.builder()
            .id(5L).username("cust5").phoneNumber("+919876543210").email("c@example.com")
            .passwordHash("secret-hash").firstName("Asha").lastName("Rao")
            .isActive(true).phoneVerifiedAt(registered).createdAt(registered)
            .roles(new HashSet<>()).build();
        stubPage(List.of(customer), PageRequest.of(0, 20), 1);

        AdminCustomerDtos.CustomerPage page = service.listCustomers(null, null, null);

        assertEquals(1, page.getTotalElements());
        AdminCustomerDtos.CustomerSummary summary = page.getContent().get(0);
        assertEquals(5L, summary.getId());
        assertEquals("Asha Rao", summary.getFullName());
        assertEquals("+919876543210", summary.getPhoneNumber());
        assertEquals("c@example.com", summary.getEmail());
        assertEquals(registered, summary.getRegisteredAt());
        assertTrue(summary.getPhoneVerified());
    }

    @Test
    void customerWithoutNameIsNotGivenAFabricatedName() {
        User customer = User.builder().id(6L).username("919000000006").phoneNumber("9000000006")
            .roles(new HashSet<>()).build();
        stubPage(List.of(customer), PageRequest.of(0, 20), 1);

        AdminCustomerDtos.CustomerSummary summary = service.listCustomers(0, 20, "").getContent().get(0);

        assertNull(summary.getFullName());
        assertEquals("9000000006", summary.getPhoneNumber());
        assertFalse(summary.getPhoneVerified());
    }

    @Test
    void emptyResultIsReturnedAsEmptyPage() {
        stubPage(List.of(), PageRequest.of(0, 20), 0);

        AdminCustomerDtos.CustomerPage page = service.listCustomers(0, 20, null);

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getTotalPages());
    }

    @Test
    void repositoryFailurePropagatesInsteadOfReturningAnEmptyList() {
        when(userRepository.findAdminCustomers(anyBoolean(), anyString(), anyBoolean(), anyString(), any()))
            .thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class, () -> service.listCustomers(0, 20, null));
    }

    @Test
    void noSearchDisablesFiltersAndUsesNewestFirstPaging() {
        stubPage(List.of(), PageRequest.of(0, 20), 0);

        service.listCustomers(-3, 0, "   ");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAdminCustomers(eq(false), anyString(), eq(false), anyString(), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(1, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void pageSizeIsCapped() {
        stubPage(List.of(), PageRequest.of(2, 100), 0);

        service.listCustomers(2, 5000, null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAdminCustomers(anyBoolean(), anyString(), anyBoolean(), anyString(), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
        assertEquals(2, pageable.getValue().getPageNumber());
    }

    @Test
    void phoneSearchMatchesOnDigitsAndNameSearchIsCaseInsensitiveAndEscaped() {
        stubPage(List.of(), PageRequest.of(0, 20), 0);

        service.listCustomers(0, 20, "+91 98765-43");

        verify(userRepository).findAdminCustomers(
            eq(true), eq("%+91 98765-43%"), eq(true), eq("%919876543%"), any());

        service.listCustomers(0, 20, "Asha_R%");
        verify(userRepository).findAdminCustomers(
            eq(true), eq("%asha\\_r\\%%"), eq(false), eq("%%"), any());
    }

    @Test
    void summaryDtoHasNoCredentialFields() {
        Set<String> fields = Arrays.stream(AdminCustomerDtos.CustomerSummary.class.getDeclaredFields())
            .map(Field::getName).map(String::toLowerCase).collect(Collectors.toSet());
        for (String forbidden : List.of("password", "passwordhash", "otp", "token", "accesstoken", "refreshtoken", "roles")) {
            assertFalse(fields.contains(forbidden), forbidden);
        }
    }

    private void stubPage(List<User> users, Pageable pageable, long total) {
        Page<User> page = new PageImpl<>(users, pageable, total);
        when(userRepository.findAdminCustomers(anyBoolean(), anyString(), anyBoolean(), anyString(), any()))
            .thenReturn(page);
    }
}
