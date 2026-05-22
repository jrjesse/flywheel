package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
