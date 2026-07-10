package com.antigravity.sales.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(max = 200)
    private String tenantName;

    @NotBlank
    private String document;

    @NotBlank
    private String documentType;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotBlank @Size(max = 200)
    private String displayName;
}
