package com.daily.cetaring.features.auth.service;

public interface OtpSender {
    OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose);

    default OtpDeliveryResult sendOtp(String mobileNumber, String otp, String purpose, String channel) {
        return sendOtp(mobileNumber, otp, purpose);
    }
}
