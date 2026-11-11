package com.daily.cetaring.features.service.controller;

import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.function.Supplier;

@RestController @RequestMapping("/service-requests") @RequiredArgsConstructor
public class ServiceRequestController {
    static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private final ServiceRequestService service;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_WORKER','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ServiceRequestDtos.Response create(
        Authentication auth,
        @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
        @Valid @RequestBody ServiceRequestDtos.CreateRequest request
    ) {
        return withDuplicateGuard(auth, idempotencyKey, () -> service.create(auth.getName(), request, idempotencyKey));
    }

    @PostMapping("/catering-staff")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ServiceRequestDtos.Response createCateringStaffBooking(
        Authentication auth,
        @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
        @Valid @RequestBody ServiceRequestDtos.CreateCateringStaffRequest request
    ) {
        return withDuplicateGuard(auth, idempotencyKey,
            () -> service.createCateringStaffBooking(auth.getName(), request, idempotencyKey));
    }

    /** Two concurrent retries with the same key: the loser hits the unique index and receives the winner's booking. */
    private ServiceRequestDtos.Response withDuplicateGuard(
        Authentication auth, String idempotencyKey, Supplier<ServiceRequestDtos.Response> action
    ) {
        try {
            return action.get();
        } catch (DataIntegrityViolationException ex) {
            return service.findExisting(auth.getName(), idempotencyKey).orElseThrow(() -> ex);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<ServiceRequestDtos.Response> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @GetMapping("/{id}/me")
    @PreAuthorize("isAuthenticated()")
    public ServiceRequestDtos.Response mineById(@PathVariable Long id, Authentication auth) {
        return service.mineById(id, auth.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public List<ServiceRequestDtos.Response> all() { return service.all(); }
}
