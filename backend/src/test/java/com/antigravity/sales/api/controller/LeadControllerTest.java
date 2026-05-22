package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.LeadRequest;
import com.antigravity.sales.core.model.Company;
import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.CompanyRepository;
import com.antigravity.sales.core.repository.LeadInteractionRepository;
import com.antigravity.sales.core.repository.LeadRepository;
import com.antigravity.sales.core.repository.LeadSocialMediaRepository;
import com.antigravity.sales.core.service.LeadScoringService;
import com.antigravity.sales.messaging.producer.NotificationProducer;
import com.antigravity.sales.queue.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadInteractionRepository interactionRepository;

    @Mock
    private LeadSocialMediaRepository socialMediaRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private LeadScoringService leadScoringService;

    @Mock
    private QueueService queueService;

    @InjectMocks
    private LeadController leadController;

    @Test
    void testWebhookIngestionSuccess() {
        LeadRequest request = new LeadRequest();
        request.setName("John Doe");
        request.setEmail("john@acme.com");
        request.setCompanyName("Acme Corp");
        request.setCompanySize("MEDIUM");
        request.setMrr(new BigDecimal("15000"));
        request.setBio("Icebreaker test");
        request.setRole("CEO");

        Company mockedCompany = new Company();
        mockedCompany.setCompanyName("Acme Corp");

        Lead savedLead = new Lead();
        savedLead.setId(1L);
        savedLead.setName("John Doe");
        savedLead.setScore(100);

        when(companyRepository.findByCompanyName("Acme Corp")).thenReturn(Optional.of(mockedCompany));
        when(leadScoringService.calculateScore(any(Lead.class))).thenReturn(100);
        when(leadRepository.save(any(Lead.class))).thenReturn(savedLead);

        ResponseEntity<Lead> response = leadController.receiveLead(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testSearchLeads() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setName("Search Result");
        Page<Lead> page = new PageImpl<>(Collections.singletonList(lead));

        when(leadRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Lead>> response = leadController.searchLeads("Search", null, null, null, null, 0, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Search Result", response.getBody().getContent().get(0).getName());
    }
}
