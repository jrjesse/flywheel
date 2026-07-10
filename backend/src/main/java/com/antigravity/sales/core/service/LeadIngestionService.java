package com.antigravity.sales.core.service;

import com.antigravity.sales.api.dto.LeadIngestionRequest;
import com.antigravity.sales.api.dto.SocialMediaRequest;
import com.antigravity.sales.core.model.*;
import com.antigravity.sales.core.repository.*;
import com.antigravity.sales.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LeadIngestionService {

    private final LeadRepository leadRepository;
    private final LeadSocialMediaRepository socialMediaRepository;
    private final CompanyRepository companyRepository;
    private final LeadScoringService leadScoringService;
    private final LeadAccessService leadAccessService;

    public LeadIngestionService(
            LeadRepository leadRepository,
            LeadSocialMediaRepository socialMediaRepository,
            CompanyRepository companyRepository,
            LeadScoringService leadScoringService,
            LeadAccessService leadAccessService) {
        this.leadRepository = leadRepository;
        this.socialMediaRepository = socialMediaRepository;
        this.companyRepository = companyRepository;
        this.leadScoringService = leadScoringService;
        this.leadAccessService = leadAccessService;
    }

    @Transactional
    public Lead ingest(LeadIngestionRequest request, UUID tenantId) {
        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());

        String cName = request.getCompanyName() != null ? request.getCompanyName() : "Empresa de " + request.getName();
        Company company = companyRepository.findByCompanyName(cName).orElseGet(() -> {
            Company c = new Company();
            c.setCompanyName(cName);
            c.setCompanySize(CompanySize.fromString(request.getCompanySize()));
            return c;
        });
        if (request.getMrr() != null) {
            company.setMrr(request.getMrr());
        }
        lead.setCompany(company);

        if (request.getBio() != null) {
            lead.setBio(request.getBio());
        }
        if (request.getSource() != null) {
            lead.setSource(request.getSource());
        }

        LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
        proInfo.setJobTitle(request.getRole());
        proInfo.setLead(lead);
        lead.setProfessionalInfo(proInfo);

        lead.setScore(leadScoringService.calculateScore(lead));
        Lead savedLead = leadRepository.save(lead);

        if (request.getSocialMedias() != null) {
            for (SocialMediaRequest smReq : request.getSocialMedias()) {
                if (smReq.getType() != null && smReq.getUrl() != null && !smReq.getUrl().isBlank()) {
                    if (!socialMediaRepository.existsByLeadIdAndType(savedLead.getId(), smReq.getType())) {
                        LeadSocialMedia sm = new LeadSocialMedia();
                        sm.setLead(savedLead);
                        sm.setType(smReq.getType());
                        sm.setUrl(smReq.getUrl());
                        socialMediaRepository.save(sm);
                    }
                }
            }
        }
        return savedLead;
    }

    public Lead requireLeadForTenant(Long id) {
        return leadAccessService.requireAccessibleLead(id);
    }
}
