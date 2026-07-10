package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.LeadIngestionRequest;
import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.core.service.LeadIngestionService;
import com.antigravity.sales.security.WebhookAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks/leads")
public class LeadWebhookController {

    private final LeadIngestionService leadIngestionService;
    private final WebhookAuthService webhookAuthService;
    private final AuditService auditService;

    public LeadWebhookController(
            LeadIngestionService leadIngestionService,
            WebhookAuthService webhookAuthService,
            AuditService auditService) {
        this.leadIngestionService = leadIngestionService;
        this.webhookAuthService = webhookAuthService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<?> receiveLead(
            HttpServletRequest httpRequest,
            @Valid @RequestBody LeadIngestionRequest request) {
        UUID tenantId = webhookAuthService.resolveTenantFromLeadWebhook(httpRequest)
                .orElse(null);
        if (tenantId == null) {
            auditService.logAuthFailure("lead_webhook", "invalid_or_missing_secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        Lead savedLead = leadIngestionService.ingest(request, tenantId);
        auditService.log("CREATE", "LEAD", savedLead.getId().toString());
        return ResponseEntity.ok(savedLead);
    }
}
