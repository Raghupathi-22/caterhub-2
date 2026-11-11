package com.daily.cetaring.features.auth.dto;

public enum OtpPurpose {
    LOGIN,
    REGISTER_CUSTOMER,
    REGISTER_WORKER,
    /** Unified customer sign-in: existing customers log in, new numbers register after verification. */
    CUSTOMER_ACCESS
}
