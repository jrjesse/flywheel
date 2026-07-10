package com.antigravity.sales.queue.service;

import com.antigravity.sales.api.dto.InstagramConfigRequest;
import com.antigravity.sales.api.dto.InstagramConfigResponse;
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
        return repository.findByClientId(clientId).orElseGet(() -> {
            InstagramChannelConfig config = new InstagramChannelConfig();
            config.setClientId(clientId);
            return config;
        });
    }

    public InstagramChannelConfig saveConfig(UUID clientId, InstagramConfigRequest request) {
        InstagramChannelConfig existing = repository.findByClientId(clientId).orElse(new InstagramChannelConfig());
        existing.setClientId(clientId);
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            existing.setAccessToken(request.getAccessToken());
        }
        if (request.getInstagramAccountId() != null) existing.setInstagramAccountId(request.getInstagramAccountId());
        if (request.getPageId() != null) existing.setPageId(request.getPageId());
        if (request.getAppSecret() != null && !request.getAppSecret().isBlank()) {
            existing.setAppSecret(request.getAppSecret());
        }
        if (request.getVerifyToken() != null && !request.getVerifyToken().isBlank()) {
            existing.setVerifyToken(request.getVerifyToken());
        }
        existing.setActive(request.isActive());
        return repository.save(existing);
    }

    public InstagramChannelConfig mergeForTest(UUID clientId, InstagramConfigRequest request) {
        InstagramChannelConfig config = getConfig(clientId);
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            config.setAccessToken(request.getAccessToken());
        }
        if (request.getInstagramAccountId() != null) config.setInstagramAccountId(request.getInstagramAccountId());
        return config;
    }

    public InstagramConfigResponse toResponse(InstagramChannelConfig config) {
        return InstagramConfigResponse.builder()
                .clientId(config.getClientId())
                .instagramAccountId(config.getInstagramAccountId())
                .pageId(config.getPageId())
                .hasAccessToken(config.getAccessToken() != null && !config.getAccessToken().isBlank())
                .hasAppSecret(config.getAppSecret() != null && !config.getAppSecret().isBlank())
                .hasVerifyToken(config.getVerifyToken() != null && !config.getVerifyToken().isBlank())
                .active(config.isActive())
                .build();
    }

    public boolean testConnection(InstagramChannelConfig config) {
        return config.getAccessToken() != null && !config.getAccessToken().isBlank()
                && config.getInstagramAccountId() != null && !config.getInstagramAccountId().isBlank();
    }
}
