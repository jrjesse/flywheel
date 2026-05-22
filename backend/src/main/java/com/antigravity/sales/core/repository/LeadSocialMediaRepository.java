package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.LeadSocialMedia;
import com.antigravity.sales.core.model.SocialMediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadSocialMediaRepository extends JpaRepository<LeadSocialMedia, Long> {
    List<LeadSocialMedia> findByLeadId(Long leadId);
    boolean existsByLeadIdAndType(Long leadId, SocialMediaType type);
}
