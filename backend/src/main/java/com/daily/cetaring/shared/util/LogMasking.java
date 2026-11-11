package com.daily.cetaring.shared.util;

/** Helpers that keep personal data such as mobile numbers out of application logs. */
public final class LogMasking {

    private LogMasking() {
    }

    /** Returns only the last four digits of a phone number, e.g. "******3210". */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "******";
        }
        String trimmed = phone.trim();
        if (trimmed.length() <= 4) {
            return "******";
        }
        return "******" + trimmed.substring(trimmed.length() - 4);
    }
}