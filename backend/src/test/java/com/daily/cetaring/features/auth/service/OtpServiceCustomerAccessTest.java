package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.features.auth.dto.OtpSendResponse;
import com.daily.cetaring.features.auth.entity.OtpEntity;
import com.daily.cetaring.features.auth.repository.OtpRepository;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpServiceCustomerAccessTest {

    private static final String MOBILE = MobileNumberNormalizer.normalize("9876543210");
    private static final String PURPOSE = OtpPurpose.CUSTOMER_ACCESS.name();

    private OtpRepository otpRepository;
    private OtpSender otpSender;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpRepository = mock(OtpRepository.class);
        otpSender = mock(OtpSender.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        otpService = new OtpService(otpRepository, otpSender, passwordEncoder, userRepository);
        ReflectionTestUtils.setField(otpService, "expirationSeconds", 300L);
        ReflectionTestUtils.setField(otpService, "resendCooldownSeconds", 60L);
        ReflectionTestUtils.setField(otpService, "mobileRateLimitWindowSeconds", 3600L);
        ReflectionTestUtils.setField(otpService, "mobileRateLimitMaxSends", 5L);
        ReflectionTestUtils.setField(otpService, "ipRateLimitWindowSeconds", 3600L);
        ReflectionTestUtils.setField(otpService, "ipRateLimitMaxSends", 20L);
        ReflectionTestUtils.setField(otpService, "registrationTicketSeconds", 600L);
    }

    private static User userWithRole(String role) {
        User user = User.builder().id(7L).phoneNumber(MOBILE).isActive(true).roles(new HashSet<>()).build();
        user.getRoles().add(Role.builder().name(role).build());
        return user;
    }

    @Test
    void customerAccessSendsOtpToUnregisteredNumberWithoutBlocking() {
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpSender.sendOtp(eq(MOBILE), anyString(), eq(PURPOSE), any())).thenReturn(OtpDeliveryResult.sms());

        OtpSendResponse response = otpService.generateAndSendOtp("9876543210", OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", "1.2.3.4");

        assertTrue(response.isSuccess());
        verify(otpSender).sendOtp(eq(MOBILE), anyString(), eq(PURPOSE), any());
    }

    @Test
    void customerAccessSendsOtpToExistingCustomer() {
        when(userRepository.findByPhoneNumber(MOBILE)).thenReturn(Optional.of(userWithRole("ROLE_CUSTOMER")));
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpSender.sendOtp(eq(MOBILE), anyString(), eq(PURPOSE), any())).thenReturn(OtpDeliveryResult.sms());

        assertTrue(otpService.generateAndSendOtp(MOBILE, OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", null).isSuccess());
    }

    @Test
    void customerAccessSendsOtpToRolelessAccount() {
        User roleless = userWithRole("ROLE_CUSTOMER");
        roleless.getRoles().clear();
        when(userRepository.findByPhoneNumber(MOBILE)).thenReturn(Optional.of(roleless));
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpSender.sendOtp(eq(MOBILE), anyString(), eq(PURPOSE), any())).thenReturn(OtpDeliveryResult.sms());

        assertTrue(otpService.generateAndSendOtp(MOBILE, OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", null).isSuccess());
    }

    @Test
    void customerAccessRejectsWorkerOnlyNumberWithoutSendingOtp() {
        when(userRepository.findByPhoneNumber(MOBILE)).thenReturn(Optional.of(userWithRole("ROLE_WORKER")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> otpService.generateAndSendOtp(MOBILE, OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", null));

        assertEquals(OtpService.NON_CUSTOMER_ACCOUNT_MESSAGE, ex.getMessage());
        verify(otpSender, never()).sendOtp(anyString(), anyString(), anyString(), any());
    }

    @Test
    void customerAccessKeepsResendCooldown() {
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        OtpEntity recent = OtpEntity.builder().createdAt(LocalDateTime.now().minusSeconds(10)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.of(recent));

        assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp(MOBILE, OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", null));
        verify(otpSender, never()).sendOtp(anyString(), anyString(), anyString(), any());
    }

    @Test
    void customerAccessKeepsHourlyRateLimit() {
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpRepository.countByMobileNumberAndPurposeAndCreatedAtAfter(eq(MOBILE), eq(PURPOSE), any())).thenReturn(5L);

        assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp(MOBILE, OtpPurpose.CUSTOMER_ACCESS, "CUSTOMER", null));
    }

    @Test
    void incorrectOtpIncrementsAttemptsAndIsRejected() {
        OtpEntity otp = OtpEntity.builder().mobileNumber(MOBILE).otpHash("hash").attempts(0).maxAttempts(5)
                .expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.of(otp));
        when(passwordEncoder.matches("000000", "hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> otpService.verifyOtp(MOBILE, "000000", OtpPurpose.CUSTOMER_ACCESS));

        assertEquals(1, otp.getAttempts());
        assertFalse(otp.isUsed());
        verify(otpRepository).save(otp);
    }

    @Test
    void expiredOtpIsRejected() {
        OtpEntity otp = OtpEntity.builder().mobileNumber(MOBILE).otpHash("hash").attempts(0).maxAttempts(5)
                .expiresAt(LocalDateTime.now().minusSeconds(1)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.of(otp));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp(MOBILE, "123456", OtpPurpose.CUSTOMER_ACCESS));

        assertEquals("OTP has expired", ex.getMessage());
        assertTrue(otp.isUsed());
    }

    @Test
    void otpLockedAfterMaxAttempts() {
        OtpEntity otp = OtpEntity.builder().mobileNumber(MOBILE).otpHash("hash").attempts(5).maxAttempts(5)
                .expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.of(otp));

        assertThrows(IllegalArgumentException.class, () -> otpService.verifyOtp(MOBILE, "123456", OtpPurpose.CUSTOMER_ACCESS));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void failedVerificationDoesNotRollBackTheAttemptCounter() throws Exception {
        Set<Class<? extends Throwable>> verifyNoRollback = Set.of(OtpService.class
                .getMethod("verifyOtp", String.class, String.class, OtpPurpose.class)
                .getAnnotation(Transactional.class).noRollbackFor());
        Set<Class<? extends Throwable>> ticketNoRollback = Set.of(OtpService.class
                .getMethod("redeemRegistrationTicket", String.class, String.class)
                .getAnnotation(Transactional.class).noRollbackFor());

        assertTrue(verifyNoRollback.contains(IllegalArgumentException.class));
        assertTrue(ticketNoRollback.contains(IllegalArgumentException.class));
    }

    @Test
    void registrationTicketIsRandomHashedAndShortLived() {
        String ticket = otpService.issueRegistrationTicket(MOBILE);

        ArgumentCaptor<OtpEntity> saved = ArgumentCaptor.forClass(OtpEntity.class);
        verify(otpRepository).save(saved.capture());
        assertNotNull(ticket);
        assertTrue(ticket.length() >= 40);
        assertEquals("hash", saved.getValue().getOtpHash());
        assertEquals(OtpService.REGISTRATION_TICKET_PURPOSE, saved.getValue().getPurpose());
        assertTrue(saved.getValue().getExpiresAt().isBefore(LocalDateTime.now().plusSeconds(601)));
        assertFalse(Arrays.stream(OtpPurpose.values()).anyMatch(p -> p.name().equals(OtpService.REGISTRATION_TICKET_PURPOSE)),
                "Clients must not be able to request a registration ticket purpose");
    }

    @Test
    void redeemingValidTicketMarksItUsed() {
        OtpEntity ticket = ticketEntity(false, LocalDateTime.now().plusMinutes(5));
        when(passwordEncoder.matches("ticket", "hash")).thenReturn(true);

        assertEquals(OtpService.RegistrationTicketState.REDEEMED, otpService.redeemRegistrationTicket(MOBILE, "ticket"));
        assertTrue(ticket.isUsed());
    }

    @Test
    void redeemingUsedTicketReportsAlreadyRedeemed() {
        ticketEntity(true, LocalDateTime.now().plusMinutes(5));
        when(passwordEncoder.matches("ticket", "hash")).thenReturn(true);

        assertEquals(OtpService.RegistrationTicketState.ALREADY_REDEEMED, otpService.redeemRegistrationTicket(MOBILE, "ticket"));
    }

    @Test
    void forgedTicketIsRejectedAndCounted() {
        OtpEntity ticket = ticketEntity(false, LocalDateTime.now().plusMinutes(5));
        when(passwordEncoder.matches("forged", "hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> otpService.redeemRegistrationTicket(MOBILE, "forged"));
        assertEquals(1, ticket.getAttempts());
        assertFalse(ticket.isUsed());
    }

    @Test
    void expiredTicketIsRejected() {
        ticketEntity(false, LocalDateTime.now().minusSeconds(1));

        assertThrows(IllegalArgumentException.class, () -> otpService.redeemRegistrationTicket(MOBILE, "ticket"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void missingTicketIsRejected() {
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, OtpService.REGISTRATION_TICKET_PURPOSE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> otpService.redeemRegistrationTicket(MOBILE, "ticket"));
    }

    private OtpEntity ticketEntity(boolean used, LocalDateTime expiresAt) {
        OtpEntity ticket = OtpEntity.builder().mobileNumber(MOBILE).otpHash("hash").purpose(OtpService.REGISTRATION_TICKET_PURPOSE)
                .attempts(0).maxAttempts(5).isUsed(used).expiresAt(expiresAt).build();
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, OtpService.REGISTRATION_TICKET_PURPOSE))
                .thenReturn(Optional.of(ticket));
        return ticket;
    }
}
