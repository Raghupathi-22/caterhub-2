package com.daily.cetaring.config.bootstrap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "caterhub.bootstrap.admin")
public class AdminBootstrapProperties {
    private boolean enabled = true;
    private String email = "admin@caterhub.in";
    private String username = "admin";
    private String password = "";
    private String phoneNumber = "+919999999999";
    private String firstName = "System";
    private String lastName = "Admin";
}

