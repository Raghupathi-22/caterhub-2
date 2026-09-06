package com.daily.cetaring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private static final List<String> REQUIRED_PRODUCTION_ORIGINS = List.of(
        "https://mycaterhub.in",
        "https://www.mycaterhub.in"
    );

    private List<String> allowedOrigins = List.of(
        "https://caterhub.in",
        "https://www.caterhub.in",
        "https://admin.caterhub.in",
        "https://mycaterhub.in",
        "https://www.mycaterhub.in",
        "https://caterhub-2-production.up.railway.app",
        "https://*.up.railway.app",
        "https://*.vercel.app",
        "http://10.0.2.2",
        "http://10.0.2.2:8080",
        "http://localhost",
        "http://localhost:8080",
        "http://127.0.0.1",
        "http://127.0.0.1:8080",
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173"
    );

    public List<String> getAllowedOrigins() {
        Set<String> merged = new LinkedHashSet<>();
        if (allowedOrigins != null) {
            for (String origin : allowedOrigins) {
                if (origin == null) {
                    continue;
                }
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    merged.add(trimmed);
                }
            }
        }
        merged.addAll(REQUIRED_PRODUCTION_ORIGINS);
        return List.copyOf(merged);
    }
}
