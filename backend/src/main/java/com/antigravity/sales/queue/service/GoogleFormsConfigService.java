package com.antigravity.sales.queue.service;

import com.antigravity.sales.queue.model.GoogleFormsConfig;
import com.antigravity.sales.queue.repository.GoogleFormsConfigRepository;
import com.antigravity.sales.security.crypto.WebhookTokenHasher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class GoogleFormsConfigService {
    private final GoogleFormsConfigRepository repository;
    private final WebhookTokenHasher tokenHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleFormsConfigService(GoogleFormsConfigRepository repository, WebhookTokenHasher tokenHasher) {
        this.repository = repository;
        this.tokenHasher = tokenHasher;
    }

    public GoogleFormsConfig getConfig(UUID clientId) {
        return repository.findByClientId(clientId).orElse(new GoogleFormsConfig());
    }

    public SaveResult saveConfig(UUID clientId, GoogleFormsConfig config) {
        GoogleFormsConfig existing = repository.findByClientId(clientId).orElse(new GoogleFormsConfig());
        existing.setClientId(clientId);
        existing.setFormName(config.getFormName());
        existing.setActive(config.isActive());

        String plainToken = null;
        if (config.getWebhookTokenHash() == null || config.getWebhookTokenHash().isBlank()) {
            plainToken = generateToken();
            existing.setWebhookTokenHash(tokenHasher.hash(plainToken));
        }

        GoogleFormsConfig saved = repository.save(existing);
        return new SaveResult(saved, plainToken);
    }

    public boolean testConnection(String token) {
        return token != null && !token.isBlank()
                && repository.findByWebhookTokenHash(tokenHasher.hash(token)).isPresent();
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record SaveResult(GoogleFormsConfig config, String plainWebhookToken) {}
}
