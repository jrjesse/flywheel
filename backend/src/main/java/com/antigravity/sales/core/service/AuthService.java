package com.antigravity.sales.core.service;

import com.antigravity.sales.api.dto.auth.AuthResponse;
import com.antigravity.sales.api.dto.auth.LoginRequest;
import com.antigravity.sales.api.dto.auth.RegisterRequest;
import com.antigravity.sales.core.model.Tenant;
import com.antigravity.sales.core.model.TenantWebhookSecret;
import com.antigravity.sales.core.model.User;
import com.antigravity.sales.core.model.UserRole;
import com.antigravity.sales.core.model.PasswordResetToken;
import com.antigravity.sales.core.repository.TenantRepository;
import com.antigravity.sales.core.repository.TenantWebhookSecretRepository;
import com.antigravity.sales.core.repository.UserRepository;
import com.antigravity.sales.core.repository.PasswordResetTokenRepository;
import com.antigravity.sales.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantWebhookSecretRepository webhookSecretRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final boolean exposeResetTokenInResponse;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            TenantWebhookSecretRepository webhookSecretRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.security.password-reset.expose-token:false}") boolean exposeResetTokenInResponse) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.webhookSecretRepository = webhookSecretRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.exposeResetTokenInResponse = exposeResetTokenInResponse;
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
        return new AuthResponse(token, user.getId(), tenant.getId(), user.getEmail(), user.getDisplayName(), user.getRole().name(), webhookSecret);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getTenantId(), user.getEmail(), user.getDisplayName(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getTenantId(), user.getEmail(), user.getDisplayName(), user.getRole().name(), null);
    }

    @Transactional
    public com.antigravity.sales.api.dto.auth.ForgotPasswordResponse forgotPassword(String email) {
        var userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) {
            return new com.antigravity.sales.api.dto.auth.ForgotPasswordResponse(
                    "Se o email existir, um link de redefinição será enviado.", null);
        }

        User user = userOpt.get();
        String token = generateResetToken();
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUserId(user.getId());
        entity.setToken(token);
        entity.setExpiresAt(Instant.now().plusSeconds(3600));
        passwordResetTokenRepository.save(entity);

        String exposed = exposeResetTokenInResponse ? token : null;
        return new com.antigravity.sales.api.dto.auth.ForgotPasswordResponse(
                "Se o email existir, um link de redefinição será enviado.", exposed);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expirado");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateWebhookSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
