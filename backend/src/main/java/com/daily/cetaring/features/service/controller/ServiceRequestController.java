package com.daily.cetaring.features.service.controller;

import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController @RequestMapping("/service-requests") @RequiredArgsConstructor
public class ServiceRequestController {
    private final ServiceRequestService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ServiceRequestDtos.Response create(Authentication auth, @Valid @RequestBody ServiceRequestDtos.CreateRequest request) { return service.create(auth.getName(), request); }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<ServiceRequestDtos.Response> mine(Authentication auth) { return service.mine(auth.getName()); }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public List<ServiceRequestDtos.Response> all() { return service.all(); }
}
