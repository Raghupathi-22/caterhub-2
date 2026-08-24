package com.daily.cetaring.features.auth.service;

public interface SmsOtpGateway {
    boolean isDltConfigured();

    void sendSms(String mobileNumber, String otp, String purpose);
}
