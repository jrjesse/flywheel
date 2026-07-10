package com.antigravity.sales.security;

import com.antigravity.sales.core.model.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record AuthenticatedUser(
        UUID userId,
        UUID tenantId,
        String email,
        String displayName,
        Set<UserRole> roles
) {
    public Collection<SimpleGrantedAuthority> authorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
}
