package com.daily.cetaring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Read-only admin view of customer accounts. Never carries credentials, OTPs or tokens. */
public final class AdminCustomerDtos {
    private AdminCustomerDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSummary {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private Boolean active;
        private Boolean phoneVerified;
        private LocalDateTime registeredAt;
        private LocalDateTime lastLoginAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerPage {
        private List<CustomerSummary> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
