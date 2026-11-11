package com.daily.cetaring.features.service.controller;

import com.daily.cetaring.config.exception.GlobalExceptionHandler;
import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.service.ServiceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestControllerTest {

    private static final String VALID_BODY = """
        {"eventType":"Wedding","eventDate":"2026-11-12","startTime":"18:25","endTime":"21:55",
         "location":"hyf","area":"mani","selectedServices":["1 x Catering Boy"],"totalAmount":850,
         "staffing":[{"workerType":"CATERING_BOY","requiredWorkers":1,"payment":850}]}
        """;

    private ServiceRequestService service;
    private MockMvc mvc;
    private final Authentication customer = new UsernamePasswordAuthenticationToken("customer7", null, List.of());

    @BeforeEach
    void setUp() {
        service = mock(ServiceRequestService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders.standaloneSetup(new ServiceRequestController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
            .build();
    }

    @Test
    void validBookingReturns201AndPassesIdempotencyKey() throws Exception {
        when(service.createCateringStaffBooking(eq("customer7"), any(), eq("abc-123")))
            .thenReturn(ServiceRequestDtos.Response.builder().id(42L).totalAmount(new BigDecimal("850")).build());

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .header("Idempotency-Key", "abc-123")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void oversizedAreaIsRejectedWith400NotServerError() throws Exception {
        String body = VALID_BODY.replace("\"area\":\"mani\"", "\"area\":\"" + "x".repeat(101) + "\"");

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Area")));
        verifyNoInteractions(service);
    }

    @Test
    void missingStaffingIsRejectedWithReadableMessage() throws Exception {
        String body = VALID_BODY.replaceAll(",\\s*\"staffing\":\\[.*]", "");

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Staffing")));
    }

    @Test
    void malformedJsonReturnsApiResponse400() throws Exception {
        String body = VALID_BODY.replace("\"18:25\"", "\"6:25 PM\"");

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void databaseFailureReturns500WithErrorIdAndNoSqlLeak() throws Exception {
        when(service.createCateringStaffBooking(eq("customer7"), any(), any()))
            .thenThrow(new IllegalStateException("could not execute statement [insert into service_requests ... 9876500001]"));

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.data.errorId").exists())
            .andExpect(jsonPath("$.message").value(not(containsString("insert"))))
            .andExpect(jsonPath("$.data.error").value(not(containsString("9876500001"))));
    }

    @Test
    void concurrentDuplicateReturnsTheExistingBooking() throws Exception {
        when(service.createCateringStaffBooking(eq("customer7"), any(), eq("dup-1")))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry for uk_service_requests_client_request"));
        when(service.findExisting("customer7", "dup-1"))
            .thenReturn(Optional.of(ServiceRequestDtos.Response.builder().id(99L).build()));

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .header("Idempotency-Key", "dup-1")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void integrityViolationWithoutKeyReturns409() throws Exception {
        when(service.createCateringStaffBooking(eq("customer7"), any(), any()))
            .thenThrow(new DataIntegrityViolationException("FK violation"));
        when(service.findExisting(any(), any())).thenReturn(Optional.empty());

        mvc.perform(post("/service-requests/catering-staff").principal(customer)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.data.errorId").exists());
    }

    @Test
    void bookingEndpointsRequireCustomerOrAdminRole() throws Exception {
        String expression = ServiceRequestController.class
            .getMethod("createCateringStaffBooking", Authentication.class, String.class, ServiceRequestDtos.CreateCateringStaffRequest.class)
            .getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class).value();
        assertEquals("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN','ROLE_SUPER_ADMIN')", expression);
    }
}
