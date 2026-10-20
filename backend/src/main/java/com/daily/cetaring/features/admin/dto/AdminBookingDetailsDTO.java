package com.daily.cetaring.features.admin.dto;

import com.daily.cetaring.features.worker.entity.JobAssignment;
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
public class AdminBookingDetailsDTO {
    private Long id;
    private String bookingReference;
    private String eventType;
    private Integer guestCount;
    private String mealType;
    private LocalDate eventDate;
    private LocalDateTime eventDateTime;
    private String deliveryAddress;
    private String specialInstructions;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private CustomerDTO customer;
    private List<AcceptedWorkerDTO> acceptedWorkers;
    private List<WorkerAssignmentDTO> workerAssignments;

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
    public static class AcceptedWorkerDTO {
        private Long assignmentId;
        private Long workerProfileId;
        private Long userId;
        private String name;
        private String username;
        private String mobileNumber;
        private String email;
        private WorkerProfile.WorkerType workerType;
        private WorkerProfile.WorkerStatus profileStatus;
        private JobAssignment.AssignmentStatus assignmentStatus;
        private LocalDateTime acceptedAt;
        private Integer experienceYears;
        private String skills;
        private String preferredAreas;
        private String languages;
        private BigDecimal rating;
        private String bio;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WorkerAssignmentDTO {
        private Long assignmentId;
        private Long workerProfileId;
        private String name;
        private String mobileNumber;
        private WorkerProfile.WorkerType workerType;
        private JobAssignment.AssignmentStatus status;
        private LocalDateTime offeredAt;
        private LocalDateTime respondedAt;
        private String declineReason;
    }
}
