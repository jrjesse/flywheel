package com.antigravity.sales.queue.api;

import com.antigravity.sales.core.repository.LeadRepository;
import com.antigravity.sales.core.service.AuditService;
import com.antigravity.sales.queue.service.AgentStatusService;
import com.antigravity.sales.queue.service.QueueService;
import com.antigravity.sales.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final AgentStatusService agentStatusService;
    private final LeadRepository leadRepository;
    private final AuditService auditService;

    public QueueController(QueueService queueService,
                           AgentStatusService agentStatusService,
                           LeadRepository leadRepository,
                           AuditService auditService) {
        this.queueService = queueService;
        this.agentStatusService = agentStatusService;
        this.leadRepository = leadRepository;
        this.auditService = auditService;
    }

    @PostMapping("/agent/{agentId}/heartbeat")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<Void> heartbeat(@PathVariable UUID agentId) {
        if (!TenantContext.requireUserId().equals(agentId)) {
            return ResponseEntity.status(403).build();
        }
        agentStatusService.registerHeartbeat(agentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agent/{agentId}/offline")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<Void> goOffline(@PathVariable UUID agentId) {
        if (!TenantContext.requireUserId().equals(agentId)) {
            return ResponseEntity.status(403).build();
        }
        agentStatusService.goOffline(agentId);
        auditService.log("UPDATE", "AGENT_STATUS", agentId.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/interaction/{interactionId}/finish")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<Void> finishInteraction(@PathVariable UUID interactionId) {
        queueService.finishInteraction(interactionId);
        auditService.log("UPDATE", "INTERACTION_QUEUE", interactionId.toString());
        return ResponseEntity.ok().build();
    }
}
