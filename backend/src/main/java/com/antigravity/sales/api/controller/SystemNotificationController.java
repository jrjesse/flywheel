package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.SystemNotification;
import com.antigravity.sales.core.repository.SystemNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class SystemNotificationController {

    @Autowired
    private SystemNotificationRepository notificationRepository;

    @GetMapping("/unread")
    public ResponseEntity<List<SystemNotification>> getUnreadNotifications() {
        return ResponseEntity.ok(notificationRepository.findByIsReadFalseOrderByCreatedAtDesc());
    }

    @PostMapping
    public ResponseEntity<SystemNotification> createNotification(@RequestBody SystemNotification request) {
        SystemNotification notification = new SystemNotification();
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setLeadId(request.getLeadId());
        notification.setRead(false);
        return ResponseEntity.ok(notificationRepository.save(notification));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<SystemNotification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notification -> {
            notification.setRead(true);
            return ResponseEntity.ok(notificationRepository.save(notification));
        }).orElse(ResponseEntity.notFound().build());
    }
}
