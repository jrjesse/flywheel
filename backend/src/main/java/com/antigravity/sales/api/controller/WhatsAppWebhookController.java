package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.LeadRepository;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.queue.service.QueueService;
import com.antigravity.sales.security.WebhookAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private final QueueService queueService;
    private final LeadRepository leadRepository;
    private final WebhookAuthService webhookAuthService;
    private final AuditService auditService;

    public WhatsAppWebhookController(
            QueueService queueService,
            LeadRepository leadRepository,
            WebhookAuthService webhookAuthService,
            AuditService auditService) {
        this.queueService = queueService;
        this.leadRepository = leadRepository;
        this.webhookAuthService = webhookAuthService;
        this.auditService = auditService;
    }

    @PostMapping("/{tenantId}")
    public ResponseEntity<Void> receiveMessage(
            @PathVariable UUID tenantId,
            HttpServletRequest request,
            @RequestBody String payload,
            @RequestParam Long leadId,
            @RequestParam(required = false) String text) {

        if (!webhookAuthService.validateWhatsAppSignature(request, payload, tenantId)) {
            auditService.logAuthFailure("whatsapp_webhook", "invalid_signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Lead lead = leadRepository.findByIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        queueService.processIncomingMessage(lead, text != null ? text : payload);
        auditService.log("WEBHOOK", "WHATSAPP_MESSAGE", leadId.toString());
        return ResponseEntity.ok().build();
    }
}
