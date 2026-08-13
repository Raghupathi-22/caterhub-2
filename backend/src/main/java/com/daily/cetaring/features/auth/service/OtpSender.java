package com.daily.cetaring.features.auth.service;

public interface OtpSender {
    void sendOtp(String mobileNumber, String otp, String purpose);
}
