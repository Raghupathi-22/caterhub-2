package com.daily.cetaring.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    private final String baseUrl;
    private final String apiKey;
    private final String templateName;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${twofactor.base-url}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.otp-template}") String templateName,
            @Value("${twofactor.timeout-seconds:10}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(requireConfiguration("TWOFACTOR_BASE_URL", baseUrl));
        this.apiKey = requireConfiguration("TWOFACTOR_API_KEY", apiKey);
        this.templateName = requireConfiguration("TWOFACTOR_OTP_TEMPLATE", templateName);
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String maskedMobile = maskMobile(normalizedMobile);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/API/V1/OTP/SEND"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(normalizedMobile, otp)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || !isSuccessfulProviderResponse(response.body())) {
                log.error("2Factor rejected OTP delivery for mobile {} with status {} and purpose {}",
                        maskedMobile, response.statusCode(), purpose);
                throw new IllegalStateException("SMS provider rejected OTP delivery");
            }
            log.info("2Factor accepted OTP delivery for mobile {} with status {} and purpose {}",
                    maskedMobile, response.statusCode(), purpose);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("2Factor OTP delivery interrupted for mobile {} and purpose {}", maskedMobile, purpose);
            throw new IllegalStateException("SMS provider request was interrupted", exception);
        } catch (IOException | IllegalArgumentException exception) {
            log.error("2Factor OTP delivery failed for mobile {} and purpose {}", maskedMobile, purpose, exception);
            throw new IllegalStateException("SMS provider request failed", exception);
        }
    }

    private String createRequestBody(String mobileNumber, String otp) {
        Map<String, Object> body = Map.of(
                "to", mobileNumber,
                "template_name", templateName,
                "var1", otp
        );
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to prepare SMS provider request", exception);
        }
    }

    private boolean isSuccessfulProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode status = root.path("Status").isMissingNode() ? root.path("status") : root.path("Status");
            if (status.isTextual() && "Success".equalsIgnoreCase(status.asText())) {
                return true;
            }
            JsonNode success = root.path("success");
            return success.isBoolean() && success.asBoolean();
        } catch (JsonProcessingException exception) {
            return false;
        }
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
            throw new IllegalArgumentException("2Factor OTP delivery requires an Indian mobile number");
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
            throw new IllegalStateException(variableName + " must be configured when OTP_SMS_ENABLED=true");
        }
        return value.trim();
    }
}
