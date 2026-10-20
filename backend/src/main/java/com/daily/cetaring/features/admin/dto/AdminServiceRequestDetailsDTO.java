package com.daily.cetaring.features.admin.dto;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminServiceRequestDetailsDTO {
    private Long id;
    private String serviceType;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String area;
    private List<String> selectedServices;
    private String instructions;
    private String details;
    private Boolean quoteBased;
    private BigDecimal totalAmount;
    private ServiceRequest.Status status;
    private LocalDateTime createdAt;
    private CustomerDTO customer;
    private List<StaffingJobDTO> staffingJobs;
    private List<AcceptedWorkerDTO> acceptedWorkers;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CustomerDTO {
        private Long id;
        private String name;
        private String username;
        private String mobileNumber;
        private String email;
        private Boolean verified;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StaffingJobDTO {
        private Long id;
        private WorkerProfile.WorkerType workerType;
        private Integer requiredWorkers;
        private Integer acceptedWorkers;
        private Integer remainingPositions;
        private BigDecimal paymentPerWorker;
        private StaffingRequest.StaffingStatus status;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AcceptedWorkerDTO {
        private Long acceptanceId;
        private Long staffingRequestId;
        private Long workerProfileId;
        private Long userId;
        private String name;
        private String username;
        private String mobileNumber;
        private String email;
        private WorkerProfile.WorkerType workerType;
        private WorkerProfile.WorkerStatus profileStatus;
        private StaffingJobAcceptance.AcceptanceStatus acceptanceStatus;
        private LocalDateTime acceptedAt;
        private Integer experienceYears;
        private String skills;
        private String preferredAreas;
        private String languages;
        private BigDecimal rating;
        private String bio;
    }
}
