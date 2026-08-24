package com.daily.cetaring.features.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FallbackOtpSenderTest {

    @Test
    void callsVoiceWhenDltIsMissing() {
        SmsOtpGateway sms = mock(SmsOtpGateway.class);
        VoiceOtpProvider voice = mock(VoiceOtpProvider.class);
        when(sms.isDltConfigured()).thenReturn(false);
        FallbackOtpSender sender = new FallbackOtpSender(sms, providerOf(voice));

        OtpDeliveryResult result = sender.sendOtp("+919999999999", "123456", "LOGIN");

        assertEquals(OtpDeliveryResult.CHANNEL_VOICE, result.getChannel());
        assertTrue(result.getMessage().toLowerCase().contains("call"));
        verify(sms, never()).sendSms(anyString(), anyString(), anyString());
        verify(voice).sendVoiceOtp("+919999999999", "123456", "LOGIN");
    }

    @Test
    void usesSmsWhenDltConfigured() {
        SmsOtpGateway sms = mock(SmsOtpGateway.class);
        VoiceOtpProvider voice = mock(VoiceOtpProvider.class);
        when(sms.isDltConfigured()).thenReturn(true);
        FallbackOtpSender sender = new FallbackOtpSender(sms, providerOf(voice));

        OtpDeliveryResult result = sender.sendOtp("+919999999999", "123456", "LOGIN");

        assertEquals(OtpDeliveryResult.CHANNEL_SMS, result.getChannel());
        verify(sms).sendSms("+919999999999", "123456", "LOGIN");
        verify(voice, never()).sendVoiceOtp(anyString(), anyString(), anyString());
    }

    @Test
    void fallsBackToVoiceWhenSmsBlocked() {
        SmsOtpGateway sms = mock(SmsOtpGateway.class);
        VoiceOtpProvider voice = mock(VoiceOtpProvider.class);
        when(sms.isDltConfigured()).thenReturn(true);
        doThrow(new IllegalStateException("SMS provider rejected OTP delivery"))
                .when(sms).sendSms(anyString(), anyString(), anyString());
        FallbackOtpSender sender = new FallbackOtpSender(sms, providerOf(voice));

        OtpDeliveryResult result = sender.sendOtp("+919999999999", "123456", "LOGIN");

        assertEquals(OtpDeliveryResult.CHANNEL_VOICE, result.getChannel());
        verify(voice).sendVoiceOtp("+919999999999", "123456", "LOGIN");
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<VoiceOtpProvider> providerOf(VoiceOtpProvider voice) {
        ObjectProvider<VoiceOtpProvider> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(voice);
        return provider;
    }
}
