package com.antigravity.sales.api.dto.user;

import com.antigravity.sales.core.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotNull
    private UserRole role;
}
