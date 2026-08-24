package com.daily.cetaring.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FallbackOtpSender implements OtpSender {

    private final SmsOtpGateway smsOtpGateway;
    private final ObjectProvider<VoiceOtpProvider> voiceOtpProvider;

    @Override
    public OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose) {
        return sendOtp(mobileNumber, otp, purpose, null);
    }

    @Override
    public OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose, String channel) {
        boolean voiceOnly = channel != null && "VOICE".equalsIgnoreCase(channel.trim());
        boolean dltReady = smsOtpGateway.isDltConfigured();

        if (voiceOnly || !dltReady) {
            String reason = voiceOnly
                    ? "Calling you with the OTP."
                    : "SMS DLT is not configured. We are calling you with the OTP.";
            return sendVoice(mobileNumber, otp, purpose, reason);
        }

        try {
            smsOtpGateway.sendSms(mobileNumber, otp, purpose);
            return OtpDeliveryResult.sms();
        } catch (RuntimeException smsFailure) {
            log.warn("SMS OTP blocked or failed ({}). Falling back to voice call.", rootMessage(smsFailure));
            return sendVoice(
                    mobileNumber,
                    otp,
                    purpose,
                    "SMS was blocked. We are calling you with the OTP."
            );
        }
    }

    private OtpDeliveryResult sendVoice(String mobileNumber, String otp, String purpose, String message) {
        VoiceOtpProvider voice = voiceOtpProvider.getIfAvailable();
        if (voice == null) {
            throw new IllegalStateException("Voice OTP is not enabled. Set OTP_VOICE_ENABLED=true.");
        }
        voice.sendVoiceOtp(mobileNumber, otp, purpose);
        return OtpDeliveryResult.voice(message);
    }

    private static String rootMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }
}
