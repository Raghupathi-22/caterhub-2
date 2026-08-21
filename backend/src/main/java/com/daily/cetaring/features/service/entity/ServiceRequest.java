package com.daily.cetaring.features.service.entity;

import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "service_requests", indexes = {
    @Index(name = "idx_service_requests_status", columnList = "status"),
    @Index(name = "idx_service_requests_type", columnList = "service_type"),
    @Index(name = "idx_service_requests_date", columnList = "event_date")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceRequest {
    public enum Status { PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "service_type", nullable = false, length = 30)
    private String serviceType;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(nullable = false, length = 255)
    private String location;
    @Column(nullable = false, length = 100)
    private String area;
    @Column(columnDefinition = "TEXT")
    private String details;
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void create() { LocalDateTime now=LocalDateTime.now(); createdAt=now; updatedAt=now; if(status==null) status=Status.PENDING; }
    @PreUpdate void update() { updatedAt=LocalDateTime.now(); }
}
