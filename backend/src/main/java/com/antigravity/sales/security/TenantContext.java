package com.antigravity.sales.security;

import com.antigravity.sales.core.model.UserRole;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> DISPLAY_NAME = new ThreadLocal<>();
    private static final ThreadLocal<Set<UserRole>> ROLES = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId, UUID userId, String email, String displayName, Set<UserRole> roles) {
        TENANT_ID.set(tenantId);
        USER_ID.set(userId);
        USER_EMAIL.set(email);
        DISPLAY_NAME.set(displayName);
        ROLES.set(roles);
    }

    public static Optional<UUID> getTenantId() {
        return Optional.ofNullable(TENANT_ID.get());
    }

    public static UUID requireTenantId() {
        return getTenantId().orElseThrow(() -> new SecurityException("Tenant context not set"));
    }

    public static Optional<UUID> getUserId() {
        return Optional.ofNullable(USER_ID.get());
    }

    public static UUID requireUserId() {
        return getUserId().orElseThrow(() -> new SecurityException("User context not set"));
    }

    public static Optional<String> getDisplayName() {
        return Optional.ofNullable(DISPLAY_NAME.get());
    }

    public static Optional<String> getUserEmail() {
        return Optional.ofNullable(USER_EMAIL.get());
    }

    public static boolean hasRole(UserRole role) {
        Set<UserRole> roles = ROLES.get();
        return roles != null && roles.contains(role);
    }

    public static boolean hasAnyRole(UserRole... required) {
        for (UserRole role : required) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        USER_EMAIL.remove();
        DISPLAY_NAME.remove();
        ROLES.remove();
    }
}
