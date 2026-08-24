package com.daily.cetaring.features.auth.service;

/**
 * Abstraction for voice-call OTP providers.
 * Implementations should place automated calls (or request provider-side voice OTP)
 * and must NOT log OTP values.
 */
public interface VoiceOtpProvider {
    /**
     * Request a voice call delivering the provided OTP to the given Indian mobile number.
     * @param mobileNumber normalized mobile number (e.g. +911234567890)
     * @param otp the plaintext OTP to deliver to the user
     * @param purpose purpose string (e.g. LOGIN, REGISTER)
     */
    void sendVoiceOtp(String mobileNumber, String otp, String purpose);
}
