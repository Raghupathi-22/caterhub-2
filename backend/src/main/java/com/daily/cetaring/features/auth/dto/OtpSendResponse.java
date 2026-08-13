package com.daily.cetaring.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSendResponse {
    private boolean success;
    private String message;
    private long expiresInSeconds;
}
