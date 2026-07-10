package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    @Query("""
            SELECT n FROM SystemNotification n
            WHERE n.tenantId = :tenantId AND n.isRead = false
            AND (n.targetUserId IS NULL OR n.targetUserId = :userId)
            ORDER BY n.createdAt DESC
            """)
    List<SystemNotification> findUnreadForUser(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);

    Optional<SystemNotification> findByIdAndTenantId(Long id, UUID tenantId);
}
