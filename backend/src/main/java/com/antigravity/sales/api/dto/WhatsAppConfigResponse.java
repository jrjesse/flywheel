package com.antigravity.sales.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WhatsAppConfigResponse {
    private UUID clientId;
    private String phoneNumberId;
    private String wabaId;
    private boolean hasAccessToken;
    private boolean hasAppSecret;
    private boolean hasVerifyToken;
    private Integer debounceSeconds;
    private Integer slaMinutes;
    private Integer maxCapacity;
}
