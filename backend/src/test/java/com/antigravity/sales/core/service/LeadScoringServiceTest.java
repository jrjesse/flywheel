package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.Company;
import com.antigravity.sales.core.model.CompanySize;
import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.model.LeadProfessionalInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadScoringServiceTest {

    private LeadScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new LeadScoringService();
    }

    @Test
    void testScoreForEnterpriseWhale() {
        // Setup Whale Lead (CTO, Enterprise, High MRR, with Bio)
        Lead lead = new Lead();
        lead.setBio("Great icebreaker");

        Company company = new Company();
        company.setCompanySize(CompanySize.ENTERPRISE); // 500+
        company.setMrr(new BigDecimal("125000")); // >= 50k
        lead.setCompany(company);

        LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
        proInfo.setJobTitle("CTO e Founder");
        lead.setProfessionalInfo(proInfo);

        // Calculate
        // Expected: 
        // founder -> 50
        // ENTERPRISE -> 40
        // bio -> 10
        // MRR >= 50k -> 50
        // Total = 150
        int score = scoringService.calculateScore(lead);
        assertEquals(150, score, "Enterprise Whale should score 150");
    }

    @Test
    void testScoreForSmallBusiness() {
        // Setup Small Business
        Lead lead = new Lead();
        lead.setBio(""); // blank bio

        Company company = new Company();
        company.setCompanySize(CompanySize.MICRO); // 1-10
        company.setMrr(new BigDecimal("2000")); // < 5000
        lead.setCompany(company);

        LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
        proInfo.setJobTitle("Analista de Vendas"); // Others
        lead.setProfessionalInfo(proInfo);

        // Expected:
        // Others -> 10
        // MICRO -> 20
        // bio blank -> 0
        // MRR < 5000 -> 0
        // Total = 30
        int score = scoringService.calculateScore(lead);
        assertEquals(30, score, "Small business analyst should score 30");
    }

    @Test
    void testScoreWithMidMarketManager() {
        Lead lead = new Lead();
        
        Company company = new Company();
        company.setCompanySize(CompanySize.MEDIUM); // 51-200
        company.setMrr(new BigDecimal("15000")); // >= 10k
        lead.setCompany(company);

        LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
        proInfo.setJobTitle("Gerente de Projetos"); // gerente
        lead.setProfessionalInfo(proInfo);

        // Expected:
        // gerente -> 30
        // MEDIUM -> 40
        // bio null -> 0
        // MRR >= 10k -> 30
        // Total = 100
        int score = scoringService.calculateScore(lead);
        assertEquals(100, score, "Mid-market manager should score 100");
    }

    @Test
    void testScoreWithNulls() {
        Lead lead = new Lead();
        // everything null, score should be 0
        int score = scoringService.calculateScore(lead);
        assertEquals(0, score, "Completely empty lead should score 0");
    }
}
