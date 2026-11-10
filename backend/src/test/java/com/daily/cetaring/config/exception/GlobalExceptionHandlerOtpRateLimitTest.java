package com.daily.cetaring.config.exception;

import com.daily.cetaring.features.auth.service.OtpRateLimitException;
import com.daily.cetaring.shared.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerOtpRateLimitTest {

    @Test
    void otpRateLimitMapsTo429WithRetryAfter() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/v1/auth/otp/send");
        ResponseEntity<ApiResponse<Map<String, Object>>> response = new GlobalExceptionHandler()
                .handleOtpRateLimitException(
                        new OtpRateLimitException("Please wait before requesting another OTP", 42),
                        new ServletWebRequest(servletRequest));

        assertEquals(429, response.getStatusCode().value());
        assertEquals("42", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertEquals(429, response.getBody().getCode());
        assertEquals("Please wait before requesting another OTP", response.getBody().getMessage());
        assertEquals(42L, response.getBody().getData().get("retryAfterSeconds"));
        assertEquals("Please wait before requesting another OTP", response.getBody().getData().get("error"));
    }
}
