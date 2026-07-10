package com.antigravity.sales.queue.service;

import com.antigravity.sales.api.dto.WhatsAppConfigRequest;
import com.antigravity.sales.api.dto.WhatsAppConfigResponse;
import com.antigravity.sales.queue.model.WhatsAppChannelConfig;
import com.antigravity.sales.queue.repository.WhatsAppChannelConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
public class WhatsAppConfigService {

    private final WhatsAppChannelConfigRepository configRepository;
    private final RestTemplate restTemplate;

    public WhatsAppConfigService(WhatsAppChannelConfigRepository configRepository) {
        this.configRepository = configRepository;
        this.restTemplate = new RestTemplate();
    }

    public WhatsAppChannelConfig getConfig(UUID clientId) {
        return configRepository.findByClientId(clientId).orElseGet(() -> {
            WhatsAppChannelConfig newConfig = new WhatsAppChannelConfig();
            newConfig.setClientId(clientId);
            return newConfig;
        });
    }

    public WhatsAppChannelConfig saveConfig(UUID clientId, WhatsAppConfigRequest request) {
        WhatsAppChannelConfig existing = configRepository.findByClientId(clientId).orElse(new WhatsAppChannelConfig());
        existing.setClientId(clientId);
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            existing.setAccessToken(request.getAccessToken());
        }
        if (request.getPhoneNumberId() != null) existing.setPhoneNumberId(request.getPhoneNumberId());
        if (request.getWabaId() != null) existing.setWabaId(request.getWabaId());
        if (request.getAppSecret() != null && !request.getAppSecret().isBlank()) {
            existing.setAppSecret(request.getAppSecret());
        }
        if (request.getVerifyToken() != null && !request.getVerifyToken().isBlank()) {
            existing.setVerifyToken(request.getVerifyToken());
        }
        if (request.getDebounceSeconds() != null) existing.setDebounceSeconds(request.getDebounceSeconds());
        if (request.getSlaMinutes() != null) existing.setSlaMinutes(request.getSlaMinutes());
        if (request.getMaxCapacity() != null) existing.setMaxCapacity(request.getMaxCapacity());
        return configRepository.save(existing);
    }

    public WhatsAppChannelConfig mergeForTest(UUID clientId, WhatsAppConfigRequest request) {
        WhatsAppChannelConfig config = getConfig(clientId);
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            config.setAccessToken(request.getAccessToken());
        }
        if (request.getPhoneNumberId() != null) config.setPhoneNumberId(request.getPhoneNumberId());
        return config;
    }

    public WhatsAppConfigResponse toResponse(WhatsAppChannelConfig config) {
        return WhatsAppConfigResponse.builder()
                .clientId(config.getClientId())
                .phoneNumberId(config.getPhoneNumberId())
                .wabaId(config.getWabaId())
                .hasAccessToken(config.getAccessToken() != null && !config.getAccessToken().isBlank())
                .hasAppSecret(config.getAppSecret() != null && !config.getAppSecret().isBlank())
                .hasVerifyToken(config.getVerifyToken() != null && !config.getVerifyToken().isBlank())
                .debounceSeconds(config.getDebounceSeconds())
                .slaMinutes(config.getSlaMinutes())
                .maxCapacity(config.getMaxCapacity())
                .build();
    }

    public boolean testConnection(WhatsAppChannelConfig config) {
        try {
            if (config.getPhoneNumberId() == null || config.getAccessToken() == null) {
                return false;
            }
            String url = "https://graph.facebook.com/v19.0/" + config.getPhoneNumberId();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Meta API connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
