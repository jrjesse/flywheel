package com.antigravity.sales.queue.model;

import com.antigravity.sales.security.crypto.AesGcmConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "instagram_channel_config")
@Data
@NoArgsConstructor
public class InstagramChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Tenant / Client identifier
    @Column(nullable = false, unique = true)
    private UUID clientId;

    // Meta API Credentials
    @Convert(converter = AesGcmConverter.class)
    @Column(columnDefinition = "TEXT")
    private String accessToken;
    
    private String instagramAccountId;
    private String pageId;
    
    @Convert(converter = AesGcmConverter.class)
    @Column(columnDefinition = "TEXT")
    private String appSecret;
    
    @Convert(converter = AesGcmConverter.class)
    private String verifyToken;
    
    private boolean active = true;
}
