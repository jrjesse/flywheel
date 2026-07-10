package com.antigravity.sales.api.dto.user;

import com.antigravity.sales.core.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InviteUserRequest {

    @NotBlank @Email
    private String email;

    @NotBlank @Size(max = 200)
    private String displayName;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private UserRole role;
}
