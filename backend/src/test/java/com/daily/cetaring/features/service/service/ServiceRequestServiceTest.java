package com.daily.cetaring.features.service.service;

import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRequestServiceTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    // 2026-06-01 10:00 IST
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T04:30:00Z"), IST);

    private ServiceRequestRepository repository;
    private StaffingRequestRepository staffingRepository;
    private ServiceRequestService service;
    private User customer;

    @BeforeEach
    void setUp() {
        repository = mock(ServiceRequestRepository.class);
        staffingRepository = mock(StaffingRequestRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        service = new ServiceRequestService(repository, staffingRepository, userRepository);
        service.setClock(CLOCK);
        customer = new User();
        customer.setId(7L);
        customer.setUsername("customer7");
        when(userRepository.findByUsername("customer7")).thenReturn(Optional.of(customer));
        when(repository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest request = invocation.getArgument(0);
            request.setId(101L);
            return request;
        });
    }

    @Test
    void createsCateringStaffBookingWithServerCalculatedTotalAndStaffingJobs() {
        ServiceRequestDtos.CreateCateringStaffRequest request = reviewScreenPayload();
        request.totalAmount = new BigDecimal("1.00"); // client total is never trusted

        ServiceRequestDtos.Response response = service.createCateringStaffBooking("customer7", request, "key-1");

        assertEquals(101L, response.getId());
        assertEquals(0, new BigDecimal("850").compareTo(response.getTotalAmount()));
        assertEquals(LocalDate.of(2026, 11, 12), response.getEventDate());
        assertEquals(LocalTime.of(18, 25), response.getStartTime());
        assertEquals(LocalTime.of(21, 55), response.getEndTime());
        ArgumentCaptor<ServiceRequest> saved = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(repository).save(saved.capture());
        assertEquals("key-1", saved.getValue().getClientRequestId());
        assertEquals(customer, saved.getValue().getCreatedBy());
        assertEquals(ServiceRequest.Status.PENDING, saved.getValue().getStatus());
        ArgumentCaptor<StaffingRequest> staffing = ArgumentCaptor.forClass(StaffingRequest.class);
        verify(staffingRepository).save(staffing.capture());
        assertEquals(101L, staffing.getValue().getServiceRequestId());
        assertEquals(WorkerType.CATERING_BOY, staffing.getValue().getWorkerType());
        assertEquals(1, staffing.getValue().getRequiredWorkers());
        assertEquals(StaffingRequest.StaffingStatus.OPEN, staffing.getValue().getStatus());
    }

    @Test
    void duplicateSubmissionWithSameKeyReturnsExistingBookingWithoutSaving() {
        ServiceRequest existing = ServiceRequest.builder().id(55L).createdBy(customer).clientRequestId("key-1")
            .serviceType("CATERING_STAFF").eventType("Wedding").eventDate(LocalDate.of(2026, 11, 12))
            .startTime(LocalTime.of(18, 25)).endTime(LocalTime.of(21, 55)).location("hyf").area("mani")
            .selectedServices("1 x Catering Boy").quoteBased(false).totalAmount(new BigDecimal("850"))
            .status(ServiceRequest.Status.PENDING).build();
        when(repository.findByCreatedByIdAndClientRequestId(7L, "key-1")).thenReturn(Optional.of(existing));

        ServiceRequestDtos.Response response = service.createCateringStaffBooking("customer7", reviewScreenPayload(), "key-1");

        assertEquals(55L, response.getId());
        verify(repository, never()).save(any());
        verify(staffingRepository, never()).save(any());
    }

    @Test
    void rejectsPastEventDate() {
        ServiceRequestDtos.CreateCateringStaffRequest request = reviewScreenPayload();
        request.eventDate = LocalDate.of(2026, 5, 31);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.createCateringStaffBooking("customer7", request, null));
        assertEquals("Event date and start time must be in the future.", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsTodayWhenStartTimeAlreadyPassedInIndia() {
        ServiceRequestDtos.CreateCateringStaffRequest request = reviewScreenPayload();
        request.eventDate = LocalDate.of(2026, 6, 1);
        request.startTime = LocalTime.of(9, 0);
        request.endTime = LocalTime.of(12, 0);

        assertThrows(IllegalArgumentException.class,
            () -> service.createCateringStaffBooking("customer7", request, null));
    }

    @Test
    void rejectsNullStaffingLineInsteadOfServerError() {
        ServiceRequestDtos.CreateCateringStaffRequest request = reviewScreenPayload();
        List<ServiceRequestDtos.StaffingLine> lines = new ArrayList<>();
        lines.add(null);
        request.staffing = lines;

        assertThrows(IllegalArgumentException.class,
            () -> service.createCateringStaffBooking("customer7", request, null));
    }

    @Test
    void rejectsMalformedIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createCateringStaffBooking("customer7", reviewScreenPayload(), "bad key; drop"));
    }

    @Test
    void genericServiceRequestCreatesStaffingJobsAtomically() {
        ServiceRequestDtos.CreateRequest request = new ServiceRequestDtos.CreateRequest(
            "DECORATION", "Wedding", LocalDate.of(2026, 11, 12), LocalTime.of(18, 0), LocalTime.of(22, 0),
            "hyf", "mani", List.of("Stage decoration"), null, null, true, BigDecimal.ZERO,
            List.of(new ServiceRequestDtos.StaffingLine(WorkerType.EVENT_DECORATOR, 2, new BigDecimal("1200"))));

        ServiceRequestDtos.Response response = service.create("customer7", request, "key-2");

        assertEquals(101L, response.getId());
        ArgumentCaptor<StaffingRequest> staffing = ArgumentCaptor.forClass(StaffingRequest.class);
        verify(staffingRepository, times(1)).save(staffing.capture());
        assertEquals(101L, staffing.getValue().getServiceRequestId());
        assertEquals(0, new BigDecimal("1200").compareTo(staffing.getValue().getPayment()));
    }

    @Test
    void genericServiceRequestWithoutStaffingRemainsBackwardCompatible() {
        ServiceRequestDtos.CreateRequest request = new ServiceRequestDtos.CreateRequest(
            "DECORATION", "Wedding", LocalDate.of(2026, 11, 12), LocalTime.of(18, 0), LocalTime.of(22, 0),
            "hyf", "mani", List.of("Stage decoration"), null, null, true, BigDecimal.ZERO);

        service.create("customer7", request);

        verify(staffingRepository, never()).save(any());
    }

    private static ServiceRequestDtos.CreateCateringStaffRequest reviewScreenPayload() {
        return new ServiceRequestDtos.CreateCateringStaffRequest(
            "Wedding", LocalDate.of(2026, 11, 12), LocalTime.of(18, 25), LocalTime.of(21, 55),
            "hyf", "mani", List.of("1 x Catering Boy"), null, null, new BigDecimal("850"),
            new ArrayList<>(List.of(new ServiceRequestDtos.StaffingLine(WorkerType.CATERING_BOY, 1, new BigDecimal("850")))));
    }
}
