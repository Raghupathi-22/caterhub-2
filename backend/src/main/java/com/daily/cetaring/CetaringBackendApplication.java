package com.daily.cetaring;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@SpringBootApplication
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Cetaring Catering Booking Platform API",
        version = "1.0.0",
        description = "Production-ready REST API for catering booking platform",
        contact = @Contact(
            name = "Support",
            email = "support@cetaring.com"
        ),
        license = @License(
            name = "Proprietary"
        )
    )
)
public class CetaringBackendApplication {

    /** Must match spring.jpa.properties.hibernate.jdbc.time_zone in application.yml. */
    public static final String BUSINESS_TIME_ZONE = "Asia/Kolkata";

    public static void main(String[] args) {
        configureDefaultTimeZone();
        SpringApplication.run(CetaringBackendApplication.class, args);
    }

    /**
     * Hibernate converts LocalTime/LocalDateTime between the JVM zone and hibernate.jdbc.time_zone.
     * Railway containers default to UTC, which shifted every stored time by +05:30 and made evening
     * events wrap past midnight (violating chk_staffing_time). Pinning the JVM to the JDBC zone
     * makes those conversions an identity.
     */
    static void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(BUSINESS_TIME_ZONE));
    }

}

