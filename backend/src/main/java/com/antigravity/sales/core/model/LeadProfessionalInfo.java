package com.antigravity.sales.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lead_professional_info")
@Data
@NoArgsConstructor
public class LeadProfessionalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, unique = true)
    @JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Lead lead;

    @Column(name = "lead_id", insertable = false, updatable = false)
    private Long leadId;

    private String jobTitle;

    @Enumerated(EnumType.STRING)
    private JobFunction jobFunction;

    @Enumerated(EnumType.STRING)
    private SeniorityLevel seniorityLevel;
}
