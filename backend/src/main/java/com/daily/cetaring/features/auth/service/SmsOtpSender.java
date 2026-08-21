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
 * CaterHub SMS-only OTP sender using 2Factor's current OTP SMS API.
 *
 * This class intentionally:
 *  - requests channel=SMS explicitly;
 *  - never calls the Voice/OBD API;
 *  - never uses an automatic/fallback channel;
 *  - uses the approved 2Factor/DLT template configured in Railway;
 *  - never logs the OTP or API key.
 */
@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    private final String baseUrl;
    private final String apiKey;
    private final String otpTemplate;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${twofactor.base-url}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.otp-template:}") String otpTemplate,
            @Value("${twofactor.timeout-seconds:10}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(requireConfiguration("TWOFACTOR_BASE_URL", baseUrl));
        this.apiKey = requireConfiguration("TWOFACTOR_API_KEY", apiKey);
        this.otpTemplate = requireConfiguration("TWOFACTOR_OTP_TEMPLATE", otpTemplate);
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String maskedMobile = maskMobile(normalizedMobile);

        try {
            // 2Factor current OTP API: SMS channel is explicit.
            // The template controls the approved DLT sender/header and message text.
            String endpoint = baseUrl + "/API/V1/OTP/SEND";

            String json = objectMapper.createObjectNode()
                    .put("to", normalizedMobile)
                    .put("channel", "SMS")
                    .put("template_name", otpTemplate)
                    .put("var1", otp)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(timeout)
                    .header("X-API-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300 && isSuccessfulProviderResponse(response.body())) {
                log.info(
                        "2Factor SMS OTP accepted for mobile {} with status {} and purpose {}",
                        maskedMobile,
                        response.statusCode(),
                        purpose
                );
                return; // success
            }

            // Primary JSON POST failed — attempt legacy path (some 2Factor installations expect API key in URL)
            log.warn("Primary 2Factor JSON API rejected SMS for mobile {} with status {} — trying legacy endpoint. Response: {}",
                    maskedMobile, response.statusCode(), sanitizeProviderResponse(response.body()));

            String legacyEndpoint = baseUrl + "/API/V1/" + apiKey + "/SMS/" + normalizedMobile + "/" + otp;
            if (otpTemplate != null && !otpTemplate.isBlank()) {
                String encodedTemplate = URLEncoder.encode(otpTemplate, StandardCharsets.UTF_8);
                legacyEndpoint += "?template_id=" + encodedTemplate;
            }

            HttpRequest legacyRequest = HttpRequest.newBuilder(URI.create(legacyEndpoint))
                    .timeout(timeout)
                    .GET()
                    .build();

            HttpResponse<String> legacyResponse = httpClient.send(legacyRequest, HttpResponse.BodyHandlers.ofString());

            if (legacyResponse.statusCode() >= 200 && legacyResponse.statusCode() < 300 && isSuccessfulProviderResponse(legacyResponse.body())) {
                log.info("2Factor SMS OTP accepted via legacy endpoint for mobile {} with status {} and purpose {}",
                        maskedMobile, legacyResponse.statusCode(), purpose);
                return; // success via legacy
            }

            // Both attempts failed — log both responses and raise
            log.error(
                    "2Factor SMS OTP rejected for mobile {} with primary status {} and legacy status {}, purpose {}, primary response {}, legacy response {}",
                    maskedMobile,
                    response.statusCode(),
                    legacyResponse.statusCode(),
                    purpose,
                    sanitizeProviderResponse(response.body()),
                    sanitizeProviderResponse(legacyResponse.body())
            );
            throw new IllegalStateException("SMS provider rejected OTP delivery");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS provider request was interrupted", exception);
        } catch (IOException | IllegalArgumentException exception) {
            log.error(
                    "2Factor SMS OTP delivery failed for mobile {} and purpose {}",
                    maskedMobile,
                    purpose,
                    exception
            );
            throw new IllegalStateException("SMS provider request failed", exception);
        }
    }

    private boolean isSuccessfulProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode status = root.path("status");
            if (status.isTextual()) {
                return "sent".equalsIgnoreCase(status.asText())
                        || "success".equalsIgnoreCase(status.asText())
                        || "Success".equalsIgnoreCase(status.asText());
            }

            JsonNode legacyStatus = root.path("Status");
            return legacyStatus.isTextual()
                    && "Success".equalsIgnoreCase(legacyStatus.asText());

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
            throw new IllegalArgumentException(
                    "2Factor SMS OTP delivery requires an Indian mobile number"
            );
        }
        return normalizedMobile;
    }

    private static String maskMobile(String mobileNumber) {
        if (mobileNumber.length() <= 4) {
            return "****";
        }
        return "******" + mobileNumber.substring(mobileNumber.length() - 4);
    }

    private static String requireConfiguration(String variableName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    variableName + " must be configured when OTP_SMS_ENABLED=true"
            );
        }
        return value.trim();
    }
}
