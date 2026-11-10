package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.features.auth.dto.OtpSendResponse;
import com.daily.cetaring.features.auth.entity.OtpEntity;
import com.daily.cetaring.features.auth.repository.OtpRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpServiceRateLimitTest {

    private static final String MOBILE = MobileNumberNormalizer.normalize("9876543210");
    private static final String PURPOSE = OtpPurpose.REGISTER_CUSTOMER.name();

    private OtpRepository otpRepository;
    private OtpSender otpSender;
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpRepository = mock(OtpRepository.class);
        otpSender = mock(OtpSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        otpService = new OtpService(otpRepository, otpSender, passwordEncoder, mock(UserRepository.class));
        ReflectionTestUtils.setField(otpService, "expirationSeconds", 300L);
        ReflectionTestUtils.setField(otpService, "resendCooldownSeconds", 60L);
        ReflectionTestUtils.setField(otpService, "mobileRateLimitWindowSeconds", 3600L);
        ReflectionTestUtils.setField(otpService, "mobileRateLimitMaxSends", 5L);
        ReflectionTestUtils.setField(otpService, "ipRateLimitWindowSeconds", 3600L);
        ReflectionTestUtils.setField(otpService, "ipRateLimitMaxSends", 20L);
    }

    @Test
    void successfulSendReturnsBackendResendCooldown() {
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpSender.sendOtp(eq(MOBILE), anyString(), eq(PURPOSE), any())).thenReturn(OtpDeliveryResult.sms());

        OtpSendResponse response = otpService.generateAndSendOtp(MOBILE, OtpPurpose.REGISTER_CUSTOMER, "CUSTOMER", "1.2.3.4");

        assertEquals(60L, response.getResendCooldownSeconds());
        assertEquals(300L, response.getExpiresInSeconds());
    }

    @Test
    void resendWithinCooldownIsRejectedWithRemainingSecondsAndNoSms() {
        OtpEntity recent = OtpEntity.builder().createdAt(LocalDateTime.now().minusSeconds(20)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.of(recent));

        OtpRateLimitException ex = assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp(MOBILE, OtpPurpose.REGISTER_CUSTOMER, "CUSTOMER", "1.2.3.4"));

        assertEquals("Please wait before requesting another OTP", ex.getMessage());
        assertTrue(ex.getRetryAfterSeconds() >= 38 && ex.getRetryAfterSeconds() <= 40, "retryAfter=" + ex.getRetryAfterSeconds());
        verify(otpSender, never()).sendOtp(anyString(), anyString(), anyString(), any());
        verify(otpRepository, never()).save(any());
    }

    @Test
    void mobileHourlyLimitReportsTimeUntilOldestAttemptLeavesWindow() {
        when(otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(MOBILE, PURPOSE)).thenReturn(Optional.empty());
        when(otpRepository.countByMobileNumberAndPurposeAndCreatedAtAfter(eq(MOBILE), eq(PURPOSE), any())).thenReturn(5L);
        OtpEntity oldest = OtpEntity.builder().createdAt(LocalDateTime.now().minusSeconds(3000)).build();
        when(otpRepository.findTopByMobileNumberAndPurposeAndCreatedAtAfterOrderByCreatedAtAsc(eq(MOBILE), eq(PURPOSE), any()))
                .thenReturn(Optional.of(oldest));

        OtpRateLimitException ex = assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp(MOBILE, OtpPurpose.REGISTER_CUSTOMER, "CUSTOMER", "1.2.3.4"));

        assertTrue(ex.getRetryAfterSeconds() >= 598 && ex.getRetryAfterSeconds() <= 600, "retryAfter=" + ex.getRetryAfterSeconds());
        verify(otpSender, never()).sendOtp(anyString(), anyString(), anyString(), any());
    }

    @Test
    void rateLimitExceptionRemainsAnIllegalArgumentExceptionForExistingCallers() {
        assertTrue(new OtpRateLimitException("x", 0) instanceof IllegalArgumentException);
        assertEquals(1L, new OtpRateLimitException("x", 0).getRetryAfterSeconds());
    }
}
