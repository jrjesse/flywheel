package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.LeadKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadKeywordRepository extends JpaRepository<LeadKeyword, Long> {
    List<LeadKeyword> findByLeadId(Long leadId);
}
