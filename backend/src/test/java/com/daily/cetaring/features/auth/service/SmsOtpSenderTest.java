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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsOtpSenderTest {

    private HttpServer server;
    private int statusCode;
    private String responseBody;
    private final AtomicReference<String> capturedPath = new AtomicReference<>();
    private final AtomicReference<String> capturedQuery = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        statusCode = 200;
        responseBody = "{\"Status\":\"Success\",\"Details\":\"REQ123\"}";
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/API/R1/", this::handleRequest);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsConfiguredSenderTemplateAndOtpVariable() {
        SmsOtpSender sender = sender("test-api-key", "OTP1", "", "SULTNE");

        sender.sendSms("+919876543210", "123456", "LOGIN");

        assertEquals("/API/R1/", capturedPath.get());
        Map<String, String> query = parseQuery(capturedQuery.get());
        assertEquals("TRANS_SMS", query.get("module"));
        assertEquals("test-api-key", query.get("apikey"));
        assertEquals("9876543210", query.get("to"));
        assertEquals("SULTNE", query.get("from"));
        assertEquals("OTP1", query.get("templatename"));
        assertEquals("123456", query.get("var1"));
    }

    @Test
    void reportsProviderRejection() {
        statusCode = 200;
        responseBody = "{\"Status\":\"Error\",\"ErrorCode\":\"1701\",\"Message\":\"Template rejected\",\"Details\":\"REQ456\"}";
        SmsOtpSender sender = sender("test-api-key", "OTP1", "", "SULTNE");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.sendSms("+919876543210", "123456", "LOGIN")
        );

        assertEquals("SMS provider rejected OTP delivery", exception.getMessage());
    }

    @Test
    void reportsHttpError() {
        statusCode = 500;
        responseBody = "{\"Status\":\"Error\",\"ErrorCode\":\"500\",\"Message\":\"Internal error\",\"Details\":\"REQ789\"}";
        SmsOtpSender sender = sender("test-api-key", "OTP1", "", "SULTNE");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.sendSms("+919876543210", "123456", "LOGIN")
        );

        assertEquals("SMS provider rejected OTP delivery", exception.getMessage());
    }

    @Test
    void validatesDltConfigurationInputs() {
        assertTrue(sender("k", "OTP1", "", "SULTNE").isDltConfigured());
        assertTrue(sender("k", "", "1707162736154333359", "SULTNE").isDltConfigured());
        assertFalse(sender("", "OTP1", "", "SULTNE").isDltConfigured());
        assertFalse(sender("k", "", "", "SULTNE").isDltConfigured());
        assertFalse(sender("k", "OTP1", "", "").isDltConfigured());
    }

    @Test
    void logsProviderFailureWithoutExposingSecrets() {
        statusCode = 200;
        responseBody = "{\"Status\":\"Error\",\"ErrorCode\":\"1702\",\"Message\":\"failed test-api-key 123456 9876543210\",\"Details\":\"REQ999\"}";
        SmsOtpSender sender = sender("test-api-key", "OTP1", "", "SULTNE");

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

        assertTrue(logs.contains("httpStatus=200"));
        assertTrue(logs.contains("providerCode=1702"));
        assertTrue(logs.contains("providerRequestId=REQ999"));
        assertFalse(logs.contains("test-api-key"));
        assertFalse(logs.contains("123456"));
        assertFalse(logs.contains("9876543210"));
    }

    @Test
    void prefersTemplateIdOverTemplateNameWhenBothConfigured() {
        SmsOtpSender sender = sender("test-api-key", "OTP1", "1707162736154333359", "SULTNE");

        sender.sendSms("+919876543210", "123456", "LOGIN");

        Map<String, String> query = parseQuery(capturedQuery.get());
        assertEquals("1707162736154333359", query.get("templatename"));
    }

    private SmsOtpSender sender(String apiKey, String templateName, String templateId, String senderId) {
        int port = server.getAddress().getPort();
        return new SmsOtpSender("http://localhost:" + port, apiKey, templateName, templateId, senderId, 5);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        capturedPath.set(exchange.getRequestURI().getPath());
        capturedQuery.set(exchange.getRequestURI().getRawQuery());
        byte[] payload = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(payload);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return query;
        }
        List<String> parts = Arrays.asList(rawQuery.split("&"));
        for (String part : parts) {
            String[] token = part.split("=", 2);
            String key = URLDecoder.decode(token[0], StandardCharsets.UTF_8);
            String value = token.length > 1 ? URLDecoder.decode(token[1], StandardCharsets.UTF_8) : "";
            query.put(key, value);
        }
        return query;
    }
}
