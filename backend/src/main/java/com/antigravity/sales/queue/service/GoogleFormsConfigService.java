package com.antigravity.sales.queue.service;

import com.antigravity.sales.queue.model.GoogleFormsConfig;
import com.antigravity.sales.queue.repository.GoogleFormsConfigRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GoogleFormsConfigService {
    private final GoogleFormsConfigRepository repository;

    public GoogleFormsConfigService(GoogleFormsConfigRepository repository) {
        this.repository = repository;
    }

    public GoogleFormsConfig getConfig(UUID clientId) {
        return repository.findByClientId(clientId).orElse(new GoogleFormsConfig());
    }

    public GoogleFormsConfig saveConfig(UUID clientId, GoogleFormsConfig config) {
        GoogleFormsConfig existing = repository.findByClientId(clientId).orElse(new GoogleFormsConfig());
        existing.setClientId(clientId);
        existing.setWebhookToken(config.getWebhookToken());
        existing.setFormName(config.getFormName());
        existing.setActive(config.isActive());
        return repository.save(existing);
    }

    public boolean testConnection(GoogleFormsConfig config) {
        // Mock connection test for Google Forms Webhook
        return config.getWebhookToken() != null && !config.getWebhookToken().isBlank();
    }
}
