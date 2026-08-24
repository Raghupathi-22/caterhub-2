package com.daily.cetaring.features.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 2Factor Voice/OBD OTP. Does not use DLT SMS templates.
 * Never logs the OTP or API key.
 */
@Component
@ConditionalOnProperty(name = "otp.voice.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class TwoFactorVoiceOtpProvider implements VoiceOtpProvider {

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TwoFactorVoiceOtpProvider(
            @Value("${twofactor.voice-base-url:${twofactor.base-url:https://2factor.in}}") String baseUrl,
            @Value("${twofactor.api-key:}") String apiKey,
            @Value("${twofactor.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(baseUrl == null || baseUrl.isBlank() ? "https://2factor.in" : baseUrl);
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendVoiceOtp(String mobileNumber, String otp, String purpose) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("TWOFACTOR_API_KEY must be configured for voice OTP");
        }

        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String maskedMobile = maskMobile(normalizedMobile);
        String phoneWithoutCountryCode = normalizedMobile.substring(3);

        try {
            // Voice/OBD does not require a DLT SMS template or sender ID.
            String endpoint = baseUrl + "/API/V1/" + encode(apiKey)
                    + "/VOICE/" + encode(phoneWithoutCountryCode)
                    + "/" + encode(otp);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(timeout)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300
                    || !isSuccessfulProviderResponse(response.body())) {
                log.error(
                        "2Factor VOICE OTP rejected for mobile {} with HTTP status {}, purpose {}, response {}",
                        maskedMobile,
                        response.statusCode(),
                        purpose,
                        sanitizeProviderResponse(response.body())
                );
                throw new IllegalStateException("Voice OTP provider rejected delivery");
            }

            log.info(
                    "2Factor VOICE OTP accepted for mobile {} with HTTP status {} and purpose {}",
                    maskedMobile,
                    response.statusCode(),
                    purpose
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Voice OTP request was interrupted", exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Voice OTP request failed", exception);
        }
    }

    private boolean isSuccessfulProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode status = root.path("Status");
            if (status.isTextual() && "Success".equalsIgnoreCase(status.asText())) {
                return true;
            }
            JsonNode lowerStatus = root.path("status");
            return lowerStatus.isTextual()
                    && ("success".equalsIgnoreCase(lowerStatus.asText())
                    || "sent".equalsIgnoreCase(lowerStatus.asText()));
        } catch (IOException exception) {
            return false;
        }
    }

    private String sanitizeProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "<empty>";
        }
        String sanitized = responseBody.replace(apiKey, "[redacted]");
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String normalizeBaseUrl(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String normalizeIndianMobile(String mobileNumber) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        if (!normalizedMobile.matches("^\\+91\\d{10}$")) {
            throw new IllegalArgumentException("Voice OTP delivery requires an Indian mobile number");
        }
        return normalizedMobile;
    }

    private static String maskMobile(String mobileNumber) {
        if (mobileNumber.length() <= 4) {
            return "****";
        }
        return "******" + mobileNumber.substring(mobileNumber.length() - 4);
    }
}
