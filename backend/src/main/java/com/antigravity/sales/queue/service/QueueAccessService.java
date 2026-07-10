package com.antigravity.sales.queue.service;

import com.antigravity.sales.api.dto.QueueInteractionResponse;
import com.antigravity.sales.core.model.UserRole;
import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import com.antigravity.sales.queue.repository.InteractionQueueRepository;
import com.antigravity.sales.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QueueAccessService {

    private final InteractionQueueRepository queueRepository;

    public QueueAccessService(InteractionQueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public boolean isAgentScoped() {
        return TenantContext.hasRole(UserRole.AGENT)
                && !TenantContext.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
    }

    public List<QueueInteractionResponse> listInteractionsForCurrentUser() {
        UUID tenantId = TenantContext.requireTenantId();
        List<InteractionQueue> items;

        if (isAgentScoped()) {
            UUID userId = TenantContext.requireUserId();
            items = queueRepository.findActiveByTenantIdForAgent(
                    tenantId, userId, List.of(
                            InteractionStatus.AGUARDANDO_ATENDIMENTO,
                            InteractionStatus.EM_ATENDIMENTO,
                            InteractionStatus.TRANSBORDADO,
                            InteractionStatus.RECEBIDO));
        } else {
            items = queueRepository.findActiveByTenantId(
                    tenantId, List.of(
                            InteractionStatus.AGUARDANDO_ATENDIMENTO,
                            InteractionStatus.EM_ATENDIMENTO,
                            InteractionStatus.TRANSBORDADO,
                            InteractionStatus.RECEBIDO));
        }

        return items.stream().map(this::toResponse).toList();
    }

    private QueueInteractionResponse toResponse(InteractionQueue q) {
        return new QueueInteractionResponse(
                q.getId(),
                q.getLead().getId(),
                q.getLead().getName(),
                q.getStatus().name(),
                q.getChannel(),
                q.getAssignedAgentId(),
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }
}
