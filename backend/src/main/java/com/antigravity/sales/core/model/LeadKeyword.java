package com.antigravity.sales.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lead_keywords")
@Data
@NoArgsConstructor
public class LeadKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    @JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Lead lead;

    @Column(name = "lead_id", insertable = false, updatable = false)
    private Long leadId;

    @Column(nullable = false)
    private String keyword;
}
