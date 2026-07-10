package com.antigravity.sales.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
    /** Present only in dev profile for testing without email */
    private String resetToken;
}
