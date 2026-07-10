package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {
    List<Lead> findByStatus(String status);
    List<Lead> findByTenantId(UUID tenantId);
    List<Lead> findByTenantIdAndAssignedToUserId(UUID tenantId, UUID assignedToUserId);
    List<Lead> findByTenantIdAndAssignedToUserIdIsNull(UUID tenantId);
    Optional<Lead> findByIdAndTenantId(Long id, UUID tenantId);
}
