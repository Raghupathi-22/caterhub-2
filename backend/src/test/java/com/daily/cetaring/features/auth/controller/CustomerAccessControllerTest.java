package com.daily.cetaring.features.auth.controller;

import com.daily.cetaring.config.exception.GlobalExceptionHandler;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.CustomerAccessStatus;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.VerifyResponse;
import com.daily.cetaring.features.auth.service.AuthService;
import com.daily.cetaring.features.auth.service.CustomerAccessService;
import com.daily.cetaring.features.auth.service.OtpService;
import com.daily.cetaring.features.user.service.UserProfileService;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerAccessControllerTest {

    private CustomerAccessService service;
    private OtpService otpService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(CustomerAccessService.class);
        otpService = mock(OtpService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(
                        new CustomerAccessController(service),
                        new OtpController(otpService, mock(AuthService.class), mock(UserProfileService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    void verifyReturnsRegistrationTicketForNewNumber() throws Exception {
        when(service.verify("+919876543210", "123456")).thenReturn(VerifyResponse.builder()
                .status(CustomerAccessStatus.REGISTRATION_REQUIRED).registrationToken("ticket")
                .registrationExpiresInSeconds(600L).mobileNumber("+919876543210").build());

        mvc.perform(post("/auth/otp/customer/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTRATION_REQUIRED"))
                .andExpect(jsonPath("$.registrationToken").value("ticket"))
                .andExpect(jsonPath("$.auth").doesNotExist());
    }

    @Test
    void verifyRejectsMalformedOtpWithoutCallingService() throws Exception {
        mvc.perform(post("/auth/otp/customer/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"otp\":\"12ab\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void invalidOtpReturns400() throws Exception {
        when(service.verify(anyString(), anyString())).thenThrow(new IllegalArgumentException("Invalid OTP"));

        mvc.perform(post("/auth/otp/customer/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"otp\":\"000000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    void registerRequiresTicketAndName() throws Exception {
        mvc.perform(post("/auth/otp/customer/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"fullName\":\"Ravi\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/otp/customer/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"registrationToken\":\"t\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void registerReturnsTokens() throws Exception {
        when(service.register("+919876543210", "ticket", "Ravi Kumar"))
                .thenReturn(AuthResponse.builder().accessToken("access").refreshToken("refresh").build());

        mvc.perform(post("/auth/otp/customer/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"registrationToken\":\"ticket\",\"fullName\":\"Ravi Kumar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access"));
    }

    @Test
    void legacyVerifyCannotBeUsedToConsumeCustomerAccessOtp() throws Exception {
        mvc.perform(post("/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"+919876543210\",\"otp\":\"123456\",\"purpose\":\"CUSTOMER_ACCESS\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(otpService);
    }
}
