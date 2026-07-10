package com.antigravity.sales.queue.api;

import com.antigravity.sales.api.dto.InstagramConfigRequest;
import com.antigravity.sales.api.dto.InstagramConfigResponse;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.queue.model.InstagramChannelConfig;
import com.antigravity.sales.queue.service.InstagramConfigService;
import com.antigravity.sales.security.TenantAccessValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/instagram")
public class InstagramConfigController {

    private final InstagramConfigService configService;
    private final TenantAccessValidator tenantAccessValidator;
    private final AuditService auditService;

    public InstagramConfigController(
            InstagramConfigService configService,
            TenantAccessValidator tenantAccessValidator,
            AuditService auditService) {
        this.configService = configService;
        this.tenantAccessValidator = tenantAccessValidator;
        this.auditService = auditService;
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstagramConfigResponse> getConfig(@PathVariable UUID clientId) {
        tenantAccessValidator.validateClientAccess(clientId);
        return ResponseEntity.ok(configService.toResponse(configService.getConfig(clientId)));
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstagramConfigResponse> updateConfig(
            @PathVariable UUID clientId,
            @Valid @RequestBody InstagramConfigRequest request) {
        tenantAccessValidator.validateClientAccess(clientId);
        InstagramChannelConfig saved = configService.saveConfig(clientId, request);
        auditService.log("UPDATE", "INSTAGRAM_CONFIG", clientId.toString());
        return ResponseEntity.ok(configService.toResponse(saved));
    }

    @PostMapping("/{clientId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody InstagramConfigRequest request) {
        tenantAccessValidator.validateClientAccess(clientId);
        boolean isSuccess = configService.testConnection(configService.mergeForTest(clientId, request));
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Connected successfully to Instagram API"));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid credentials"));
    }
}
