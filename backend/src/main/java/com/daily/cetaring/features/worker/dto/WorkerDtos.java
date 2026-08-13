package com.daily.cetaring.features.worker.dto;

import com.daily.cetaring.features.worker.entity.JobAssignment;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerAvailability;
import com.daily.cetaring.features.worker.entity.WorkerDocument;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class WorkerDtos {
    private WorkerDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateWorkerProfileRequest {
        @NotNull(message = "User id is required")
        private Long userId;

        @NotNull(message = "Worker type is required")
        private WorkerProfile.WorkerType workerType;

        @NotNull(message = "Experience years is required")
        @Min(value = 0, message = "Experience years cannot be negative")
        private Integer experienceYears;

        private String skills;
        private String preferredAreas;
        private String languages;
        private String bio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMyWorkerProfileRequest {
        @NotNull(message = "Worker type is required")
        private WorkerProfile.WorkerType workerType;

        @NotNull(message = "Experience years is required")
        @Min(value = 0, message = "Experience years cannot be negative")
        private Integer experienceYears;

        private String skills;
        private String preferredAreas;
        private String languages;
        private String bio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateWorkerStatusRequest {
        @NotNull(message = "Status is required")
        private WorkerProfile.WorkerStatus status;

        private Long adminUserId;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpsertAvailabilityRequest {
        @NotNull(message = "Available date is required")
        private LocalDate availableDate;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        private WorkerAvailability.AvailabilityStatus status;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddDocumentRequest {
        @NotNull(message = "Document type is required")
        private WorkerDocument.DocumentType documentType;

        @NotBlank(message = "File name is required")
        private String fileName;

        @NotBlank(message = "File URL is required")
        private String fileUrl;

        private String contentType;

        @Positive(message = "File size must be positive")
        private Long fileSizeBytes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignWorkerRequest {
        @NotNull(message = "Booking id is required")
        private Long bookingId;

        @NotNull(message = "Worker profile id is required")
        private Long workerProfileId;

        @NotNull(message = "Assigned by user id is required")
        private Long assignedByUserId;

        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RespondAssignmentRequest {
        @NotNull(message = "Status is required")
        private JobAssignment.AssignmentStatus status;

        private String declineReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerProfileResponse {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private WorkerProfile.WorkerType workerType;
        private WorkerProfile.WorkerStatus status;
        private Integer experienceYears;
        private String skills;
        private String preferredAreas;
        private String languages;
        private String bio;
        private BigDecimal rating;
        private Integer totalRatings;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityResponse {
        private Long id;
        private Long workerProfileId;
        private LocalDate availableDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private WorkerAvailability.AvailabilityStatus status;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentResponse {
        private Long id;
        private Long workerProfileId;
        private WorkerDocument.DocumentType documentType;
        private String fileName;
        private String fileUrl;
        private String contentType;
        private Long fileSizeBytes;
        private WorkerDocument.DocumentStatus status;
        private LocalDateTime reviewedAt;
        private String rejectionReason;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentResponse {
        private Long id;
        private Long bookingId;
        private Long workerProfileId;
        private Long assignedByUserId;
        private WorkerProfile.WorkerType workerType;
        private JobAssignment.AssignmentStatus status;
        private LocalDateTime offeredAt;
        private LocalDateTime respondedAt;
        private LocalDateTime completedAt;
        private String notes;
        private String declineReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateStaffingRequest {
        @NotBlank(message = "Event type is required")
        private String eventType;

        @NotNull(message = "Worker type is required")
        private WorkerProfile.WorkerType workerType;

        @NotNull(message = "Event date is required")
        private LocalDate eventDate;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        @NotBlank(message = "Location is required")
        private String location;

        @NotBlank(message = "Area is required")
        private String area;

        @NotNull(message = "Required workers is required")
        @Min(value = 1, message = "Required workers must be at least 1")
        private Integer requiredWorkers;

        @NotNull(message = "Payment is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Payment must be greater than 0")
        private BigDecimal payment;

        private String additionalRequirements;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StaffingJobResponse {
        private Long id;
        private String eventType;
        private WorkerProfile.WorkerType workerType;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String area;
        private Integer requiredWorkers;
        private Integer acceptedWorkers;
        private Integer remainingPositions;
        private BigDecimal payment;
        private String additionalRequirements;
        private StaffingRequest.StaffingStatus status;
        private Boolean alreadyAccepted;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerJobResponse {
        private Long acceptanceId;
        private Long jobId;
        private String eventType;
        private WorkerProfile.WorkerType workerType;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String area;
        private BigDecimal payment;
        private StaffingJobAcceptance.AcceptanceStatus status;
        private LocalDateTime acceptedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcceptStaffingJobResponse {
        private StaffingJobResponse job;
        private WorkerJobResponse myJob;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateAvailabilityToggleRequest {
        private Boolean available;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerDashboardResponse {
        private WorkerProfileResponse profile;
        private Integer profileCompletionPercent;
        private Boolean availableForWork;
        private List<StaffingJobResponse> nearbyOpportunities;
        private List<WorkerJobResponse> myJobs;
    }
}
