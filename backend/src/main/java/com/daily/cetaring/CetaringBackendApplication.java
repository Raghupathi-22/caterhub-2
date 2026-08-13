package com.daily.cetaring;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

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

    public static void main(String[] args) {
        SpringApplication.run(CetaringBackendApplication.class, args);
    }

}

