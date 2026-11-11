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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class ServiceRequestService {
    /** Event dates are entered in Indian local time; the server may run in UTC. */
    private static final ZoneId EVENT_ZONE = ZoneId.of("Asia/Kolkata");

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
    private Clock clock = Clock.system(EVENT_ZONE);

    /** Test hook for deterministic "now". */
    @Autowired(required = false)
    void setClock(Clock clock) {
        if (clock != null) this.clock = clock.withZone(EVENT_ZONE);
    }

    /** Returns the booking previously created with this idempotency key, if any. */
    @Transactional(readOnly = true)
    public Optional<ServiceRequestDtos.Response> findExisting(String username, String clientRequestId) {
        String key = normalizeClientRequestId(clientRequestId);
        if (key == null) return Optional.empty();
        return userRepository.findByUsername(username)
            .flatMap(user -> repository.findByCreatedByIdAndClientRequestId(user.getId(), key))
            .map(this::map);
    }

    @Transactional
    public ServiceRequestDtos.Response create(String username, ServiceRequestDtos.CreateRequest r) {
        return create(username, r, null);
    }

    @Transactional
    public ServiceRequestDtos.Response create(String username, ServiceRequestDtos.CreateRequest r, String clientRequestId) {
        String normalizedServiceType = r.serviceType == null ? null : r.serviceType.trim().toUpperCase();
        if (!ServiceCatalog.isSupportedServiceType(normalizedServiceType)) {
            throw new IllegalArgumentException("Unsupported service category.");
        }
        validateSchedule(r.eventDate, r.startTime, r.endTime);
        List<String> selectedServices = cleanSelectedServices(r.selectedServices, "Please select at least one service.");
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        String key = normalizeClientRequestId(clientRequestId);
        Optional<ServiceRequest> existing = findByKey(user, key);
        if (existing.isPresent()) {
            log.info("Duplicate service request submission ignored id={} userId={}", existing.get().getId(), user.getId());
            return map(existing.get());
        }
        List<ServiceRequestDtos.StaffingLine> staffing = r.staffing == null ? List.of() : r.staffing;
        Set<com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType> seenRoles = EnumSet.noneOf(com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.class);
        for (ServiceRequestDtos.StaffingLine line : staffing) {
            if (!seenRoles.add(line.workerType)) {
                throw new IllegalArgumentException("Each service role can be selected only once.");
            }
        }
        ServiceRequest saved = repository.save(ServiceRequest.builder().createdBy(user)
            .clientRequestId(key)
            .serviceType(normalizedServiceType).eventType(r.eventType.trim()).eventDate(r.eventDate)
            .startTime(r.startTime).endTime(r.endTime).location(r.location.trim()).area(r.area.trim())
            .selectedServices(toStorage(selectedServices))
            .instructions(trimToNull(r.instructions))
            .details(trimToNull(r.details))
            .quoteBased(Boolean.TRUE.equals(r.quoteBased))
            .totalAmount(r.totalAmount).status(ServiceRequest.Status.PENDING).build());
        // Worker jobs are created in the same transaction so a failed booking never leaves orphan jobs.
        for (ServiceRequestDtos.StaffingLine line : staffing) {
            saveStaffingLine(user, saved, r.eventType, r.eventDate, r.startTime, r.endTime, r.location, r.area,
                r.instructions, line, line.payment);
        }
        log.info("Service request created id={} userId={} type={} staffingLines={}",
            saved.getId(), user.getId(), normalizedServiceType, staffing.size());
        return map(saved);
    }

    @Transactional
    public ServiceRequestDtos.Response createCateringStaffBooking(String username, ServiceRequestDtos.CreateCateringStaffRequest r) {
        return createCateringStaffBooking(username, r, null);
    }

    @Transactional
    public ServiceRequestDtos.Response createCateringStaffBooking(String username, ServiceRequestDtos.CreateCateringStaffRequest r, String clientRequestId) {
        validateSchedule(r.eventDate, r.startTime, r.endTime);
        if (r.staffing == null || r.staffing.isEmpty() || r.staffing.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Please select at least one catering staff role.");
        }
        List<String> selectedServices = cleanSelectedServices(r.selectedServices, "Please select at least one catering staff role.");
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        String key = normalizeClientRequestId(clientRequestId);
        Optional<ServiceRequest> existing = findByKey(user, key);
        if (existing.isPresent()) {
            log.info("Duplicate catering staff submission ignored id={} userId={}", existing.get().getId(), user.getId());
            return map(existing.get());
        }

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
            .clientRequestId(key)
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
            saveStaffingLine(user, saved, r.eventType, r.eventDate, r.startTime, r.endTime, r.location, r.area,
                r.instructions, line, unitPrice(line.workerType));
        }
        log.info("Catering staff booking created id={} userId={} staffingLines={} total={}",
            saved.getId(), user.getId(), r.staffing.size(), calculatedTotal);
        return map(saved);
    }

    private void saveStaffingLine(User user, ServiceRequest saved, String eventType, LocalDate eventDate,
                                  LocalTime startTime, LocalTime endTime, String location, String area,
                                  String instructions, ServiceRequestDtos.StaffingLine line, BigDecimal payment) {
        staffingRequestRepository.save(StaffingRequest.builder()
            .createdBy(user)
            .serviceRequestId(saved.getId())
            .eventType(eventType.trim())
            .workerType(line.workerType)
            .eventDate(eventDate)
            .startTime(startTime)
            .endTime(endTime)
            .location(location.trim())
            .area(area.trim())
            .requiredWorkers(line.requiredWorkers)
            .acceptedWorkers(0)
            .payment(payment)
            .additionalRequirements(trimToNull(instructions))
            .status(StaffingRequest.StaffingStatus.OPEN)
            .build());
    }

    private void validateSchedule(LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (eventDate.isBefore(now.toLocalDate())
            || (eventDate.isEqual(now.toLocalDate()) && !startTime.isAfter(now.toLocalTime()))) {
            throw new IllegalArgumentException("Event date and start time must be in the future.");
        }
    }

    private List<String> cleanSelectedServices(List<String> services, String emptyMessage) {
        List<String> cleaned = services == null ? List.of() : services.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
        return cleaned;
    }

    private Optional<ServiceRequest> findByKey(User user, String key) {
        return key == null ? Optional.empty() : repository.findByCreatedByIdAndClientRequestId(user.getId(), key);
    }

    static String normalizeClientRequestId(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > 64 || !trimmed.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid Idempotency-Key header.");
        }
        return trimmed;
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
