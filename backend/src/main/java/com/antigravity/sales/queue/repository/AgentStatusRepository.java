package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentStatusRepository extends JpaRepository<AgentStatus, UUID> {

    List<AgentStatus> findByIsOnlineTrue();

    // Atomic increment query to avoid race conditions when assigning chats
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AgentStatus a SET a.activeChatsCount = a.activeChatsCount + 1, a.version = a.version + 1 " +
           "WHERE a.agentId = :agentId AND a.activeChatsCount < a.maxCapacity AND a.isOnline = true")
    int incrementActiveChatsIfCapacityAllows(@Param("agentId") UUID agentId);

    // Atomic decrement query when chat is finished
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AgentStatus a SET a.activeChatsCount = a.activeChatsCount - 1, a.version = a.version + 1 " +
           "WHERE a.agentId = :agentId AND a.activeChatsCount > 0")
    int decrementActiveChats(@Param("agentId") UUID agentId);

    // Atomic reset of capacity for a specific agent (e.g. abrupt disconnect)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AgentStatus a SET a.activeChatsCount = 0, a.isOnline = false, a.version = a.version + 1 " +
           "WHERE a.agentId = :agentId")
    void resetAgentCapacityAndSetOffline(@Param("agentId") UUID agentId);
}
