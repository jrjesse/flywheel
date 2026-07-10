package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.TenantWebhookSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantWebhookSecretRepository extends JpaRepository<TenantWebhookSecret, UUID> {
    Optional<TenantWebhookSecret> findByTenantId(UUID tenantId);
}
