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
 * Sends CaterHub OTPs through 2Factor's SMS OTP API only.
 *
 * IMPORTANT:
 * - This implementation intentionally uses the SMS endpoint.
 * - It never calls the Voice/OBD API.
 * - It does not use the unified "auto" OTP channel, because that can fall
 *   back to Voice.
 *
 * The legacy SMS OTP endpoint is retained here because the deployed account
 * is currently returning HTTP 404 for the newer JSON /OTP/SEND endpoint.
 */
@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    private final String baseUrl;
    private final String apiKey;
    private final String senderId;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${twofactor.base-url}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.sender-id:}") String senderId,
            @Value("${twofactor.timeout-seconds:10}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(requireConfiguration("TWOFACTOR_BASE_URL", baseUrl));
        this.apiKey = requireConfiguration("TWOFACTOR_API_KEY", apiKey);
        this.senderId = senderId == null ? "" : senderId.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String providerMobile = normalizedMobile.substring(1); // 919xxxxxxxxx
        String maskedMobile = maskMobile(normalizedMobile);

        try {
            /*
             * 2Factor SMS OTP endpoint:
             *   /API/V1/{apiKey}/SMS/{mobile}/{otp}
             *
             * The optional sender ID is appended only when configured.
             * There is deliberately no VOICE/OBD endpoint and no "auto"
             * channel here.
             */
            StringBuilder url = new StringBuilder()
                    .append(baseUrl)
                    .append("/API/V1/")
                    .append(urlEncode(apiKey))
                    .append("/SMS/")
                    .append(urlEncode(providerMobile))
                    .append("/")
                    .append(urlEncode(otp));

            if (!senderId.isBlank()) {
                url.append("/").append(urlEncode(senderId));
            }

            HttpRequest request = HttpRequest.newBuilder(URI.create(url.toString()))
                    .timeout(timeout)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300
                    || !isSuccessfulProviderResponse(response.body())) {

                log.error(
                        "2Factor SMS OTP delivery rejected for mobile {} with status {}, purpose {}, response {}",
                        maskedMobile,
                        response.statusCode(),
                        purpose,
                        sanitizeProviderResponse(response.body())
                );

                throw new IllegalStateException("SMS provider rejected OTP delivery");
            }

            log.info(
                    "2Factor SMS OTP accepted for mobile {} with status {} and purpose {}",
                    maskedMobile,
                    response.statusCode(),
                    purpose
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error(
                    "2Factor SMS OTP delivery interrupted for mobile {} and purpose {}",
                    maskedMobile,
                    purpose
            );
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

            JsonNode status = root.path("Status");
            if (status.isMissingNode()) {
                status = root.path("status");
            }

            if (status.isTextual()) {
                String value = status.asText();
                return "Success".equalsIgnoreCase(value)
                        || "sent".equalsIgnoreCase(value)
                        || "success".equalsIgnoreCase(value);
            }

            JsonNode success = root.path("success");
            return success.isBoolean() && success.asBoolean();

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

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
