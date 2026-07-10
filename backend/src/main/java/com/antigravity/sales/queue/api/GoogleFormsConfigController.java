package com.antigravity.sales.queue.api;

import com.antigravity.sales.api.dto.GoogleFormsConfigRequest;
import com.antigravity.sales.api.dto.GoogleFormsConfigResponse;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.queue.model.GoogleFormsConfig;
import com.antigravity.sales.queue.service.GoogleFormsConfigService;
import com.antigravity.sales.security.TenantAccessValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/google-forms")
public class GoogleFormsConfigController {

    private final GoogleFormsConfigService configService;
    private final TenantAccessValidator tenantAccessValidator;
    private final AuditService auditService;

    public GoogleFormsConfigController(
            GoogleFormsConfigService configService,
            TenantAccessValidator tenantAccessValidator,
            AuditService auditService) {
        this.configService = configService;
        this.tenantAccessValidator = tenantAccessValidator;
        this.auditService = auditService;
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GoogleFormsConfigResponse> getConfig(@PathVariable UUID clientId) {
        tenantAccessValidator.validateClientAccess(clientId);
        return ResponseEntity.ok(toResponse(configService.getConfig(clientId)));
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable UUID clientId,
            @Valid @RequestBody GoogleFormsConfigRequest request) {
        tenantAccessValidator.validateClientAccess(clientId);
        GoogleFormsConfig config = new GoogleFormsConfig();
        config.setFormName(request.getFormName());
        config.setActive(request.isActive());
        if (request.isRegenerateToken()) {
            config.setWebhookTokenHash(null);
        }
        GoogleFormsConfigService.SaveResult result = configService.saveConfig(clientId, config);
        auditService.log("UPDATE", "GOOGLE_FORMS_CONFIG", clientId.toString());

        Map<String, Object> body = new HashMap<>();
        body.put("config", toResponse(result.config()));
        if (result.plainWebhookToken() != null) {
            body.put("webhookToken", result.plainWebhookToken());
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{clientId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testConnection(@PathVariable UUID clientId, @RequestBody Map<String, String> payload) {
        tenantAccessValidator.validateClientAccess(clientId);
        String token = payload.get("token");
        boolean isSuccess = configService.testConnection(token);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Webhook Token is valid"));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid webhook token"));
    }

    private GoogleFormsConfigResponse toResponse(GoogleFormsConfig config) {
        return GoogleFormsConfigResponse.builder()
                .clientId(config.getClientId())
                .formName(config.getFormName())
                .active(config.isActive())
                .hasWebhookToken(config.getWebhookTokenHash() != null && !config.getWebhookTokenHash().isBlank())
                .build();
    }
}
