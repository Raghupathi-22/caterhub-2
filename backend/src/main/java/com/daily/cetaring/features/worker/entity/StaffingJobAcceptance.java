package com.daily.cetaring.features.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staffing_job_acceptances", uniqueConstraints = {
    @UniqueConstraint(name = "uk_staffing_acceptance_request_worker", columnNames = {"staffing_request_id", "worker_profile_id"})
}, indexes = {
    @Index(name = "idx_staffing_acceptances_worker_status", columnList = "worker_profile_id, status"),
    @Index(name = "idx_staffing_acceptances_request_status", columnList = "staffing_request_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffingJobAcceptance {
    public enum AcceptanceStatus {
        ACCEPTED,
        CANCELLED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffing_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_staffing_acceptance_request"))
    private StaffingRequest staffingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_staffing_acceptance_worker"))
    private WorkerProfile workerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcceptanceStatus status;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        acceptedAt = now;
        updatedAt = now;
        if (status == null) status = AcceptanceStatus.ACCEPTED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

