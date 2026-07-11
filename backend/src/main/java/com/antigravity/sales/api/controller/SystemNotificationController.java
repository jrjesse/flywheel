package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.SystemNotification;
import com.antigravity.sales.core.repository.SystemNotificationRepository;
import com.antigravity.sales.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class SystemNotificationController {

    private final SystemNotificationRepository notificationRepository;

    public SystemNotificationController(SystemNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<List<SystemNotification>> getUnreadNotifications() {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.requireUserId();
        return ResponseEntity.ok(notificationRepository.findUnreadForUser(tenantId, userId));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<SystemNotification> markAsRead(@PathVariable Long id) {
        UUID tenantId = TenantContext.requireTenantId();
        return notificationRepository.findByIdAndTenantId(id, tenantId).map(notification -> {
            notification.setRead(true);
            return ResponseEntity.ok(notificationRepository.save(notification));
        }).orElse(ResponseEntity.notFound().build());
    }
}
