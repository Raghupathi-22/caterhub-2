package com.daily.cetaring.features.service.service;

import com.daily.cetaring.features.catalog.ServiceCatalog;
import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ServiceRequestService {
    private static final Set<com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType> CATERING_STAFF_ROLES =
        EnumSet.of(
            com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.CATERING_BOY,
            com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.CATERING_GIRL,
            com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.CHEF,
            com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.KITCHEN_HELPER
        );

    private final ServiceRequestRepository repository;
    private final StaffingRequestRepository staffingRequestRepository;
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

    @Transactional
    public ServiceRequestDtos.Response createCateringStaffBooking(String username, ServiceRequestDtos.CreateCateringStaffRequest r) {
        if (!r.endTime.isAfter(r.startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (r.staffing == null || r.staffing.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one catering staff role.");
        }
        List<String> selectedServices = r.selectedServices.stream()
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
        if (selectedServices.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one catering staff role.");
        }
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        Set<com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType> seenRoles = EnumSet.noneOf(com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.class);
        for (ServiceRequestDtos.StaffingLine line : r.staffing) {
            if (line.workerType == null || !CATERING_STAFF_ROLES.contains(line.workerType)) {
                throw new IllegalArgumentException("Only Catering Boy, Catering Girl, Chef and Kitchen Helper can be requested here.");
            }
            if (!seenRoles.add(line.workerType)) {
                throw new IllegalArgumentException("Each catering staff role can be selected only once.");
            }
            calculatedTotal = calculatedTotal.add(unitPrice(line.workerType).multiply(BigDecimal.valueOf(line.requiredWorkers)));
        }

        ServiceRequest saved = repository.save(ServiceRequest.builder().createdBy(user)
            .serviceType("CATERING_STAFF")
            .eventType(r.eventType.trim()).eventDate(r.eventDate)
            .startTime(r.startTime).endTime(r.endTime)
            .location(r.location.trim()).area(r.area.trim())
            .selectedServices(toStorage(selectedServices))
            .instructions(trimToNull(r.instructions))
            .details(trimToNull(r.details))
            .quoteBased(false)
            .totalAmount(calculatedTotal)
            .status(ServiceRequest.Status.PENDING)
            .build());

        for (ServiceRequestDtos.StaffingLine line : r.staffing) {
            staffingRequestRepository.save(StaffingRequest.builder()
                .createdBy(user)
                .eventType(r.eventType.trim())
                .workerType(line.workerType)
                .eventDate(r.eventDate)
                .startTime(r.startTime)
                .endTime(r.endTime)
                .location(r.location.trim())
                .area(r.area.trim())
                .requiredWorkers(line.requiredWorkers)
                .acceptedWorkers(0)
                .payment(unitPrice(line.workerType))
                .additionalRequirements(trimToNull(r.instructions))
                .status(StaffingRequest.StaffingStatus.OPEN)
                .build());
        }
        return map(saved);
    }

    private BigDecimal unitPrice(com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType workerType) {
        return switch (workerType) {
            case CATERING_BOY -> BigDecimal.valueOf(850);
            case CATERING_GIRL -> BigDecimal.valueOf(900);
            case CHEF -> BigDecimal.valueOf(2200);
            case KITCHEN_HELPER -> BigDecimal.valueOf(700);
            default -> throw new IllegalArgumentException("Unsupported catering staff role.");
        };
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
