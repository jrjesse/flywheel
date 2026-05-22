package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.Lead;
import org.springframework.stereotype.Service;

@Service
public class LeadScoringService {

    public int calculateScore(Lead lead) {
        int score = 0;
        
        // Basic keywords matching using new ProfessionalInfo relation
        if (lead.getProfessionalInfo() != null && lead.getProfessionalInfo().getJobTitle() != null) {
            String role = lead.getProfessionalInfo().getJobTitle().toLowerCase();
            if (role.contains("ceo") || role.contains("diretor") || role.contains("vp") || role.contains("founder")) {
                score += 50;
            } else if (role.contains("gerente") || role.contains("manager")) {
                score += 30;
            } else {
                score += 10;
            }
        }

        if (lead.getCompany() != null && lead.getCompany().getCompanySize() != null) {
            String size = lead.getCompany().getCompanySize().getLabel();
            if (size.contains("51-200") || size.contains("200-500") || size.contains("500+")) {
                score += 40;
            } else {
                score += 20;
            }
        }

        if (lead.getBio() != null && !lead.getBio().isBlank()) {
            score += 10;
        }

        if (lead.getCompany() != null && lead.getCompany().getMrr() != null) {
            java.math.BigDecimal mrr = lead.getCompany().getMrr();
            if (mrr.compareTo(new java.math.BigDecimal("50000")) >= 0) {
                score += 50;
            } else if (mrr.compareTo(new java.math.BigDecimal("10000")) >= 0) {
                score += 30;
            } else if (mrr.compareTo(new java.math.BigDecimal("5000")) >= 0) {
                score += 10;
            }
        }

        return score;
    }
}
