package com.antigravity.sales.queue.api;

import com.antigravity.sales.queue.model.InstagramChannelConfig;
import com.antigravity.sales.queue.service.InstagramConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/instagram")
@CrossOrigin(origins = "*")
public class InstagramConfigController {
    private final InstagramConfigService configService;

    public InstagramConfigController(InstagramConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<InstagramChannelConfig> getConfig(@PathVariable UUID clientId) {
        return ResponseEntity.ok(configService.getConfig(clientId));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<InstagramChannelConfig> updateConfig(
            @PathVariable UUID clientId, 
            @RequestBody InstagramChannelConfig config) {
        return ResponseEntity.ok(configService.saveConfig(clientId, config));
    }

    @PostMapping("/{clientId}/test")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody InstagramChannelConfig config) {
        boolean isSuccess = configService.testConnection(config);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Connected successfully to Instagram API"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid credentials"));
        }
    }
}
