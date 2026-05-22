package com.antigravity.sales.queue.service;

import com.antigravity.sales.queue.model.AgentStatus;
import com.antigravity.sales.queue.repository.AgentStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentStatusService {

    private static final Logger log = LoggerFactory.getLogger(AgentStatusService.class);
    private final AgentStatusRepository agentStatusRepository;

    public AgentStatusService(AgentStatusRepository agentStatusRepository) {
        this.agentStatusRepository = agentStatusRepository;
    }

    @Transactional
    public void registerHeartbeat(UUID agentId) {
        Optional<AgentStatus> statusOpt = agentStatusRepository.findById(agentId);
        AgentStatus status;
        if (statusOpt.isPresent()) {
            status = statusOpt.get();
        } else {
            status = new AgentStatus(agentId);
        }
        
        status.setOnline(true);
        status.setLastHeartbeat(Instant.now());
        agentStatusRepository.save(status);
        log.debug("Heartbeat registered for agent {}", agentId);
    }

    @Transactional
    public void goOffline(UUID agentId) {
        Optional<AgentStatus> statusOpt = agentStatusRepository.findById(agentId);
        if (statusOpt.isPresent()) {
            AgentStatus status = statusOpt.get();
            status.setOnline(false);
            agentStatusRepository.save(status);
            log.info("Agent {} went offline", agentId);
        }
    }

    @Transactional
    public UUID findAvailableAgentAndAssign() {
        // Simple Round-Robin: find all online, pick one that we can increment
        // In a real high-scale system, this query would be ordered by last assigned or we would use a Redis queue for agents.
        List<AgentStatus> onlineAgents = agentStatusRepository.findByIsOnlineTrue();
        
        for (AgentStatus agent : onlineAgents) {
            int updated = agentStatusRepository.incrementActiveChatsIfCapacityAllows(agent.getAgentId());
            if (updated > 0) {
                return agent.getAgentId();
            }
        }
        return null; // No capacity
    }
    
    @Transactional
    public void releaseChatCapacity(UUID agentId) {
        agentStatusRepository.decrementActiveChats(agentId);
    }
}
