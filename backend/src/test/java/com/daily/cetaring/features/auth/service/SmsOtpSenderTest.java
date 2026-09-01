package com.daily.cetaring.features.auth.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsOtpSenderTest {

    private HttpServer server;
    private int statusCode;
    private String responseBody;
    private String capturedPath;
    private String capturedQuery;

    @BeforeEach
    void setUp() throws IOException {
        statusCode = 200;
        responseBody = "{\"Status\":\"Success\",\"Details\":\"REQ123\"}";
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/API/V1/", this::handleRequest);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void constructsOtpEndpointPath() {
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        sender.sendSms("+919876543210", "123456", "LOGIN");

        assertEquals("/API/V1/test-api-key/SMS/9876543210/123456", capturedPath);
    }

    @Test
    void normalizesLocalIndianMobileToTenDigitsInPath() {
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        sender.sendSms("9876543210", "123456", "LOGIN");

        assertEquals("/API/V1/test-api-key/SMS/9876543210/123456", capturedPath);
    }

    @Test
    void placesOtpInPathSegment() {
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        sender.sendSms("+919876543210", "654321", "LOGIN");

        assertTrue(capturedPath.endsWith("/654321"));
    }

    @Test
    void doesNotSendSenderOrTemplateParameters() {
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        sender.sendSms("+919876543210", "123456", "LOGIN");

        assertTrue(capturedQuery == null || capturedQuery.isBlank());
        assertFalse(capturedPath.contains("from="));
        assertFalse(capturedPath.contains("sender"));
        assertFalse(capturedPath.contains("templatename"));
        assertFalse(capturedPath.contains("template"));
    }

    @Test
    void handlesSuccessfulProviderResponse() {
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");
        assertDoesNotThrow(() -> sender.sendSms("+919876543210", "123456", "LOGIN"));
    }

    @Test
    void rejectsProviderErrorEvenWhenHttp200() {
        statusCode = 200;
        responseBody = "{\"Status\":\"Error\",\"Details\":\"Incorrect sender id and templatename provided\"}";
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.sendSms("+919876543210", "123456", "LOGIN")
        );

        assertEquals("SMS provider rejected OTP delivery", exception.getMessage());
    }

    @Test
    void rejectsHttp400Response() {
        statusCode = 400;
        responseBody = "{\"Status\":\"Error\",\"Details\":\"Bad request\"}";
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.sendSms("+919876543210", "123456", "LOGIN")
        );

        assertEquals("SMS provider rejected OTP delivery", exception.getMessage());
    }

    @Test
    void requestAndErrorLogsDoNotLeakApiKeyOtpOrFullMobile() {
        statusCode = 200;
        responseBody = "{\"Status\":\"Error\",\"Message\":\"failed test-api-key 123456 9876543210\",\"Details\":\"REQ999\"}";
        SmsOtpSender sender = sender("test-api-key", "SULTNE", "OTP1");

        Logger logger = (Logger) LoggerFactory.getLogger(SmsOtpSender.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(IllegalStateException.class, () -> sender.sendSms("+919876543210", "123456", "LOGIN"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        String logs = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));

        assertTrue(logs.contains("path=/API/V1/[REDACTED]/SMS/******3210/[OTP_REDACTED]"));
        assertTrue(logs.contains("providerStatus=Error"));
        assertFalse(logs.contains("test-api-key"));
        assertFalse(logs.contains("123456"));
        assertFalse(logs.contains("9876543210"));
    }

    @Test
    void readinessDependsOnApiKey() {
        assertTrue(sender("k", "SULTNE", "OTP1").isDltConfigured());
        assertFalse(sender("", "SULTNE", "OTP1").isDltConfigured());
    }

    private SmsOtpSender sender(String apiKey, String senderId, String otpTemplate) {
        int port = server.getAddress().getPort();
        return new SmsOtpSender(
                "http://localhost:" + port,
                apiKey,
                senderId,
                otpTemplate,
                5
        );
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        capturedPath = exchange.getRequestURI().getPath();
        capturedQuery = exchange.getRequestURI().getRawQuery();
        byte[] payload = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(payload);
        }
    }
}
