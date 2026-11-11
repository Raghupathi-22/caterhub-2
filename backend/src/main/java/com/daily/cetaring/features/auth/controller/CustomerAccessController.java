package com.daily.cetaring.features.auth.controller;

import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.RegisterRequest;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.VerifyRequest;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.VerifyResponse;
import com.daily.cetaring.features.auth.service.CustomerAccessService;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.util.LogMasking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Unified customer sign-in and auto-registration. OTPs are sent via POST /auth/otp/send with purpose CUSTOMER_ACCESS. */
@RestController
@RequestMapping("/auth/otp/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Access", description = "Unified customer OTP sign-in and registration")
public class CustomerAccessController {

    private final CustomerAccessService customerAccessService;

    @PostMapping("/verify")
    @Operation(summary = "Verify customer OTP",
            description = "Signs in an existing customer, or returns a single-use registration ticket for a new number")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request) {
        log.info("Customer access verify for mobile: {}", LogMasking.maskPhone(request.getMobileNumber()));
        return ResponseEntity.ok(customerAccessService.verify(request.getMobileNumber(), request.getOtp()));
    }

    @PostMapping("/register")
    @Operation(summary = "Create customer account",
            description = "Creates a customer account for a verified mobile number using its registration ticket")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Customer access register for mobile: {}", LogMasking.maskPhone(request.getMobileNumber()));
        return ResponseEntity.ok(customerAccessService.register(
                request.getMobileNumber(), request.getRegistrationToken(), request.getFullName()));
    }
}
