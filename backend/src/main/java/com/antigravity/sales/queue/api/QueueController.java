package com.antigravity.sales.queue.api;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.LeadRepository;
import com.antigravity.sales.queue.service.AgentStatusService;
import com.antigravity.sales.queue.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final AgentStatusService agentStatusService;
    private final LeadRepository leadRepository;

    public QueueController(QueueService queueService, 
                           AgentStatusService agentStatusService,
                           LeadRepository leadRepository) {
        this.queueService = queueService;
        this.agentStatusService = agentStatusService;
        this.leadRepository = leadRepository;
    }

    // 1. Webhook for incoming WhatsApp Messages (Z-API, Evolution API, Twilio, etc)
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveMessage(@RequestBody IncomingMessageRequest request) {
        Lead lead = leadRepository.findById(request.getLeadId())
            .orElseThrow(() -> new IllegalArgumentException("Lead not found"));
            
        queueService.processIncomingMessage(lead, request.getText());
        return ResponseEntity.ok().build();
    }

    // 2. Agent Heartbeat Endpoint
    @PostMapping("/agent/{agentId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable UUID agentId) {
        agentStatusService.registerHeartbeat(agentId);
        return ResponseEntity.ok().build();
    }

    // 3. Agent going offline manually
    @PostMapping("/agent/{agentId}/offline")
    public ResponseEntity<Void> goOffline(@PathVariable UUID agentId) {
        agentStatusService.goOffline(agentId);
        return ResponseEntity.ok().build();
    }
    
    // 4. Agent finishes interaction
    @PostMapping("/interaction/{interactionId}/finish")
    public ResponseEntity<Void> finishInteraction(@PathVariable UUID interactionId) {
        queueService.finishInteraction(interactionId);
        return ResponseEntity.ok().build();
    }
}

class IncomingMessageRequest {
    private Long leadId;
    private String text;

    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
