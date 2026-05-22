package com.antigravity.sales.queue.service;

import com.antigravity.sales.queue.model.WhatsAppChannelConfig;
import com.antigravity.sales.queue.repository.WhatsAppChannelConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
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

    public WhatsAppChannelConfig saveConfig(UUID clientId, WhatsAppChannelConfig newConfig) {
        WhatsAppChannelConfig existing = configRepository.findByClientId(clientId).orElse(new WhatsAppChannelConfig());
        existing.setClientId(clientId);
        existing.setAccessToken(newConfig.getAccessToken());
        existing.setPhoneNumberId(newConfig.getPhoneNumberId());
        existing.setWabaId(newConfig.getWabaId());
        existing.setAppSecret(newConfig.getAppSecret());
        existing.setVerifyToken(newConfig.getVerifyToken());
        
        if(newConfig.getDebounceSeconds() != null) existing.setDebounceSeconds(newConfig.getDebounceSeconds());
        if(newConfig.getSlaMinutes() != null) existing.setSlaMinutes(newConfig.getSlaMinutes());
        if(newConfig.getMaxCapacity() != null) existing.setMaxCapacity(newConfig.getMaxCapacity());

        // For MVP: Plain text storage. 
        // TODO: In production, apply AES encryption/decryption on accessToken and appSecret here.

        return configRepository.save(existing);
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
