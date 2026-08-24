package com.daily.cetaring.features.auth.service;

public interface VoiceOtpProvider {
    void sendVoiceOtp(String mobileNumber, String otp, String purpose);
}
