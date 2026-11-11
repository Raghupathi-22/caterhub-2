package com.daily.cetaring.features.auth.dto;

import com.daily.cetaring.shared.dto.AuthResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTOs for the unified customer sign-in / auto-registration flow (OTP purpose CUSTOMER_ACCESS). */
public final class CustomerAccessDtos {

    private CustomerAccessDtos() {
    }

    public enum CustomerAccessStatus { AUTHENTICATED, REGISTRATION_REQUIRED }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyRequest {
        @NotBlank(message = "Mobile number is required")
        @Size(max = 20, message = "Invalid mobile number")
        private String mobileNumber;

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{6}", message = "Please enter the 6-digit OTP")
        private String otp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VerifyResponse {
        private CustomerAccessStatus status;
        /** Present when status is AUTHENTICATED. */
        private AuthResponse auth;
        /** Single-use ticket proving server-side OTP verification; present when REGISTRATION_REQUIRED. */
        private String registrationToken;
        private Long registrationExpiresInSeconds;
        /** Normalized number that was verified, for read-only display. */
        private String mobileNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Mobile number is required")
        @Size(max = 20, message = "Invalid mobile number")
        private String mobileNumber;

        @NotBlank(message = "Verification has expired. Please verify your mobile number again.")
        @Size(max = 128, message = "Verification has expired. Please verify your mobile number again.")
        private String registrationToken;

        @NotBlank(message = "Please enter your full name")
        @Size(min = 2, max = 80, message = "Full name must be between 2 and 80 characters")
        private String fullName;
    }
}
