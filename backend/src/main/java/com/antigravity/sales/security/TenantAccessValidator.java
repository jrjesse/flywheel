package com.antigravity.sales.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantAccessValidator {

    public void validateClientAccess(UUID clientId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (!tenantId.equals(clientId)) {
            throw new AccessDeniedException("Access denied to tenant resources");
        }
    }

    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }
}
