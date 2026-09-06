package com.daily.cetaring.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigurationTest {

    @Test
    void allowedOriginsAlwaysIncludeRequiredProductionDomains() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("https://caterhub.in"));

        List<String> effectiveOrigins = properties.getAllowedOrigins();

        assertTrue(effectiveOrigins.contains("https://mycaterhub.in"));
        assertTrue(effectiveOrigins.contains("https://www.mycaterhub.in"));
        assertTrue(effectiveOrigins.contains("https://caterhub.in"));
    }

    @Test
    void preflightForAdminLoginAllowsConfiguredProductionOrigin() throws Exception {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("https://legacy.example.com"));

        CorsConfigurationSource source = new CorsConfiguration(properties).corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/auth/admin/login");
        request.addHeader("Origin", "https://mycaterhub.in");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "content-type");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(CorsUtils.isPreFlightRequest(request));
        org.springframework.web.cors.CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
        assertNotNull(corsConfiguration);

        boolean allowed = new DefaultCorsProcessor().processRequest(corsConfiguration, request, response);

        assertTrue(allowed);
        assertEquals("https://mycaterhub.in", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertTrue(response.getHeader("Access-Control-Allow-Methods").contains("POST"));
        assertTrue(response.getHeader("Access-Control-Allow-Headers").toLowerCase().contains("content-type"));
    }
}
