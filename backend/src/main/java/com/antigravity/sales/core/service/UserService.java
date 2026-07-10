package com.antigravity.sales.core.service;

import com.antigravity.sales.api.dto.user.InviteUserRequest;
import com.antigravity.sales.api.dto.user.UpdateUserRoleRequest;
import com.antigravity.sales.api.dto.user.UserResponse;
import com.antigravity.sales.core.model.User;
import com.antigravity.sales.core.model.UserRole;
import com.antigravity.sales.core.repository.UserRepository;
import com.antigravity.sales.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Set<UserRole> INVITABLE_ROLES = EnumSet.of(UserRole.AGENT, UserRole.MANAGER, UserRole.VIEWER);
    private static final Set<UserRole> ASSIGNABLE_ROLES = EnumSet.of(UserRole.AGENT, UserRole.MANAGER, UserRole.VIEWER, UserRole.ADMIN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse inviteUser(InviteUserRequest request) {
        if (!INVITABLE_ROLES.contains(request.getRole())) {
            throw new IllegalArgumentException("Role inválida para convite. Use AGENT, MANAGER ou VIEWER.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        UUID tenantId = TenantContext.requireTenantId();
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(tenantId);
        user.setRole(request.getRole());
        user.setActive(true);

        User saved = userRepository.save(user);
        auditService.log("CREATE", "USER", saved.getId().toString());
        return toResponse(saved);
    }

    public List<UserResponse> listTeamMembers() {
        UUID tenantId = TenantContext.requireTenantId();
        auditService.log("READ", "USER", "team");
        return userRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getTeamMember(UUID userId) {
        User user = requireUserInTenant(userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateRole(UUID userId, UpdateUserRoleRequest request) {
        if (!ASSIGNABLE_ROLES.contains(request.getRole())) {
            throw new IllegalArgumentException("Role inválida");
        }

        User user = requireUserInTenant(userId);
        if (user.getId().equals(TenantContext.requireUserId()) && request.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Não é permitido rebaixar o próprio perfil de administrador");
        }

        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        auditService.log("UPDATE", "USER_ROLE", saved.getId().toString());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse deactivateUser(UUID userId) {
        User user = requireUserInTenant(userId);
        if (user.getId().equals(TenantContext.requireUserId())) {
            throw new IllegalArgumentException("Não é permitido desativar o próprio usuário");
        }

        user.setActive(false);
        User saved = userRepository.save(user);
        auditService.log("UPDATE", "USER_DEACTIVATE", saved.getId().toString());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse activateUser(UUID userId) {
        User user = requireUserInTenant(userId);
        user.setActive(true);
        User saved = userRepository.save(user);
        auditService.log("UPDATE", "USER_ACTIVATE", saved.getId().toString());
        return toResponse(saved);
    }

    private User requireUserInTenant(UUID userId) {
        return userRepository.findByIdAndTenantId(userId, TenantContext.requireTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
