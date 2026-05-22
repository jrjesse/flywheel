package com.antigravity.sales.queue.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_status")
public class AgentStatus {

    @Id
    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    @Column(name = "active_chats_count", nullable = false)
    private int activeChatsCount = 0;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity = 5;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Version
    @Column(name = "version")
    private Long version;

    public AgentStatus() {}

    public AgentStatus(UUID agentId) {
        this.agentId = agentId;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getActiveChatsCount() {
        return activeChatsCount;
    }

    public void setActiveChatsCount(int activeChatsCount) {
        this.activeChatsCount = activeChatsCount;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
