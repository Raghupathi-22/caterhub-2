package com.daily.cetaring.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpRequest {
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;

    private String userType;

    /** AUTO (default), SMS, or VOICE. Voice does not need DLT. */
    private String channel;
}
