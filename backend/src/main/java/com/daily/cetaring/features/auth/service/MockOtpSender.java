package com.daily.cetaring.features.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class MockOtpSender implements OtpSender {

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        log.info("[MOCK SMS SENDER] Prepared OTP delivery for mobile {} and purpose {}", mobileNumber, purpose);
    }
}
