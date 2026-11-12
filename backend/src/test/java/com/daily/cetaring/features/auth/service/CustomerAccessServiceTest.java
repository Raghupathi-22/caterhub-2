package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.CustomerAccessStatus;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.VerifyResponse;
import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerAccessServiceTest {

    private static final String MOBILE = "+919876543210";

    private OtpService otpService;
    private AuthService authService;
    private CustomerAccessService service;
    private final AuthResponse tokens = AuthResponse.builder().accessToken("access").refreshToken("refresh").build();

    @BeforeEach
    void setUp() {
        otpService = mock(OtpService.class);
        authService = mock(AuthService.class);
        service = new CustomerAccessService(otpService, authService);
    }

    private static User user(String role) {
        User user = User.builder().id(5L).phoneNumber(MOBILE).isActive(true).roles(new HashSet<>()).build();
        user.getRoles().add(Role.builder().name(role).build());
        return user;
    }

    @Test
    void existingCustomerIsAuthenticatedAfterOtp() {
        User customer = user("ROLE_CUSTOMER");
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.of(customer));
        when(authService.signInVerifiedCustomer(customer)).thenReturn(tokens);

        VerifyResponse response = service.verify("9876543210", "123456");

        verify(otpService).verifyOtp(MOBILE, "123456", OtpPurpose.CUSTOMER_ACCESS);
        assertEquals(CustomerAccessStatus.AUTHENTICATED, response.getStatus());
        assertSame(tokens, response.getAuth());
        assertNull(response.getRegistrationToken());
        verify(otpService, never()).issueRegistrationTicket(anyString());
    }

    @Test
    void newNumberGetsRegistrationTicketAndNoAccountIsCreated() {
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.empty());
        when(otpService.issueRegistrationTicket(MOBILE)).thenReturn("ticket");
        when(otpService.getRegistrationTicketSeconds()).thenReturn(600L);

        VerifyResponse response = service.verify("+91 98765 43210", "123456");

        assertEquals(CustomerAccessStatus.REGISTRATION_REQUIRED, response.getStatus());
        assertEquals("ticket", response.getRegistrationToken());
        assertEquals(MOBILE, response.getMobileNumber());
        assertNull(response.getAuth());
        verify(authService, never()).createVerifiedUser(anyString(), anyString(), anyString());
        verify(authService, never()).issueTokens(any());
    }

    @Test
    void incorrectOtpNeverIssuesTicketOrTokens() {
        when(otpService.verifyOtp(MOBILE, "000000", OtpPurpose.CUSTOMER_ACCESS))
                .thenThrow(new IllegalArgumentException("Invalid OTP"));

        assertThrows(IllegalArgumentException.class, () -> service.verify(MOBILE, "000000"));

        verify(otpService, never()).issueRegistrationTicket(anyString());
        verify(authService, never()).signInVerifiedCustomer(any());
    }

    @Test
    void workerNumberCannotSignInAsCustomer() {
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.of(user("ROLE_WORKER")));

        assertThrows(IllegalArgumentException.class, () -> service.verify(MOBILE, "123456"));
        verify(authService, never()).signInVerifiedCustomer(any());
    }

    @Test
    void rolelessAccountIsSignedInAsCustomer() {
        User roleless = User.builder().id(5L).phoneNumber(MOBILE).isActive(true).roles(new HashSet<>()).build();
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.of(roleless));
        when(authService.signInVerifiedCustomer(roleless)).thenReturn(tokens);

        VerifyResponse response = service.verify(MOBILE, "123456");

        assertEquals(CustomerAccessStatus.AUTHENTICATED, response.getStatus());
        assertSame(tokens, response.getAuth());
    }

    @Test
    void registerCreatesCustomerOnlyWithRedeemedTicket() {
        User created = user("ROLE_CUSTOMER");
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.empty());
        when(otpService.redeemRegistrationTicket(MOBILE, "ticket")).thenReturn(OtpService.RegistrationTicketState.REDEEMED);
        when(authService.createVerifiedUser(MOBILE, "Ravi Kumar", "ROLE_CUSTOMER")).thenReturn(created);
        when(authService.issueTokens(created)).thenReturn(tokens);

        AuthResponse response = service.register("9876543210", "ticket", "  Ravi   Kumar ");

        assertSame(tokens, response);
        verify(authService).createVerifiedUser(MOBILE, "Ravi Kumar", "ROLE_CUSTOMER");
    }

    @Test
    void registerWithForgedTicketCreatesNothing() {
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.empty());
        when(otpService.redeemRegistrationTicket(MOBILE, "forged"))
                .thenThrow(new IllegalArgumentException(OtpService.REGISTRATION_EXPIRED_MESSAGE));

        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "forged", "Ravi Kumar"));
        verify(authService, never()).createVerifiedUser(anyString(), anyString(), anyString());
    }

    @Test
    void duplicateRegistrationSubmissionSignsInInsteadOfCreatingSecondAccount() {
        User existing = user("ROLE_CUSTOMER");
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.of(existing));
        when(otpService.redeemRegistrationTicket(MOBILE, "ticket")).thenReturn(OtpService.RegistrationTicketState.ALREADY_REDEEMED);
        when(authService.signInVerifiedCustomer(existing)).thenReturn(tokens);

        assertSame(tokens, service.register(MOBILE, "ticket", "Ravi Kumar"));
        verify(authService, never()).createVerifiedUser(anyString(), anyString(), anyString());
    }

    @Test
    void reusedTicketWithoutAccountIsRejected() {
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.empty());
        when(otpService.redeemRegistrationTicket(MOBILE, "ticket")).thenReturn(OtpService.RegistrationTicketState.ALREADY_REDEEMED);

        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "ticket", "Ravi Kumar"));
        verify(authService, never()).createVerifiedUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerDoesNotOverwriteWorkerAccount() {
        when(authService.findUserByMobile(MOBILE)).thenReturn(Optional.of(user("ROLE_WORKER")));

        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "ticket", "Ravi Kumar"));
        verify(otpService, never()).redeemRegistrationTicket(anyString(), anyString());
        verify(authService, never()).createVerifiedUser(anyString(), anyString(), anyString());
    }

    @Test
    void invalidNameIsRejectedBeforeTicketIsConsumed() {
        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "ticket", "R"));
        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "ticket", "Ravi123"));
        assertThrows(IllegalArgumentException.class, () -> service.register(MOBILE, "ticket", "<script>"));
        verify(otpService, never()).redeemRegistrationTicket(anyString(), anyString());
    }

    @Test
    void acceptsIndianScriptNames() {
        assertEquals("రవి కుమార్", CustomerAccessService.normalizeFullName(" రవి  కుమార్ "));
        assertEquals("Anne-Marie D'Souza", CustomerAccessService.normalizeFullName("Anne-Marie D'Souza"));
    }
}
