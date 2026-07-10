package com.antigravity.sales.api.dto.user;

import com.antigravity.sales.core.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String displayName;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
}
