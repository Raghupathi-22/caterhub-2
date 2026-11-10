package com.daily.cetaring.features.auth.service;

/**
 * Raised when an OTP send is rejected by the resend cooldown or rate limits.
 * Extends IllegalArgumentException so existing callers keep working; the
 * global handler maps it to HTTP 429 with a Retry-After value.
 */
public class OtpRateLimitException extends IllegalArgumentException {

    private final long retryAfterSeconds;

    public OtpRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = Math.max(1L, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
