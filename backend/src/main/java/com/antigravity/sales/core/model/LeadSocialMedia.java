package com.antigravity.sales.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "lead_social_media", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"lead_id", "type"})
)
@Data
@NoArgsConstructor
public class LeadSocialMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Lead lead;

    @Column(name = "lead_id", insertable = false, updatable = false)
    private Long leadId; // Expose the id directly for UI convenience without fetching full object

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialMediaType type;

    @Column(nullable = false)
    private String url;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
