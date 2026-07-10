package com.antigravity.sales.queue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "google_forms_config")
@Data
@NoArgsConstructor
public class GoogleFormsConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Tenant / Client identifier
    @Column(nullable = false, unique = true)
    private UUID clientId;

    // SHA-256 hash of webhook token for lookup (token shown only once on creation)
    @Column(nullable = false, unique = true)
    private String webhookTokenHash;
    
    private String formName;
    
    private boolean active = true;
}
