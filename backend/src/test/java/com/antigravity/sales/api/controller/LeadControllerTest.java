package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.*;
import com.antigravity.sales.core.service.*;
import com.antigravity.sales.queue.service.QueueService;
import com.antigravity.sales.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private LeadRepository leadRepository;
    @Mock
    private LeadInteractionRepository interactionRepository;
    @Mock
    private LeadSocialMediaRepository socialMediaRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RevenueParserService revenueParserService;
    @Mock
    private QueueService queueService;
    @Mock
    private ProposalPdfService proposalPdfService;
    @Mock
    private EmailService emailService;
    @Mock
    private LeadIngestionService leadIngestionService;
    @Mock
    private LeadAccessService leadAccessService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private LeadController leadController;

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_ID, USER_ID, "test@test.com", "Tester", Set.of(com.antigravity.sales.core.model.UserRole.ADMIN));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSearchLeads() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setName("Search Result");
        Page<Lead> page = new PageImpl<>(Collections.singletonList(lead));

        when(leadAccessService.scopedAssigneeFilter()).thenReturn(java.util.Optional.empty());
        when(leadRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Lead>> response = leadController.searchLeads("Search", null, null, null, null, 0, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Search Result", response.getBody().getContent().get(0).getName());
    }

    @Test
    void testGetLeadsForTenant() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setTenantId(TENANT_ID);
        when(leadAccessService.listLeadsForCurrentUser()).thenReturn(Collections.singletonList(lead));

        ResponseEntity<java.util.List<Lead>> response = leadController.getLeads();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }
}
