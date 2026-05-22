package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.LeadInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadInteractionRepository extends JpaRepository<LeadInteraction, Long> {
    List<LeadInteraction> findByLeadIdOrderByTimestampDesc(Long leadId);
}
