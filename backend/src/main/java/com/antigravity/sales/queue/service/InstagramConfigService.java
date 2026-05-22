package com.antigravity.sales.queue.service;

import com.antigravity.sales.queue.model.InstagramChannelConfig;
import com.antigravity.sales.queue.repository.InstagramChannelConfigRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class InstagramConfigService {
    private final InstagramChannelConfigRepository repository;

    public InstagramConfigService(InstagramChannelConfigRepository repository) {
        this.repository = repository;
    }

    public InstagramChannelConfig getConfig(UUID clientId) {
        return repository.findByClientId(clientId).orElse(new InstagramChannelConfig());
    }

    public InstagramChannelConfig saveConfig(UUID clientId, InstagramChannelConfig config) {
        InstagramChannelConfig existing = repository.findByClientId(clientId).orElse(new InstagramChannelConfig());
        existing.setClientId(clientId);
        existing.setAccessToken(config.getAccessToken());
        existing.setInstagramAccountId(config.getInstagramAccountId());
        existing.setPageId(config.getPageId());
        existing.setAppSecret(config.getAppSecret());
        existing.setVerifyToken(config.getVerifyToken());
        existing.setActive(config.isActive());
        return repository.save(existing);
    }

    public boolean testConnection(InstagramChannelConfig config) {
        // Mock connection test to Instagram Graph API
        return config.getAccessToken() != null && !config.getAccessToken().isBlank();
    }
}
