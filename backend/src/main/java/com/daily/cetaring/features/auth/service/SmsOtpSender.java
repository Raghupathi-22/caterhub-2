package com.daily.cetaring.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements OtpSender {

    private final String baseUrl;
    private final String apiKey;
    private final String senderId;
    private final String templateId;
    private final String entityId;
    private final String otpVariable;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${sms.provider.base-url}") String baseUrl,
            @Value("${sms.provider.api-key}") String apiKey,
            @Value("${sms.provider.sender-id}") String senderId,
            @Value("${sms.provider.template-id}") String templateId,
            @Value("${sms.provider.entity-id:}") String entityId,
            @Value("${sms.provider.otp-variable:OTP}") String otpVariable,
            @Value("${sms.provider.timeout-seconds:10}") long timeoutSeconds
    ) {
        this.baseUrl = requireConfiguration("SMS_PROVIDER_BASE_URL", baseUrl);
        this.apiKey = requireConfiguration("SMS_PROVIDER_API_KEY", apiKey);
        this.senderId = requireConfiguration("SMS_PROVIDER_SENDER_ID", senderId);
        this.templateId = requireConfiguration("SMS_PROVIDER_TEMPLATE_ID", templateId);
        this.entityId = entityId == null ? "" : entityId.trim();
        this.otpVariable = requireConfiguration("SMS_PROVIDER_OTP_VARIABLE", otpVariable);
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String mobileNumber, String otp, String purpose) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("authkey", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(mobileNumber, otp)))
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("MSG91 rejected OTP delivery for mobile {} with status {}", mobileNumber, response.statusCode());
                throw new IllegalStateException("SMS provider rejected OTP delivery");
            }
            log.info("MSG91 accepted OTP delivery for mobile {} and purpose {}", mobileNumber, purpose);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("MSG91 OTP delivery interrupted for mobile {}", mobileNumber);
            throw new IllegalStateException("SMS provider request was interrupted", exception);
        } catch (IOException | IllegalArgumentException exception) {
            log.error("MSG91 OTP delivery failed for mobile {}", mobileNumber, exception);
            throw new IllegalStateException("SMS provider request failed", exception);
        }
    }

    private String createRequestBody(String mobileNumber, String otp) {
        Map<String, Object> recipient = Map.of(
                "mobiles", mobileNumber.substring(1),
                otpVariable, otp
        );
        Map<String, Object> body = Map.of(
                "template_id", templateId,
                "short_url", "0",
                "recipients", List.of(recipient)
        );
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to prepare SMS provider request", exception);
        }
    }

    private static String requireConfiguration(String variableName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variableName + " must be configured when OTP_SMS_ENABLED=true");
        }
        return value.trim();
    }
}
