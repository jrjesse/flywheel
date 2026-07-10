package com.antigravity.sales.api.dto;

import lombok.Data;

@Data
public class GoogleFormsConfigRequest {
    private String formName;
    private boolean active = true;
    private boolean regenerateToken;
}
