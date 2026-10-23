package com.daily.cetaring.features.admin.dto;

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

/** Admin details for a single worker job (staffing request), including every worker who accepted it. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStaffingJobDetailsDTO {
    private Long id;
    private Long serviceRequestId;
    private String serviceCategory;
    private WorkerProfile.WorkerType workerType;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String area;
    private Integer requiredWorkers;
    private Integer acceptedWorkerCount;
    private BigDecimal paymentPerWorker;
    private String additionalRequirements;
    private StaffingRequest.StaffingStatus status;
    private LocalDateTime createdAt;
    /** The account that posted the job (normally the customer). */
    private AdminServiceRequestDetailsDTO.CustomerDTO customer;
    private List<AdminServiceRequestDetailsDTO.AcceptedWorkerDTO> acceptedWorkers;
}
