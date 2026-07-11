package com.antigravity.sales.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID userId;
    private UUID tenantId;
    private String email;
    private String displayName;
    private String role;
    private String webhookSecret;
}
