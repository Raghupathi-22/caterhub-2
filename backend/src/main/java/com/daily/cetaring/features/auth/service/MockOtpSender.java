package com.daily.cetaring.features.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class MockOtpSender implements OtpSender {

    @Override
    public OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose) {
        return sendOtp(mobileNumber, otp, purpose, null);
    }

    @Override
    public OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose, String channel) {
        boolean voiceOnly = channel != null && "VOICE".equalsIgnoreCase(channel.trim());
        if (voiceOnly) {
            log.info("[MOCK VOICE SENDER] Prepared OTP call for mobile {} and purpose {}", mobileNumber, purpose);
            return OtpDeliveryResult.voice("Calling you with the OTP.");
        }
        log.info("[MOCK SMS SENDER] Prepared OTP delivery for mobile {} and purpose {}", mobileNumber, purpose);
        return OtpDeliveryResult.sms();
    }
}
