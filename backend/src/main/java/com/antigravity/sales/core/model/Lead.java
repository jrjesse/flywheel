package com.antigravity.sales.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID tenantId;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private String locationCountry;
    private String locationRegion;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(mappedBy = "lead", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("lead")
    private LeadProfessionalInfo professionalInfo;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("lead")
    private List<LeadKeyword> keywords;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("lead")
    private List<LeadSocialMedia> socialMedias;

    private Integer score;
    private String status; 
    private String source;
    private Double closedRevenue;
    private UUID assignedToUserId;
    private LocalDateTime createdAt;
    private LocalDateTime contactedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if(status == null) status = "PROSPECT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
