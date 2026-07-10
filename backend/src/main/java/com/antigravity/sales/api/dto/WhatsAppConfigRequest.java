package com.antigravity.sales.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WhatsAppConfigRequest {
    private String accessToken;
    private String phoneNumberId;
    private String wabaId;
    private String appSecret;
    private String verifyToken;
    private Integer debounceSeconds;
    private Integer slaMinutes;
    private Integer maxCapacity;
}
