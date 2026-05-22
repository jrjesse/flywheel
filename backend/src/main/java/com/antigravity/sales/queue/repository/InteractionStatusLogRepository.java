package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.InteractionStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionStatusLogRepository extends JpaRepository<InteractionStatusLog, UUID> {
    
    List<InteractionStatusLog> findByInteractionIdOrderByTransitionedAtAsc(UUID interactionId);
}
