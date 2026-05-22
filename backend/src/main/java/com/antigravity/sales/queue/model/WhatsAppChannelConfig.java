package com.antigravity.sales.queue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "whatsapp_channel_config")
@Data
@NoArgsConstructor
public class WhatsAppChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Tenant / Client identifier
    @Column(nullable = false, unique = true)
    private UUID clientId;

    // Meta API Credentials
    @Column(columnDefinition = "TEXT")
    private String accessToken;
    
    private String phoneNumberId;
    private String wabaId;
    
    @Column(columnDefinition = "TEXT")
    private String appSecret;
    
    private String verifyToken;

    // Queue & Observability Rules
    @Column(nullable = false)
    private Integer debounceSeconds = 45;

    @Column(nullable = false)
    private Integer slaMinutes = 5;

    @Column(nullable = false)
    private Integer maxCapacity = 5;
}
