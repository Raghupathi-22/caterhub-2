package com.daily.cetaring.features.event.entity;

import com.daily.cetaring.shared.entity.Business;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "business_service_offerings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "service_key", nullable = false, length = 80)
    private String serviceKey;

    @Column(name = "min_capacity")
    private Integer minCapacity;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "price_per_unit", precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(length = 30)
    private String unit;

    private String city;

    @Column(name = "veg_supported", nullable = false)
    private boolean vegSupported;

    @Column(name = "non_veg_supported", nullable = false)
    private boolean nonVegSupported;

    @Column(nullable = false)
    private boolean enabled;
}
