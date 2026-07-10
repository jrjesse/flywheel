package com.antigravity.sales.queue.api;

import com.antigravity.sales.api.dto.WhatsAppConfigRequest;
import com.antigravity.sales.api.dto.WhatsAppConfigResponse;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.queue.model.WhatsAppChannelConfig;
import com.antigravity.sales.queue.service.WhatsAppConfigService;
import com.antigravity.sales.security.TenantAccessValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/whatsapp")
public class WhatsAppConfigController {

    private final WhatsAppConfigService configService;
    private final TenantAccessValidator tenantAccessValidator;
    private final AuditService auditService;

    public WhatsAppConfigController(
            WhatsAppConfigService configService,
            TenantAccessValidator tenantAccessValidator,
            AuditService auditService) {
        this.configService = configService;
        this.tenantAccessValidator = tenantAccessValidator;
        this.auditService = auditService;
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WhatsAppConfigResponse> getConfig(@PathVariable UUID clientId) {
        tenantAccessValidator.validateClientAccess(clientId);
        return ResponseEntity.ok(configService.toResponse(configService.getConfig(clientId)));
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WhatsAppConfigResponse> updateConfig(
            @PathVariable UUID clientId,
            @Valid @RequestBody WhatsAppConfigRequest request) {
        tenantAccessValidator.validateClientAccess(clientId);
        WhatsAppChannelConfig saved = configService.saveConfig(clientId, request);
        auditService.log("UPDATE", "WHATSAPP_CONFIG", clientId.toString());
        return ResponseEntity.ok(configService.toResponse(saved));
    }

    @PostMapping("/{clientId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody WhatsAppConfigRequest request) {
        tenantAccessValidator.validateClientAccess(clientId);
        WhatsAppChannelConfig config = configService.mergeForTest(clientId, request);
        boolean isSuccess = configService.testConnection(config);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Connected successfully to Meta API"));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid credentials or Phone Number ID"));
    }
}
