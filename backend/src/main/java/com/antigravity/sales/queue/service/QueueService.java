package com.antigravity.sales.queue.service;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import com.antigravity.sales.queue.model.InteractionStatusLog;
import com.antigravity.sales.queue.repository.InteractionQueueRepository;
import com.antigravity.sales.queue.repository.InteractionStatusLogRepository;
import com.antigravity.sales.core.model.SystemNotification;
import com.antigravity.sales.core.repository.SystemNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);
    
    private final InteractionQueueRepository queueRepository;
    private final InteractionStatusLogRepository logRepository;
    private final AgentStatusService agentStatusService;
    private final SystemNotificationRepository notificationRepository;

    public QueueService(InteractionQueueRepository queueRepository, 
                        InteractionStatusLogRepository logRepository,
                        AgentStatusService agentStatusService,
                        SystemNotificationRepository notificationRepository) {
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.agentStatusService = agentStatusService;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void processIncomingMessage(Lead lead, String messageText) {
        Optional<InteractionQueue> activeInteraction = queueRepository.findByLeadIdAndStatusIn(
            lead.getId(), 
            List.of(InteractionStatus.RECEBIDO, InteractionStatus.AGUARDANDO_ATENDIMENTO, InteractionStatus.EM_ATENDIMENTO)
        );

        if (activeInteraction.isEmpty()) {
            // New interaction: goes to Debounce state (RECEBIDO)
            InteractionQueue interaction = new InteractionQueue(lead, InteractionStatus.RECEBIDO);
            interaction.setMetadata(messageText); // In a real scenario, append to JSON array
            queueRepository.save(interaction);
            createLog(interaction, null, InteractionStatus.RECEBIDO, "New message received");
            log.info("New interaction created for lead {} with status RECEBIDO", lead.getId());
        } else {
            InteractionQueue interaction = activeInteraction.get();
            // Append message logic here...
            String currentMetadata = interaction.getMetadata() == null ? "" : interaction.getMetadata() + "\n";
            interaction.setMetadata(currentMetadata + messageText);
            
            // If it is in RECEBIDO, we just updated the timestamp so debounce gets reset
            queueRepository.save(interaction);
            
            if (interaction.getStatus() == InteractionStatus.EM_ATENDIMENTO || interaction.getStatus() == InteractionStatus.AGUARDANDO_RESPOSTA) {
                // Should notify agent via WebSocket
                if (interaction.getStatus() == InteractionStatus.AGUARDANDO_RESPOSTA) {
                    interaction.setStatus(InteractionStatus.EM_ATENDIMENTO);
                    queueRepository.save(interaction);
                    createLog(interaction, InteractionStatus.AGUARDANDO_RESPOSTA, InteractionStatus.EM_ATENDIMENTO, "Lead replied to outbound");
                }
                log.info("Message appended to active chat for agent {}", interaction.getAssignedAgentId());
            }
        }
    }

    @Transactional
    public void attemptRoute(InteractionQueue interaction) {
        UUID agentId = agentStatusService.findAvailableAgentAndAssign();
        
        InteractionStatus oldStatus = interaction.getStatus();
        if (agentId != null) {
            interaction.setStatus(InteractionStatus.AGUARDANDO_ATENDIMENTO); // or directly to EM_ATENDIMENTO depending on flow
            interaction.setAssignedAgentId(agentId);
            interaction.setStartedAt(Instant.now());
            queueRepository.save(interaction);
            createLog(interaction, oldStatus, InteractionStatus.AGUARDANDO_ATENDIMENTO, "Assigned to agent " + agentId);
            log.info("Interaction {} routed to agent {}", interaction.getId(), agentId);
        } else {
            // Transbordo if capacity full
            interaction.setStatus(InteractionStatus.TRANSBORDADO);
            queueRepository.save(interaction);
            createLog(interaction, oldStatus, InteractionStatus.TRANSBORDADO, "Capacity full");
            
            SystemNotification notif = new SystemNotification();
            notif.setTenantId(interaction.getLead().getTenantId());
            notif.setType("QUEUE_OVERFLOW");
            notif.setMessage("Fila lotada: Lead " + interaction.getLead().getName() + " entrou em transbordo por falta de agentes.");
            notif.setLeadId(interaction.getLead().getId());
            notif.setRead(false);
            notificationRepository.save(notif);
            
            log.warn("Interaction {} transbordado due to capacity", interaction.getId());
        }
    }
    
    @Transactional
    public void finishInteraction(UUID interactionId) {
        Optional<InteractionQueue> interactionOpt = queueRepository.findById(interactionId);
        if (interactionOpt.isPresent()) {
            InteractionQueue interaction = interactionOpt.get();
            InteractionStatus oldStatus = interaction.getStatus();
            interaction.setStatus(InteractionStatus.FINALIZADO);
            interaction.setFinishedAt(Instant.now());
            queueRepository.save(interaction);
            
            createLog(interaction, oldStatus, InteractionStatus.FINALIZADO, "Finished by agent");
            if (interaction.getAssignedAgentId() != null) {
                agentStatusService.releaseChatCapacity(interaction.getAssignedAgentId());
            }
        }
    }

    private void createLog(InteractionQueue interaction, InteractionStatus previous, InteractionStatus next, String reason) {
        InteractionStatusLog logEntry = new InteractionStatusLog(interaction, previous, next, reason);
        logRepository.save(logEntry);
    }
    
    @Transactional
    public void registerOutboundInteraction(Lead lead, UUID agentId, String initialMessage) {
        // Verifica se já existe, caso não exista, cria em AGUARDANDO_RESPOSTA e atribui ao agente.
        Optional<InteractionQueue> activeInteraction = queueRepository.findByLeadIdAndStatusIn(
            lead.getId(), 
            List.of(InteractionStatus.RECEBIDO, InteractionStatus.AGUARDANDO_ATENDIMENTO, InteractionStatus.EM_ATENDIMENTO, InteractionStatus.AGUARDANDO_RESPOSTA)
        );

        if (activeInteraction.isEmpty()) {
            InteractionQueue interaction = new InteractionQueue(lead, InteractionStatus.AGUARDANDO_RESPOSTA);
            interaction.setMetadata(initialMessage);
            interaction.setAssignedAgentId(agentId); // Opcão 3: Assumido pelo atendente/sistema que enviou
            interaction.setStartedAt(Instant.now());
            queueRepository.save(interaction);
            
            createLog(interaction, null, InteractionStatus.AGUARDANDO_RESPOSTA, "Outbound message sent by Prospector");
            log.info("Outbound interaction created for lead {} with status AGUARDANDO_RESPOSTA", lead.getId());
            
            // Note: Se agentId não for nulo, deveríamos incrementar a capacidade dele, 
            // mas como outbound não gasta capacity ativa imediata na mesma proporção,
            // podemos deixar para consumir só quando o status for EM_ATENDIMENTO, ou consumir agora.
        }
    }
}
