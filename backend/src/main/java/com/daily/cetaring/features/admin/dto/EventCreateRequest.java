package com.daily.cetaring.features.admin.dto;

import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequest {

    @NotNull
    private Long businessId;

    @NotBlank
    private String campaignName;

    @NotBlank
    private String campaignDescription;

    @NotBlank
    private String campaignType;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    private String targetAudience;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal budget;

    private PromotionCampaign.CampaignStatus status;
}
