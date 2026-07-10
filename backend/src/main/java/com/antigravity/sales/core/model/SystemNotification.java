package com.antigravity.sales.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_notifications")
@Data
@NoArgsConstructor
public class SystemNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID tenantId;

    private UUID targetUserId;

    private String message;
    private String type;
    private Long leadId;
    
    @Column(name = "is_read")
    private boolean isRead;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
