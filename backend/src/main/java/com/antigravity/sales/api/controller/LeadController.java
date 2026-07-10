package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.AssignLeadRequest;
import com.antigravity.sales.api.dto.InteractionRequest;
import com.antigravity.sales.api.dto.LeadRequest;
import com.antigravity.sales.api.dto.SocialMediaRequest;
import com.antigravity.sales.core.model.*;
import com.antigravity.sales.core.repository.*;
import com.antigravity.sales.core.service.*;
import com.antigravity.sales.queue.service.QueueService;
import com.antigravity.sales.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadRepository leadRepository;
    private final LeadInteractionRepository interactionRepository;
    private final LeadSocialMediaRepository socialMediaRepository;
    private final CompanyRepository companyRepository;
    private final RevenueParserService revenueParserService;
    private final QueueService queueService;
    private final ProposalPdfService proposalPdfService;
    private final EmailService emailService;
    private final LeadIngestionService leadIngestionService;
    private final LeadAccessService leadAccessService;
    private final AuditService auditService;

    public LeadController(LeadRepository leadRepository,
                          LeadInteractionRepository interactionRepository,
                          LeadSocialMediaRepository socialMediaRepository,
                          CompanyRepository companyRepository,
                          RevenueParserService revenueParserService,
                          QueueService queueService,
                          ProposalPdfService proposalPdfService,
                          EmailService emailService,
                          LeadIngestionService leadIngestionService,
                          LeadAccessService leadAccessService,
                          AuditService auditService) {
        this.leadRepository = leadRepository;
        this.interactionRepository = interactionRepository;
        this.socialMediaRepository = socialMediaRepository;
        this.companyRepository = companyRepository;
        this.revenueParserService = revenueParserService;
        this.queueService = queueService;
        this.proposalPdfService = proposalPdfService;
        this.emailService = emailService;
        this.leadIngestionService = leadIngestionService;
        this.leadAccessService = leadAccessService;
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','VIEWER')")
    public ResponseEntity<List<Lead>> getLeads() {
        auditService.log("READ", "LEAD", "list");
        return ResponseEntity.ok(leadAccessService.listLeadsForCurrentUser());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','VIEWER')")
    public ResponseEntity<Page<Lead>> searchLeads(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) JobFunction jobFunction,
            @RequestParam(required = false) CompanySize size,
            @RequestParam(required = false) java.math.BigDecimal minMrr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int sizePage) {

        UUID tenantId = TenantContext.requireTenantId();
        UUID assignedFilter = leadAccessService.scopedAssigneeFilter().orElse(null);
        var spec = LeadSpecification.filterBy(tenantId, assignedFilter, q, companyName, jobFunction, size, minMrr);
        Pageable pageable = PageRequest.of(page, sizePage);
        auditService.log("READ", "LEAD", "search");
        return ResponseEntity.ok(leadRepository.findAll(spec, pageable));
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<List<Lead>> getUnassignedLeads() {
        auditService.log("READ", "LEAD", "unassigned");
        return ResponseEntity.ok(leadAccessService.listUnassignedLeads());
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Lead> assignLead(@PathVariable Long id, @Valid @RequestBody AssignLeadRequest request) {
        Lead assigned = leadAccessService.assignLead(id, request.getUserId());
        return ResponseEntity.ok(assigned);
    }

    @PatchMapping("/{id}/claim")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Lead> claimLead(@PathVariable Long id) {
        Lead claimed = leadAccessService.claimLead(id);
        return ResponseEntity.ok(claimed);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<Lead> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Lead lead = leadIngestionService.requireLeadForTenant(id);
        leadAccessService.autoAssignIfUnassigned(lead);
        String oldStatus = lead.getStatus();
        String newStatus = payload.get("status");
        lead.setStatus(newStatus);
        lead.setContactedAt(LocalDateTime.now());
        Lead updated = leadRepository.save(lead);

        LeadInteraction audit = new LeadInteraction();
        audit.setLeadId(id);
        audit.setTenantId(TenantContext.requireTenantId());
        audit.setCreatedByUserId(TenantContext.requireUserId());
        audit.setUsername(TenantContext.getDisplayName().orElse("Sistema"));
        audit.setDescription("Status alterado: [" + oldStatus + " -> " + newStatus + "]");
        interactionRepository.save(audit);

        if ("CONTACTED".equals(newStatus)) {
            queueService.registerOutboundInteraction(updated, TenantContext.getUserId().orElse(null), "Mensagem Outbound via Prospector");
        }

        auditService.log("UPDATE", "LEAD", id.toString());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/interactions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<LeadInteraction> addInteraction(@PathVariable Long id, @Valid @RequestBody InteractionRequest req) {
        Lead lead = leadIngestionService.requireLeadForTenant(id);
        leadAccessService.autoAssignIfUnassigned(lead);

        LeadInteraction interaction = new LeadInteraction();
        interaction.setLeadId(id);
        interaction.setTenantId(TenantContext.requireTenantId());
        interaction.setCreatedByUserId(TenantContext.requireUserId());
        interaction.setUsername(TenantContext.getDisplayName().orElse("Atendente"));
        interaction.setDescription(req.getDescription());

        LeadInteraction saved = interactionRepository.save(interaction);

        Double extractedRevenue = revenueParserService.extractRevenue(req.getDescription());
        if (extractedRevenue != null) {
            lead.setClosedRevenue(extractedRevenue);
            leadRepository.save(lead);
        }

        auditService.log("CREATE", "INTERACTION", saved.getId().toString());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/interactions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','VIEWER')")
    public ResponseEntity<List<LeadInteraction>> getInteractions(@PathVariable Long id) {
        leadIngestionService.requireLeadForTenant(id);
        return ResponseEntity.ok(interactionRepository.findByLeadIdOrderByTimestampDesc(id));
    }

    @GetMapping("/{id}/social")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','VIEWER')")
    public ResponseEntity<List<LeadSocialMedia>> getSocialMedia(@PathVariable Long id) {
        leadIngestionService.requireLeadForTenant(id);
        return ResponseEntity.ok(socialMediaRepository.findByLeadId(id));
    }

    @PostMapping("/{id}/social")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<?> addSocialMedia(@PathVariable Long id, @RequestBody SocialMediaRequest req) {
        leadIngestionService.requireLeadForTenant(id);
        if (socialMediaRepository.existsByLeadIdAndType(id, req.getType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Rede social já cadastrada para este lead"));
        }
        return leadRepository.findByIdAndTenantId(id, TenantContext.requireTenantId()).map(lead -> {
            LeadSocialMedia sm = new LeadSocialMedia();
            sm.setLead(lead);
            sm.setType(req.getType());
            sm.setUrl(req.getUrl());
            return ResponseEntity.ok(socialMediaRepository.save(sm));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/social/{socialId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<Void> deleteSocialMedia(@PathVariable Long socialId) {
        if (!socialMediaRepository.existsById(socialId)) {
            return ResponseEntity.notFound().build();
        }
        socialMediaRepository.deleteById(socialId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        Lead lead = leadIngestionService.requireLeadForTenant(id);
        if (request.getName() != null) lead.setName(request.getName());
            if (request.getEmail() != null) lead.setEmail(request.getEmail());
            if (request.getPhone() != null) lead.setPhone(request.getPhone());

            if (request.getCompanyName() != null) {
                Company company = companyRepository.findByCompanyName(request.getCompanyName()).orElseGet(() -> {
                    Company c = new Company();
                    c.setCompanyName(request.getCompanyName());
                    c.setCompanySize(request.getCompanySize() != null
                            ? CompanySize.fromString(request.getCompanySize())
                            : CompanySize.UNKNOWN);
                    return companyRepository.save(c);
                });
                lead.setCompany(company);
            }

            if (request.getRole() != null) {
                if (lead.getProfessionalInfo() != null) {
                    lead.getProfessionalInfo().setJobTitle(request.getRole());
                } else {
                    LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
                    proInfo.setJobTitle(request.getRole());
                    proInfo.setLead(lead);
                    lead.setProfessionalInfo(proInfo);
                }
            }

            auditService.log("UPDATE", "LEAD", id.toString());
            return ResponseEntity.ok(leadRepository.save(lead));
    }

    @PutMapping("/{id}/interactions/{interactionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<?> updateInteraction(@PathVariable Long id, @PathVariable Long interactionId, @Valid @RequestBody InteractionRequest req) {
        return interactionRepository.findById(interactionId).map(interaction -> {
            if (!interaction.getLeadId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Interação não pertence a este lead"));
            }
            if (!TenantContext.requireUserId().equals(interaction.getCreatedByUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Não permitido. Edição apenas dos próprios comentários."));
            }
            if (interaction.getTimestamp().plusMinutes(15).isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "O tempo limite de 15 minutos para edição expirou."));
            }

            interaction.setDescription(req.getDescription());
            auditService.log("UPDATE", "INTERACTION", interactionId.toString());
            return ResponseEntity.ok(interactionRepository.save(interaction));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/interactions/{interactionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<?> deleteInteraction(@PathVariable Long id, @PathVariable Long interactionId) {
        return interactionRepository.findById(interactionId).map(interaction -> {
            if (!interaction.getLeadId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Interação não pertence a este lead"));
            }
            if (!TenantContext.requireUserId().equals(interaction.getCreatedByUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Não permitido. Exclusão apenas dos próprios comentários."));
            }
            if (interaction.getTimestamp().plusMinutes(15).isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "O tempo limite de 15 minutos para exclusão expirou."));
            }

            interactionRepository.delete(interaction);
            auditService.log("DELETE", "INTERACTION", interactionId.toString());
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/proposal")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> generateAndSendProposal(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Lead lead = leadIngestionService.requireLeadForTenant(id);
        String clientName = payload.containsKey("clientName") ? payload.get("clientName").toString() : lead.getName();
            Double proposalValue = Double.valueOf(payload.getOrDefault("proposalValue", 0.0).toString());

            byte[] pdfBytes = proposalPdfService.generateProposal(clientName, proposalValue);
            emailService.sendProposalEmail(lead.getEmail(), clientName, pdfBytes);

            String oldStatus = lead.getStatus();
            lead.setStatus("PROPOSAL_SENT");
            lead.setContactedAt(LocalDateTime.now());
            lead.setClosedRevenue(proposalValue);
            leadRepository.save(lead);

            LeadInteraction audit = new LeadInteraction();
            audit.setLeadId(id);
            audit.setTenantId(TenantContext.requireTenantId());
            audit.setCreatedByUserId(TenantContext.requireUserId());
            audit.setUsername(TenantContext.getDisplayName().orElse("Sistema"));
            audit.setDescription(String.format("Proposta enviada via PDF. Valor: R$ %,.2f. Status: [%s -> PROPOSAL_SENT]", proposalValue, oldStatus));
            interactionRepository.save(audit);

            auditService.log("CREATE", "PROPOSAL", id.toString());
            return ResponseEntity.ok(Map.of("message", "Proposta enviada com sucesso!", "newStatus", "PROPOSAL_SENT"));
    }
}
