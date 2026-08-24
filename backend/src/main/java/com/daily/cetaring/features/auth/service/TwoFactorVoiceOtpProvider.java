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
 * 2Factor Voice/OBD OTP provider.
 *
 * 2Factor's documented voice endpoint:
 * GET /API/V1/OBD/Send.php?Mode=Say&APIKey=...&PhoneNo=...&Input=...
 *
 * The OTP is generated and stored by CaterHub, then spoken by 2Factor.
 */
@Component
@ConditionalOnProperty(name = "otp.voice.enabled", havingValue = "true")
@Slf4j
public class TwoFactorVoiceOtpProvider implements VoiceOtpProvider {

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient httpClient;

    public TwoFactorVoiceOtpProvider(
            @Value("${twofactor.voice-base-url:https://2factor.in}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(require("TWOFACTOR_VOICE_BASE_URL", baseUrl));
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
    }

    @Override
    public void sendVoiceOtp(String mobileNumber, String otp, String purpose) {
        String normalized = MobileNumberNormalizer.normalize(mobileNumber);
        if (!normalized.matches("^\\+91\\d{10}$")) {
            throw new IllegalArgumentException("Only Indian mobile numbers are supported");
        }

        String phoneNo = normalized.substring(1); // 919XXXXXXXXX
        String input = "Your CaterHub verification code is " + otp
                + ". Please enter this code in the CaterHub app.";

        String url = baseUrl + "/API/V1/OBD/Send.php"
                + "?Mode=Say"
                + "&APIKey=" + encode(apiKey)
                + "&PhoneNo=" + encode(phoneNo)
                + "&Input=" + encode(input);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(timeout)
                    .header("Accept", "application/json,text/plain,*/*")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "2Factor voice API HTTP " + response.statusCode()
                );
            }

            if (!isAccepted(response.body())) {
                throw new IllegalStateException(
                        "2Factor voice API rejected request: " + sanitize(response.body())
                );
            }

            log.info("2Factor voice OTP accepted for mobile {} purpose {}",
                    "******" + normalized.substring(normalized.length() - 4), purpose);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Voice OTP request was interrupted", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Voice OTP request failed", e);
        }
    }

    private boolean isAccepted(String body) {
        if (body == null || body.isBlank()) return false;
        String lower = body.toLowerCase();
        if (lower.contains("error") || lower.contains("failed")) return false;

        try {
            JsonNode root = new ObjectMapper().readTree(body);
            String status = root.path("Status").asText("");
            if ("Success".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) return true;
            status = root.path("status").asText("");
            if ("Success".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) return true;
        } catch (Exception ignored) {
            // Older 2Factor OBD responses can be plain text. Non-error 2xx is accepted.
        }
        return true;
    }

    private String sanitize(String body) {
        if (body == null || body.isBlank()) return "<empty>";
        String safe = body.replace(apiKey, "[redacted]");
        return safe.length() > 1000 ? safe.substring(0, 1000) : safe;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String normalizeBaseUrl(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed;
    }

    private static String require(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured when voice OTP is enabled");
        }
        return value.trim();
    }
}
