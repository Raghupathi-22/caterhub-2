package com.daily.cetaring.features.service.service;

import com.daily.cetaring.features.catalog.ServiceCatalog;
import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public ServiceRequestDtos.Response create(String username, ServiceRequestDtos.CreateRequest r) {
        String normalizedServiceType = r.serviceType == null ? null : r.serviceType.trim().toUpperCase();
        if (!ServiceCatalog.isSupportedServiceType(normalizedServiceType)) {
            throw new IllegalArgumentException("Unsupported service category.");
        }
        if (!r.endTime.isAfter(r.startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        List<String> selectedServices = r.selectedServices.stream()
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
        if (selectedServices.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one service.");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        ServiceRequest saved = repository.save(ServiceRequest.builder().createdBy(user)
            .serviceType(normalizedServiceType).eventType(r.eventType.trim()).eventDate(r.eventDate)
            .startTime(r.startTime).endTime(r.endTime).location(r.location.trim()).area(r.area.trim())
            .selectedServices(toStorage(selectedServices))
            .instructions(trimToNull(r.instructions))
            .details(trimToNull(r.details))
            .quoteBased(Boolean.TRUE.equals(r.quoteBased))
            .totalAmount(r.totalAmount).status(ServiceRequest.Status.PENDING).build());
        return map(saved);
    }

    @Transactional(readOnly=true) public List<ServiceRequestDtos.Response> all() { return repository.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList(); }

    @Transactional(readOnly = true)
    public List<ServiceRequestDtos.Response> mine(String username) {
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
            .getId();
        return repository.findByCreatedByIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDtos.Response mineById(Long id, String username) {
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
            .getId();
        ServiceRequest request = repository.findByIdAndCreatedById(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        return map(request);
    }

    private ServiceRequestDtos.Response map(ServiceRequest s) {
        return ServiceRequestDtos.Response.builder()
            .id(s.getId())
            .serviceType(s.getServiceType())
            .eventType(s.getEventType())
            .eventDate(s.getEventDate())
            .startTime(s.getStartTime())
            .endTime(s.getEndTime())
            .location(s.getLocation())
            .area(s.getArea())
            .selectedServices(fromStorage(s.getSelectedServices()))
            .instructions(s.getInstructions())
            .details(s.getDetails())
            .quoteBased(Boolean.TRUE.equals(s.getQuoteBased()))
            .totalAmount(s.getTotalAmount())
            .status(s.getStatus())
            .createdAt(s.getCreatedAt())
            .updatedAt(s.getUpdatedAt())
            .build();
    }

    private String toStorage(List<String> services) {
        return services.stream().map(String::trim).filter(value -> !value.isBlank()).collect(Collectors.joining("\n"));
    }

    private List<String> fromStorage(String selectedServices) {
        if (selectedServices == null || selectedServices.isBlank()) return List.of();
        return Arrays.stream(selectedServices.split("\\R"))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
