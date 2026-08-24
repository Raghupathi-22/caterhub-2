package com.daily.cetaring.features.event;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "event_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "planned_amount")
    private BigDecimal plannedAmount;

    @Column(name = "booked_amount")
    private BigDecimal bookedAmount;

    @Column(name = "required_flag")
    private boolean requiredFlag = true;
}
