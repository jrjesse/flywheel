package com.antigravity.sales.queue.api;

import com.antigravity.sales.queue.model.GoogleFormsConfig;
import com.antigravity.sales.queue.service.GoogleFormsConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/google-forms")
@CrossOrigin(origins = "*")
public class GoogleFormsConfigController {
    private final GoogleFormsConfigService configService;

    public GoogleFormsConfigController(GoogleFormsConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<GoogleFormsConfig> getConfig(@PathVariable UUID clientId) {
        return ResponseEntity.ok(configService.getConfig(clientId));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<GoogleFormsConfig> updateConfig(
            @PathVariable UUID clientId, 
            @RequestBody GoogleFormsConfig config) {
        return ResponseEntity.ok(configService.saveConfig(clientId, config));
    }

    @PostMapping("/{clientId}/test")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody GoogleFormsConfig config) {
        boolean isSuccess = configService.testConnection(config);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Webhook Token is valid"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid webhook token"));
        }
    }
}
