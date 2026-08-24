package com.daily.cetaring.features.auth.service;

import lombok.Value;

@Value
public class OtpDeliveryResult {
    public static final String CHANNEL_SMS = "SMS";
    public static final String CHANNEL_VOICE = "VOICE";

    String channel;
    String message;

    public static OtpDeliveryResult sms() {
        return new OtpDeliveryResult(CHANNEL_SMS, "OTP sent by SMS");
    }

    public static OtpDeliveryResult voice(String message) {
        return new OtpDeliveryResult(CHANNEL_VOICE, message);
    }
}
