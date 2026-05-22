package com.antigravity.sales.queue.job;

import com.antigravity.sales.queue.model.AgentStatus;
import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import com.antigravity.sales.queue.model.InteractionStatusLog;
import com.antigravity.sales.queue.repository.AgentStatusRepository;
import com.antigravity.sales.queue.repository.InteractionQueueRepository;
import com.antigravity.sales.queue.repository.InteractionStatusLogRepository;
import com.antigravity.sales.core.model.SystemNotification;
import com.antigravity.sales.core.repository.SystemNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AgentHeartbeatJob {

    private static final Logger log = LoggerFactory.getLogger(AgentHeartbeatJob.class);

    private final AgentStatusRepository agentStatusRepository;
    private final InteractionQueueRepository queueRepository;
    private final InteractionStatusLogRepository logRepository;
    private final SystemNotificationRepository notificationRepository;

    public AgentHeartbeatJob(AgentStatusRepository agentStatusRepository, 
                             InteractionQueueRepository queueRepository,
                             InteractionStatusLogRepository logRepository,
                             SystemNotificationRepository notificationRepository) {
        this.agentStatusRepository = agentStatusRepository;
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.notificationRepository = notificationRepository;
    }

    // Runs every 30 seconds to check if agents are alive
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkAgentHeartbeats() {
        List<AgentStatus> onlineAgents = agentStatusRepository.findByIsOnlineTrue();
        Instant threshold = Instant.now().minus(2, ChronoUnit.MINUTES); // 2 minutes without heartbeat = offline
        
        for (AgentStatus agent : onlineAgents) {
            if (agent.getLastHeartbeat() != null && agent.getLastHeartbeat().isBefore(threshold)) {
                log.warn("Agent {} missed heartbeats. Marking offline and releasing chats.", agent.getAgentId());
                
                // 1. Reset agent capacity atomically
                agentStatusRepository.resetAgentCapacityAndSetOffline(agent.getAgentId());
                
                // 2. Re-enfileirar os chats ativos deste atendente
                List<InteractionQueue> activeChats = queueRepository.findByAssignedAgentIdAndStatus(agent.getAgentId(), InteractionStatus.EM_ATENDIMENTO);
                for (InteractionQueue chat : activeChats) {
                    InteractionStatus oldStatus = chat.getStatus();
                    chat.setStatus(InteractionStatus.TRANSBORDADO); // It goes to transbordo so a supervisor or the queue router can reassign it
                    chat.setAssignedAgentId(null);
                    chat.setUpdatedAt(Instant.now());
                    queueRepository.save(chat);
                    
                    InteractionStatusLog logEntry = new InteractionStatusLog(chat, oldStatus, InteractionStatus.TRANSBORDADO, "Agent Disconnected. Re-enqueued.");
                    logRepository.save(logEntry);
                    
                    SystemNotification notif = new SystemNotification();
                    notif.setType("SYSTEM_FAILURE");
                    notif.setMessage("Falha: Agente " + agent.getAgentId() + " caiu (timeout). Lead " + chat.getLead().getName() + " foi para transbordo.");
                    notif.setLeadId(chat.getLead().getId());
                    notif.setRead(false);
                    notificationRepository.save(notif);
                }
            }
        }
    }
}
