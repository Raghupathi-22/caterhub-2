package com.daily.cetaring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of(
        "https://caterhub.in",
        "https://www.caterhub.in",
        "https://admin.caterhub.in",
        "https://caterhub-2-production.up.railway.app",
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
}
