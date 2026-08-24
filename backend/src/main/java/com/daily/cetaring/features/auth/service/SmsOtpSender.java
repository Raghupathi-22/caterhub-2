package com.daily.cetaring.features.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 2Factor OTP sender.
 *
 * Uses 2Factor's OTP endpoint rather than the generic transactional SMS endpoint:
 * POST https://2factor.in/API/V1/OTP/SEND
 *
 * The API key is kept on the backend only.
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
            @Value("${twofactor.timeout-seconds:30}") long timeoutSeconds
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
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        if (!normalizedMobile.matches("^\\+91\\d{10}$")) {
            throw new IllegalArgumentException("Only Indian mobile numbers are supported");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", normalizedMobile);
            payload.put("template_name", otpTemplate);
            payload.put("var1", otp);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(baseUrl + "/API/V1/OTP/SEND"))
                    .timeout(timeout)
                    .header("X-API-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String body = sanitizeProviderResponse(response.body());

            if (response.statusCode() >= 200
                    && response.statusCode() < 300
                    && isSuccessfulProviderResponse(response.body())) {
                log.info(
                        "2Factor SMS OTP accepted for mobile {} purpose {}",
                        maskMobile(normalizedMobile), purpose
                );
                return;
            }

            log.error(
                    "2Factor SMS OTP rejected. HTTP {} purpose {} response {}",
                    response.statusCode(), purpose, body
            );
            throw new IllegalStateException("2Factor rejected SMS OTP: " + body);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS provider request was interrupted", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("SMS provider request failed", e);
        }
    }

    private boolean isSuccessfulProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return false;
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText("");
            if ("sent".equalsIgnoreCase(status)
                    || "success".equalsIgnoreCase(status)
                    || "delivered".equalsIgnoreCase(status)) {
                return true;
            }
            String legacy = root.path("Status").asText("");
            return "Success".equalsIgnoreCase(legacy)
                    || "sent".equalsIgnoreCase(legacy);
        } catch (IOException e) {
            return false;
        }
    }

    private String sanitizeProviderResponse(String body) {
        if (body == null || body.isBlank()) return "<empty>";
        String safe = body.replace(apiKey, "[redacted]");
        return safe.length() > 1000 ? safe.substring(0, 1000) : safe;
    }

    private static String normalizeBaseUrl(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed;
    }

    private static String requireConfiguration(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured when OTP SMS is enabled");
        }
        return value.trim();
    }

    private static String maskMobile(String mobile) {
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
