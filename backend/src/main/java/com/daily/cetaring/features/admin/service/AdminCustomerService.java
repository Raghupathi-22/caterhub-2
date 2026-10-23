package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminCustomerDtos;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {
    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_SEARCH_LENGTH = 100;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminCustomerDtos.CustomerPage listCustomers(Integer page, Integer size, String search) {
        int safePage = page == null ? 0 : Math.max(0, page);
        int safeSize = size == null ? DEFAULT_PAGE_SIZE : Math.min(MAX_PAGE_SIZE, Math.max(1, size));

        String term = search == null ? "" : search.trim();
        if (term.length() > MAX_SEARCH_LENGTH) {
            term = term.substring(0, MAX_SEARCH_LENGTH);
        }
        boolean filtered = !term.isEmpty();
        String namePattern = "%" + escapeLike(term.toLowerCase(Locale.ROOT)) + "%";
        // Phone numbers are stored in a normalized digit form (optionally with +91), so match on digits only.
        String digits = term.replaceAll("\\D", "");
        boolean phoneFiltered = digits.length() >= 3;
        String phonePattern = "%" + digits + "%";

        PageRequest pageRequest = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<User> customers = userRepository.findAdminCustomers(
            filtered, namePattern, phoneFiltered, phonePattern, pageRequest
        );

        return AdminCustomerDtos.CustomerPage.builder()
            .content(customers.getContent().stream().map(AdminCustomerService::toSummary).toList())
            .page(customers.getNumber())
            .size(customers.getSize())
            .totalElements(customers.getTotalElements())
            .totalPages(customers.getTotalPages())
            .build();
    }

    static AdminCustomerDtos.CustomerSummary toSummary(User user) {
        String fullName = String.join(" ",
            user.getFirstName() == null ? "" : user.getFirstName().trim(),
            user.getLastName() == null ? "" : user.getLastName().trim()
        ).trim();
        return AdminCustomerDtos.CustomerSummary.builder()
            .id(user.getId())
            .fullName(fullName.isEmpty() ? null : fullName)
            .phoneNumber(user.getPhoneNumber())
            .email(user.getEmail())
            .active(user.getIsActive())
            .phoneVerified(user.getPhoneVerifiedAt() != null)
            .registeredAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    private static String escapeLike(String value) {
        // Treat user-supplied % and _ literally in LIKE (default escape character is backslash in MySQL).
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
