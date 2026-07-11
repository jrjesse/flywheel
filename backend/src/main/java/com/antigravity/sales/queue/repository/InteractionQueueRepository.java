package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InteractionQueueRepository extends JpaRepository<InteractionQueue, UUID> {

    Optional<InteractionQueue> findByLeadIdAndStatusIn(Long leadId, List<InteractionStatus> statuses);

    List<InteractionQueue> findByStatusAndUpdatedAtBefore(InteractionStatus status, Instant thresholdTime);

    List<InteractionQueue> findByAssignedAgentIdAndStatus(UUID agentId, InteractionStatus status);

    @Query("""
            SELECT q FROM InteractionQueue q
            WHERE q.lead.tenantId = :tenantId AND q.status IN :statuses
            ORDER BY q.updatedAt ASC
            """)
    List<InteractionQueue> findActiveByTenantId(
            @Param("tenantId") UUID tenantId,
            @Param("statuses") List<InteractionStatus> statuses);

    @Query("""
            SELECT q FROM InteractionQueue q
            WHERE q.lead.tenantId = :tenantId AND q.status IN :statuses
            AND (q.assignedAgentId = :agentId OR q.assignedAgentId IS NULL)
            ORDER BY q.updatedAt ASC
            """)
    List<InteractionQueue> findActiveByTenantIdForAgent(
            @Param("tenantId") UUID tenantId,
            @Param("agentId") UUID agentId,
            @Param("statuses") List<InteractionStatus> statuses);
}
