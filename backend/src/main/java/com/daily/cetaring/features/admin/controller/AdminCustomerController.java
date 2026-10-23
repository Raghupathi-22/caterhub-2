package com.daily.cetaring.features.admin.controller;

import com.daily.cetaring.features.admin.dto.AdminCustomerDtos;
import com.daily.cetaring.features.admin.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only customer directory for administrators (contact details are admin-only). */
@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    public AdminCustomerDtos.CustomerPage listCustomers(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String search
    ) {
        return adminCustomerService.listCustomers(page, size, search);
    }
}
