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
 * CaterHub SMS OTP sender using 2Factor's Transactional SMS API.
 *
 * IMPORTANT:
 * - Uses the SMS/Transactional SMS API only.
 * - Does NOT call the Voice/OBD API.
 * - Does NOT use the old /API/V1/{key}/SMS/... fallback.
 * - Uses the approved DLT template and sender ID configured in Railway.
 * - Never logs the OTP or API key.
 */
@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    private final String baseUrl;
    private final String apiKey;
    private final String otpTemplate;
    private final String senderId;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${twofactor.base-url}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.otp-template:}") String otpTemplate,
            @Value("${twofactor.sender-id:}") String senderId,
            @Value("${twofactor.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(
                requireConfiguration("TWOFACTOR_BASE_URL", baseUrl)
        );
        this.apiKey = requireConfiguration("TWOFACTOR_API_KEY", apiKey);
        this.otpTemplate = requireConfiguration(
                "TWOFACTOR_OTP_TEMPLATE", otpTemplate
        );
        this.senderId = requireConfiguration(
                "TWOFACTOR_SENDER_ID", senderId
        );
        this.timeout = Duration.ofSeconds(timeoutSeconds);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String maskedMobile = maskMobile(normalizedMobile);

        try {
            /*
             * 2Factor Transactional SMS API:
             *
             * https://2factor.in/API/R1/?module=TRANS_SMS
             *   &apikey=API_KEY
             *   &to=10_DIGIT_NUMBER
             *   &from=CATRHB
             *   &templatename=CaterHub%20OTP
             *   &var1=123456
             *
             * We deliberately use this SMS endpoint.
             * There is NO voice endpoint and NO fallback call.
             */
            String phoneWithoutCountryCode =
                    normalizedMobile.substring(3); // +91XXXXXXXXXX -> XXXXXXXXXX

            String endpoint = baseUrl + "/API/R1/?module=TRANS_SMS"
                    + "&apikey=" + encode(apiKey)
                    + "&to=" + encode(phoneWithoutCountryCode)
                    + "&from=" + encode(senderId)
                    + "&templatename=" + encode(otpTemplate)
                    + "&var1=" + encode(otp);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(timeout)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300
                    || !isSuccessfulProviderResponse(response.body())) {

                log.error(
                        "2Factor TRANSACTIONAL SMS OTP rejected for mobile {} with HTTP status {}, purpose {}, response {}",
                        maskedMobile,
                        response.statusCode(),
                        purpose,
                        sanitizeProviderResponse(response.body())
                );

                throw new IllegalStateException(
                        "SMS provider rejected OTP delivery"
                );
            }

            log.info(
                    "2Factor TRANSACTIONAL SMS OTP accepted for mobile {} with HTTP status {} and purpose {}",
                    maskedMobile,
                    response.statusCode(),
                    purpose
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.error(
                    "2Factor SMS OTP request interrupted for mobile {} and purpose {}",
                    maskedMobile,
                    purpose
            );

            throw new IllegalStateException(
                    "SMS provider request was interrupted",
                    exception
            );

        } catch (IOException | IllegalArgumentException exception) {

            log.error(
                    "2Factor SMS OTP delivery failed for mobile {} and purpose {}",
                    maskedMobile,
                    purpose,
                    exception
            );

            throw new IllegalStateException(
                    "SMS provider request failed",
                    exception
            );
        }
    }

    private boolean isSuccessfulProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode status = root.path("Status");
            if (status.isTextual()
                    && "Success".equalsIgnoreCase(status.asText())) {
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

        return sanitized.length() > 500
                ? sanitized.substring(0, 500)
                : sanitized;
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
        String normalizedMobile =
                MobileNumberNormalizer.normalize(mobileNumber);

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

        return "******"
                + mobileNumber.substring(mobileNumber.length() - 4);
    }

    private static String requireConfiguration(
            String variableName,
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    variableName
                            + " must be configured when OTP_SMS_ENABLED=true"
            );
        }

        return value.trim();
    }
}
