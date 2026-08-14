package com.daily.cetaring.features.auth.controller;

import com.daily.cetaring.features.auth.dto.SendOtpRequest;
import com.daily.cetaring.features.auth.dto.OtpSendResponse;
import com.daily.cetaring.features.auth.dto.VerifyOtpRequest;
import com.daily.cetaring.features.auth.service.OtpService;
import com.daily.cetaring.features.auth.service.AuthService;
import com.daily.cetaring.shared.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OTP Authentication", description = "Endpoints for sending and verifying OTPs")
public class OtpController {

    private final OtpService otpService;
    private final AuthService authService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP", description = "Send 6-digit OTP to mobile number")
    public ResponseEntity<OtpSendResponse> sendOtp(@Valid @RequestBody SendOtpRequest request, HttpServletRequest servletRequest) {
        log.info("Send OTP request for mobile: {} purpose: {}", request.getMobileNumber(), request.getPurpose());
        OtpSendResponse response = otpService.generateAndSendOtp(
                request.getMobileNumber(),
                request.getPurpose(),
                extractClientIp(servletRequest)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP and authenticate or register user")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verify OTP request for mobile: {} purpose: {}", request.getMobileNumber(), request.getPurpose());
        boolean isValid = otpService.verifyOtp(request.getMobileNumber(), request.getOtp(), request.getPurpose());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        AuthResponse response = authService.authenticateWithVerifiedOtp(
                request.getMobileNumber(),
                request.getName(),
                request.getPurpose()
        );

        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
