package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findByTenantIdAndIsReadFalseOrderByCreatedAtDesc(UUID tenantId);
    Optional<SystemNotification> findByIdAndTenantId(Long id, UUID tenantId);
}
