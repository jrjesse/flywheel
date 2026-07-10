package com.antigravity.sales.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GoogleFormsConfigResponse {
    private UUID clientId;
    private String formName;
    private boolean active;
    private boolean hasWebhookToken;
}
