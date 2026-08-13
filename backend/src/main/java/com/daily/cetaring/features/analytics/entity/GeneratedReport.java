package com.daily.cetaring.features.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "generated_reports", indexes = {
    @Index(name = "idx_generated_reports_business_id", columnList = "business_id"),
    @Index(name = "idx_generated_reports_status", columnList = "status"),
    @Index(name = "idx_generated_reports_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @Column(name = "report_type", length = 100)
    private String reportType;

    @Column(name = "report_data", columnDefinition = "JSON")
    private String reportData;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_type", length = 50)
    private String fileType;  // PDF, EXCEL, CSV

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.READY;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReportStatus {
        GENERATING,  // Being generated
        READY,       // Ready for download
        FAILED,      // Generation failed
        EXPIRED      // Expired
    }

    public Boolean isReady() {
        return status == ReportStatus.READY;
    }

    public Boolean isExpired() {
        return status == ReportStatus.EXPIRED;
    }

    public Boolean canDownload() {
        return status == ReportStatus.READY && fileUrl != null;
    }
}

