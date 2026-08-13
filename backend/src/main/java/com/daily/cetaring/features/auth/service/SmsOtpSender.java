package com.daily.cetaring.features.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        log.warn("SMS OTP sender is enabled but no external provider is configured for mobile {} and purpose {}", mobileNumber, purpose);
    }
}
