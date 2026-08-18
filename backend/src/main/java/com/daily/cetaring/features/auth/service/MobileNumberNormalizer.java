package com.daily.cetaring.features.auth.service;

public final class MobileNumberNormalizer {

    private MobileNumberNormalizer() {
    }

    public static String normalize(String mobileNumber) {
        if (mobileNumber == null) {
            throw new IllegalArgumentException("Mobile number cannot be null");
        }

        String cleaned = mobileNumber.trim().replaceAll("[\\s-()]", "");
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Mobile number cannot be blank");
        }

        if (!cleaned.startsWith("+")) {
            if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned;
            } else if (cleaned.startsWith("91") && cleaned.length() == 12) {
                cleaned = "+" + cleaned;
            }
        }

        if (!cleaned.matches("^\\+[1-9]\\d{9,14}$")) {
            throw new IllegalArgumentException("Invalid mobile number");
        }

        return cleaned;
    }
}
