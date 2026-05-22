package com.antigravity.sales.queue.api;

import com.antigravity.sales.queue.model.WhatsAppChannelConfig;
import com.antigravity.sales.queue.service.WhatsAppConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/whatsapp")
@CrossOrigin(origins = "*")
public class WhatsAppConfigController {

    private final WhatsAppConfigService configService;

    public WhatsAppConfigController(WhatsAppConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<WhatsAppChannelConfig> getConfig(@PathVariable UUID clientId) {
        return ResponseEntity.ok(configService.getConfig(clientId));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<WhatsAppChannelConfig> updateConfig(
            @PathVariable UUID clientId, 
            @RequestBody WhatsAppChannelConfig config) {
        return ResponseEntity.ok(configService.saveConfig(clientId, config));
    }

    @PostMapping("/{clientId}/test")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody WhatsAppChannelConfig config) {
        // You can either test the saved config, or the unsaved one coming from the UI
        boolean isSuccess = configService.testConnection(config);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Connected successfully to Meta API"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid credentials or Phone Number ID"));
        }
    }
}
