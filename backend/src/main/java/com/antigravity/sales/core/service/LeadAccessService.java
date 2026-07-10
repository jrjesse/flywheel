package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.model.User;
import com.antigravity.sales.core.model.UserRole;
import com.antigravity.sales.core.repository.LeadRepository;
import com.antigravity.sales.core.repository.UserRepository;
import com.antigravity.sales.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LeadAccessService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public LeadAccessService(LeadRepository leadRepository, UserRepository userRepository, AuditService auditService) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public boolean isAgentScoped() {
        return TenantContext.hasRole(UserRole.AGENT)
                && !TenantContext.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
    }

    public Optional<UUID> scopedAssigneeFilter() {
        if (isAgentScoped()) {
            return Optional.of(TenantContext.requireUserId());
        }
        return Optional.empty();
    }

    public List<Lead> listUnassignedLeads() {
        UUID tenantId = TenantContext.requireTenantId();
        return leadRepository.findByTenantIdAndAssignedToUserIdIsNull(tenantId);
    }

    public List<Lead> listLeadsForCurrentUser() {
        UUID tenantId = TenantContext.requireTenantId();
        return scopedAssigneeFilter()
                .map(userId -> leadRepository.findByTenantIdAndAssignedToUserId(tenantId, userId))
                .orElseGet(() -> leadRepository.findByTenantId(tenantId));
    }

    public Lead requireAccessibleLead(Long id) {
        UUID tenantId = TenantContext.requireTenantId();
        Lead lead = leadRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (isAgentScoped()) {
            UUID userId = TenantContext.requireUserId();
            if (lead.getAssignedToUserId() == null || !lead.getAssignedToUserId().equals(userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Lead não atribuído a este vendedor");
            }
        }
        return lead;
    }

    @Transactional
    public Lead assignLead(Long leadId, UUID assigneeUserId) {
        Lead lead = leadRepository.findByIdAndTenantId(leadId, TenantContext.requireTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        User assignee = userRepository.findByIdAndTenantId(assigneeUserId, TenantContext.requireTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no tenant"));

        if (!assignee.isActive()) {
            throw new IllegalArgumentException("Usuário destino está inativo");
        }
        if (assignee.getRole() == UserRole.VIEWER) {
            throw new IllegalArgumentException("Não é possível atribuir lead a um usuário VIEWER");
        }

        lead.setAssignedToUserId(assignee.getId());
        Lead saved = leadRepository.save(lead);
        auditService.log("UPDATE", "LEAD_ASSIGN", leadId + "->" + assigneeUserId);
        return saved;
    }

    @Transactional
    public Lead claimLead(Long leadId) {
        if (!TenantContext.hasRole(UserRole.AGENT)) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas vendedores podem assumir leads");
        }

        Lead lead = leadRepository.findByIdAndTenantId(leadId, TenantContext.requireTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (lead.getAssignedToUserId() != null) {
            throw new IllegalArgumentException("Lead já está atribuído a outro vendedor");
        }

        lead.setAssignedToUserId(TenantContext.requireUserId());
        Lead saved = leadRepository.save(lead);
        auditService.log("UPDATE", "LEAD_CLAIM", leadId.toString());
        return saved;
    }

    @Transactional
    public void autoAssignIfUnassigned(Lead lead) {
        if (isAgentScoped() && lead.getAssignedToUserId() == null) {
            lead.setAssignedToUserId(TenantContext.requireUserId());
            leadRepository.save(lead);
            auditService.log("UPDATE", "LEAD_AUTO_ASSIGN", lead.getId().toString());
        }
    }
}
