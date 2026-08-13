package com.daily.cetaring.features.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoices_booking_id", columnList = "booking_id"),
    @Index(name = "idx_invoices_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_invoices_status", columnList = "status"),
    @Index(name = "idx_invoices_invoice_date", columnList = "invoice_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type")
    private InvoiceType invoiceType = InvoiceType.TAX;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "delivery_charge", precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    private String currency = "INR";

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum InvoiceType {
        TAX,           // Tax invoice
        RECEIPT,       // Receipt
        PROFORMA       // Proforma invoice
    }

    public enum InvoiceStatus {
        DRAFT,         // Draft
        ISSUED,        // Issued
        PAID,          // Paid
        OVERDUE,       // Overdue
        CANCELLED      // Cancelled
    }

    public BigDecimal calculateGST() {
        if (taxPercentage != null && subtotal != null) {
            return subtotal.multiply(taxPercentage).divide(new BigDecimal(100));
        }
        return taxAmount != null ? taxAmount : BigDecimal.ZERO;
    }

    public Boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public Boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE;
    }
}

