package com.daily.cetaring.features.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CaterHub SMS OTP sender using 2Factor's OTP SMS API.
 *
 * IMPORTANT:
 * - Uses the OTP SMS API endpoint only.
 * - Does NOT call the Voice/OBD API.
 * - Sender/template are managed in the provider account mapping.
 * - Does not pass sender/template query parameters in API request.
 * - Never logs the OTP or API key.
 */
@Component
@ConditionalOnProperty(name = "otp.sms.enabled", havingValue = "true")
@Slf4j
public class SmsOtpSender implements SmsOtpGateway {

    private final String baseUrl;
    private final String apiKey;
    private final String senderId;
    private final String otpTemplate;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsOtpSender(
            @Value("${twofactor.base-url}") String baseUrl,
            @Value("${twofactor.api-key}") String apiKey,
            @Value("${twofactor.sender-id:}") String senderId,
            @Value("${twofactor.otp-template:}") String otpTemplate,
            @Value("${twofactor.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.baseUrl = normalizeBaseUrl(
                baseUrl == null || baseUrl.isBlank() ? "https://2factor.in" : baseUrl
        );
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.senderId = senderId == null ? "" : senderId.trim();
        this.otpTemplate = otpTemplate == null ? "" : otpTemplate.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    void validateConfigurationAtStartup() {
        List<String> missing = new ArrayList<>();
        boolean baseUrlConfigured = !baseUrl.isBlank();
        boolean apiKeyConfigured = !apiKey.isBlank();
        boolean dltSenderConfigured = !senderId.isBlank();
        boolean dltTemplateConfigured = !otpTemplate.isBlank();

        if (baseUrl.isBlank()) {
            missing.add("twofactor.base-url / TWOFACTOR_BASE_URL");
        }
        if (apiKey.isBlank()) {
            missing.add("twofactor.api-key / TWOFACTOR_API_KEY");
        }

        log.info(
                "2Factor SMS configured: baseUrlConfigured={}, apiKeyConfigured={}, dltSenderConfigured={}, dltTemplateConfigured={}",
                baseUrlConfigured,
                apiKeyConfigured,
                dltSenderConfigured,
                dltTemplateConfigured
        );

        if (missing.isEmpty()) {
            return;
        }

        log.warn(
                "2Factor SMS OTP is not fully configured. Missing {}. SMS OTP may be skipped and voice fallback will be used when available.",
                missing
        );
    }

    @Override
    public boolean isDltConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public void sendSms(String mobileNumber, String otp, String purpose) {
        if (!isDltConfigured()) {
            throw new IllegalStateException("SMS provider API key is not configured");
        }
        if (apiKey.isBlank()) {
            throw new IllegalStateException("TWOFACTOR_API_KEY must be configured for SMS OTP");
        }

        String normalizedMobile = normalizeIndianMobile(mobileNumber);
        String maskedMobile = maskMobile(normalizedMobile);

        try {
            /*
             * 2Factor OTP SMS API:
             *
             * https://2factor.in/API/V1/{apiKey}/SMS/{mobile}/{otp}
             */
            String phoneWithoutCountryCode =
                    normalizedMobile.substring(3); // +91XXXXXXXXXX -> XXXXXXXXXX

            String endpoint = baseUrl
                    + "/API/V1/"
                    + encode(apiKey)
                    + "/SMS/"
                    + encode(phoneWithoutCountryCode)
                    + "/"
                    + encode(otp);
            String redactedRequestPath = "/API/V1/[REDACTED]/SMS/" + maskPhoneWithoutCountryCode(phoneWithoutCountryCode) + "/[OTP_REDACTED]";

            log.info(
                    "2Factor SMS request: path={}, mobile={}, purpose={}",
                    redactedRequestPath,
                    maskedMobile,
                    purpose
            );

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

            ProviderResponseDetails providerResponse = ProviderResponseDetails.from(
                    response.body(),
                    objectMapper,
                    apiKey,
                    otp,
                    phoneWithoutCountryCode
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300
                    || !providerResponse.success()) {

                log.error(
                        "2Factor SMS OTP rejected: httpStatus={}, providerStatus={}, providerCode={}, providerMessage={}, providerRequestId={}, mobile={}, purpose={}, response={}",
                        response.statusCode(),
                        providerResponse.status(),
                        providerResponse.code(),
                        providerResponse.message(),
                        providerResponse.requestId(),
                        maskedMobile,
                        purpose,
                        providerResponse.sanitizedResponse()
                );

                throw new IllegalStateException(
                        "SMS provider rejected OTP delivery"
                );
            }

            log.info(
                    "2Factor SMS OTP accepted: httpStatus={}, providerStatus={}, providerRequestId={}, mobile={}, purpose={}",
                    response.statusCode(),
                    providerResponse.status(),
                    providerResponse.requestId(),
                    maskedMobile,
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
            String safeMessage = sanitizeProviderResponse(
                    exception.getMessage(),
                    apiKey,
                    otp,
                    phoneWithoutCountryCodeFromMobile(normalizedMobile)
            );

            log.error(
                    "2Factor SMS OTP delivery failed for mobile {} and purpose {}: {}",
                    maskedMobile,
                    purpose,
                    safeMessage
            );

            throw new IllegalStateException(
                    "SMS provider request failed",
                    exception
            );
        }
    }

    private static boolean looksBlocked(String responseBody) {
        String lower = responseBody.toLowerCase();
        return lower.contains("dnd")
                || lower.contains("blocked")
                || lower.contains("undeliver")
                || lower.contains("not delivered");
    }

    private static String sanitizeProviderResponse(
            String responseBody,
            String apiKey,
            String otp,
            String phoneWithoutCountryCode
    ) {
        if (responseBody == null || responseBody.isBlank()) {
            return "<empty>";
        }

        String sanitized = responseBody;
        if (apiKey != null && !apiKey.isBlank()) {
            sanitized = sanitized.replace(apiKey, "[redacted]");
        }
        if (otp != null && !otp.isBlank()) {
            sanitized = sanitized.replace(otp, "[redacted]");
        }
        if (phoneWithoutCountryCode != null && !phoneWithoutCountryCode.isBlank()) {
            sanitized = sanitized.replace(phoneWithoutCountryCode, "******" + phoneWithoutCountryCode.substring(Math.max(0, phoneWithoutCountryCode.length() - 4)));
        }

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

    private static String maskPhoneWithoutCountryCode(String phoneWithoutCountryCode) {
        if (phoneWithoutCountryCode == null || phoneWithoutCountryCode.isBlank()) {
            return "******";
        }
        if (phoneWithoutCountryCode.length() <= 4) {
            return "******";
        }
        return "******" + phoneWithoutCountryCode.substring(phoneWithoutCountryCode.length() - 4);
    }

    private static String phoneWithoutCountryCodeFromMobile(String normalizedMobile) {
        if (normalizedMobile != null && normalizedMobile.startsWith("+91") && normalizedMobile.length() > 3) {
            return normalizedMobile.substring(3);
        }
        return normalizedMobile;
    }

    private record ProviderResponseDetails(
            boolean success,
            String status,
            String code,
            String message,
            String requestId,
            String sanitizedResponse
    ) {
        private static ProviderResponseDetails from(
                String responseBody,
                ObjectMapper objectMapper,
                String apiKey,
                String otp,
                String phoneWithoutCountryCode
        ) {
            String sanitized = sanitizeProviderResponse(responseBody, apiKey, otp, phoneWithoutCountryCode);
            if (responseBody == null || responseBody.isBlank()) {
                return new ProviderResponseDetails(false, "empty", null, "<empty>", null, sanitized);
            }

            try {
                JsonNode root = objectMapper.readTree(responseBody);
                String status = text(root, "Status", "status");
                String code = text(root, "Code", "code", "ErrorCode", "errorCode", "error_code");
                String message = firstNonBlank(
                        text(root, "Message", "message", "Error", "error"),
                        text(root, "Details", "details")
                );
                String requestId = firstNonBlank(
                        text(root, "requestId", "request_id", "messageId", "msgid", "id"),
                        text(root, "Details", "details")
                );
                boolean success = isSuccessfulStatus(status) && !looksBlocked(responseBody);
                return new ProviderResponseDetails(success, status, code, message, requestId, sanitized);
            } catch (IOException ignored) {
                String inferredStatus = inferStatusFromText(responseBody);
                boolean success = isSuccessfulStatus(inferredStatus) && !looksBlocked(responseBody);
                return new ProviderResponseDetails(success, inferredStatus, null, sanitized, null, sanitized);
            }
        }

        private static String text(JsonNode node, String... keys) {
            for (String key : keys) {
                JsonNode value = node.path(key);
                if (value.isTextual() && !value.asText().isBlank()) {
                    return value.asText().trim();
                }
                if (value.isNumber()) {
                    return value.asText();
                }
            }
            return null;
        }

        private static String inferStatusFromText(String responseBody) {
            String lower = responseBody.toLowerCase(Locale.ROOT);
            if (lower.contains("success") || lower.contains("sent")) {
                return "success";
            }
            if (lower.contains("error") || lower.contains("fail")) {
                return "error";
            }
            return "unknown";
        }

        private static boolean isSuccessfulStatus(String status) {
            if (status == null || status.isBlank()) {
                return false;
            }
            return "success".equalsIgnoreCase(status) || "sent".equalsIgnoreCase(status);
        }

        private static String firstNonBlank(String primary, String secondary) {
            if (primary != null && !primary.isBlank()) {
                return primary;
            }
            if (secondary != null && !secondary.isBlank()) {
                return secondary;
            }
            return null;
        }
    }
}
