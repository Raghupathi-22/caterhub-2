package com.daily.cetaring.features.rice.controller;

import com.daily.cetaring.features.rice.dto.RiceDtos;
import com.daily.cetaring.features.rice.entity.RiceOrder;
import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import com.daily.cetaring.features.rice.service.RiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/rice")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminRiceController {
    private final RiceService service;

    @GetMapping("/orders")
    public List<RiceDtos.OrderResponse> orders(
        @RequestParam(required = false) RiceOrder.Status status,
        @RequestParam(required = false) RiceOrder.PaymentStatus paymentStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) String search
    ) {
        return service.adminOrders(status, paymentStatus, from, to, search);
    }

    @PatchMapping("/orders/{id}/status")
    public RiceDtos.OrderResponse updateOrder(@PathVariable Long id, @Valid @RequestBody RiceDtos.UpdateOrderStatusRequest request) {
        return service.updateOrder(id, request);
    }

    @GetMapping("/samples")
    public List<RiceDtos.SampleResponse> samples(
        @RequestParam(required = false) RiceSampleRequest.Status status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) String search
    ) {
        return service.adminSamples(status, from, to, search);
    }

    @PatchMapping("/samples/{id}")
    public RiceDtos.SampleResponse updateSample(@PathVariable Long id, @Valid @RequestBody RiceDtos.UpdateSampleRequest request) {
        return service.updateSample(id, request);
    }

    @GetMapping("/product")
    public RiceDtos.ProductResponse product() {
        return service.adminProduct();
    }

    @PutMapping("/product")
    public RiceDtos.ProductResponse updateProduct(@Valid @RequestBody RiceDtos.UpdateProductRequest request) {
        return service.updateProduct(request);
    }
}
