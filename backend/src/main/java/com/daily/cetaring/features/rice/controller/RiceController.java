package com.daily.cetaring.features.rice.controller;

import com.daily.cetaring.features.rice.dto.RiceDtos;
import com.daily.cetaring.features.rice.service.RiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rice")
@RequiredArgsConstructor
public class RiceController {
    private final RiceService service;

    /** Public: product details and pricing configuration for the Quality screen. */
    @GetMapping("/product")
    public RiceDtos.ProductResponse product() {
        return service.publicProduct();
    }

    /** Public: server-calculated price for a quantity. */
    @GetMapping("/quote")
    public RiceDtos.QuoteResponse quote(@RequestParam Integer quantityKg) {
        return service.quote(quantityKg);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public RiceDtos.OrderResponse createOrder(Authentication auth, @Valid @RequestBody RiceDtos.CreateOrderRequest request) {
        return service.createOrder(auth.getName(), request);
    }

    @GetMapping("/orders/me")
    @PreAuthorize("isAuthenticated()")
    public List<RiceDtos.OrderResponse> myOrders(Authentication auth) {
        return service.myOrders(auth.getName());
    }

    @GetMapping("/orders/{id}/me")
    @PreAuthorize("isAuthenticated()")
    public RiceDtos.OrderResponse myOrder(@PathVariable Long id, Authentication auth) {
        return service.myOrder(auth.getName(), id);
    }

    @PostMapping("/samples")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public RiceDtos.SampleResponse requestSample(Authentication auth, @Valid @RequestBody RiceDtos.CreateSampleRequest request) {
        return service.requestSample(auth.getName(), request);
    }

    @GetMapping("/samples/me")
    @PreAuthorize("isAuthenticated()")
    public List<RiceDtos.SampleResponse> mySamples(Authentication auth) {
        return service.mySamples(auth.getName());
    }
}
