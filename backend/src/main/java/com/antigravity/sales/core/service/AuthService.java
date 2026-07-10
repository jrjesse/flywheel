package com.antigravity.sales.core.service;

import com.antigravity.sales.api.dto.auth.AuthResponse;
import com.antigravity.sales.api.dto.auth.LoginRequest;
import com.antigravity.sales.api.dto.auth.RegisterRequest;
import com.antigravity.sales.core.model.Tenant;
import com.antigravity.sales.core.model.TenantWebhookSecret;
import com.antigravity.sales.core.model.User;
import com.antigravity.sales.core.model.UserRole;
import com.antigravity.sales.core.repository.TenantRepository;
import com.antigravity.sales.core.repository.TenantWebhookSecretRepository;
import com.antigravity.sales.core.repository.UserRepository;
import com.antigravity.sales.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantWebhookSecretRepository webhookSecretRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            TenantWebhookSecretRepository webhookSecretRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.webhookSecretRepository = webhookSecretRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getTenantName());
        tenant.setDocument(request.getDocument());
        tenant.setDocumentType(request.getDocumentType());
        tenant = tenantRepository.save(tenant);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setTenantId(tenant.getId());
        user.setRole(UserRole.ADMIN);
        user = userRepository.save(user);

        String webhookSecret = generateWebhookSecret();
        TenantWebhookSecret secretEntity = new TenantWebhookSecret();
        secretEntity.setTenantId(tenant.getId());
        secretEntity.setSecretHash(passwordEncoder.encode(webhookSecret));
        webhookSecretRepository.save(secretEntity);

        String token = jwtService.generateToken(user.getId(), tenant.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
        return new AuthResponse(token, user.getId(), tenant.getId(), user.getEmail(), user.getRole().name(), webhookSecret);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getTenantId(), user.getEmail(), user.getDisplayName(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name(), null);
    }

    private String generateWebhookSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
