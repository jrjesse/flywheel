package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.InteractionRequest;
import com.antigravity.sales.api.dto.LeadRequest;
import com.antigravity.sales.api.dto.SocialMediaRequest;
import com.antigravity.sales.core.model.*;
import com.antigravity.sales.core.repository.*;
import com.antigravity.sales.core.service.LeadScoringService;
import com.antigravity.sales.core.service.RevenueParserService;
import com.antigravity.sales.core.service.ProposalPdfService;
import com.antigravity.sales.core.service.EmailService;
import com.antigravity.sales.messaging.producer.NotificationProducer;
import com.antigravity.sales.queue.service.QueueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadRepository leadRepository;
    private final LeadInteractionRepository interactionRepository;
    private final LeadSocialMediaRepository socialMediaRepository;
    private final CompanyRepository companyRepository;
    private final LeadScoringService leadScoringService;
    private final RevenueParserService revenueParserService;
    private final NotificationProducer notificationProducer;
    private final QueueService queueService;
    private final ProposalPdfService proposalPdfService;
    private final EmailService emailService;

    public LeadController(LeadRepository leadRepository, 
                          LeadInteractionRepository interactionRepository,
                          LeadSocialMediaRepository socialMediaRepository,
                          CompanyRepository companyRepository,
                          LeadScoringService leadScoringService,
                          RevenueParserService revenueParserService,
                          NotificationProducer notificationProducer,
                          QueueService queueService,
                          ProposalPdfService proposalPdfService,
                          EmailService emailService) {
        this.leadRepository = leadRepository;
        this.interactionRepository = interactionRepository;
        this.socialMediaRepository = socialMediaRepository;
        this.companyRepository = companyRepository;
        this.leadScoringService = leadScoringService;
        this.revenueParserService = revenueParserService;
        this.notificationProducer = notificationProducer;
        this.queueService = queueService;
        this.proposalPdfService = proposalPdfService;
        this.emailService = emailService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Lead> receiveLead(@RequestBody LeadRequest request) {
        Lead lead = new Lead();
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());

        // Processa Empresa (Company)
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

        // Processa Dados Profissionais (Professional Info)
        LeadProfessionalInfo proInfo = new LeadProfessionalInfo();
        proInfo.setJobTitle(request.getRole());
        proInfo.setLead(lead);
        lead.setProfessionalInfo(proInfo);

        int score = leadScoringService.calculateScore(lead);
        lead.setScore(score);

        Lead savedLead = leadRepository.save(lead);

        // Processa as Redes Sociais que vieram embutidas no Webhook
        if (request.getSocialMedias() != null && !request.getSocialMedias().isEmpty()) {
            for (SocialMediaRequest smReq : request.getSocialMedias()) {
                if (smReq.getType() != null && smReq.getUrl() != null && !smReq.getUrl().isBlank()) {
                    if(!socialMediaRepository.existsByLeadIdAndType(savedLead.getId(), smReq.getType())) {
                        LeadSocialMedia sm = new LeadSocialMedia();
                        sm.setLead(savedLead);
                        sm.setType(smReq.getType());
                        sm.setUrl(smReq.getUrl());
                        socialMediaRepository.save(sm);
                    }
                }
            }
        }

        // notificationProducer.sendLeadReceivedEvent(savedLead);

        return ResponseEntity.ok(savedLead);
    }
    
    @GetMapping
    public ResponseEntity<List<Lead>> getLeads() {
        return ResponseEntity.ok(leadRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Lead>> searchLeads(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) JobFunction jobFunction,
            @RequestParam(required = false) CompanySize size,
            @RequestParam(required = false) java.math.BigDecimal minMrr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int sizePage) {
            
        var spec = com.antigravity.sales.core.repository.LeadSpecification.filterBy(q, companyName, jobFunction, size, minMrr);
        Pageable pageable = PageRequest.of(page, sizePage);
        return ResponseEntity.ok(leadRepository.findAll(spec, pageable));
    }

    // --- Novas Rotas: Kanban e Interações ---

    @PatchMapping("/{id}/status")
    public ResponseEntity<Lead> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return leadRepository.findById(id).map(lead -> {
            String oldStatus = lead.getStatus();
            String newStatus = payload.get("status");
            lead.setStatus(newStatus);
            lead.setContactedAt(LocalDateTime.now());
            Lead updated = leadRepository.save(lead);

            // Gravar auditoria automática
            LeadInteraction audit = new LeadInteraction();
            audit.setLeadId(id);
            audit.setUsername("Sistema / Usuário Kanban");
            audit.setDescription("Status alterado: [" + oldStatus + " -> " + newStatus + "]");
            interactionRepository.save(audit);

            // Integração com a Fila do WhatsApp
            if ("CONTACTED".equals(newStatus)) {
                // Opção 3 + 1: Cria um registro AGUARDANDO_RESPOSTA.
                // Como não temos auth configurado pegando o UUID do usuário atual, passamos nulo
                // ou o UUID do sistema (Robô).
                queueService.registerOutboundInteraction(updated, null, "Mensagem Outbound via Prospector");
            }

            return ResponseEntity.ok(updated);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/interactions")
    public ResponseEntity<LeadInteraction> addInteraction(@PathVariable Long id, @RequestBody InteractionRequest req) {
        LeadInteraction interaction = new LeadInteraction();
        interaction.setLeadId(id);
        interaction.setUsername(req.getUsername() != null ? req.getUsername() : "Atendente");
        interaction.setDescription(req.getDescription());
        
        LeadInteraction saved = interactionRepository.save(interaction);

        // Verifica se há fechamento de venda no texto
        Double extractedRevenue = revenueParserService.extractRevenue(req.getDescription());
        if (extractedRevenue != null) {
            leadRepository.findById(id).ifPresent(lead -> {
                lead.setClosedRevenue(extractedRevenue);
                leadRepository.save(lead);
            });
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/interactions")
    public ResponseEntity<List<LeadInteraction>> getInteractions(@PathVariable Long id) {
        List<LeadInteraction> list = interactionRepository.findByLeadIdOrderByTimestampDesc(id);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/social")
    public ResponseEntity<List<LeadSocialMedia>> getSocialMedia(@PathVariable Long id) {
        return ResponseEntity.ok(socialMediaRepository.findByLeadId(id));
    }

    @PostMapping("/{id}/social")
    public ResponseEntity<?> addSocialMedia(@PathVariable Long id, @RequestBody SocialMediaRequest req) {
        if (socialMediaRepository.existsByLeadIdAndType(id, req.getType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Rede social já cadastrada para este lead"));
        }
        return leadRepository.findById(id).map(lead -> {
            LeadSocialMedia sm = new LeadSocialMedia();
            sm.setLead(lead);
            sm.setType(req.getType());
            sm.setUrl(req.getUrl());
            return ResponseEntity.ok(socialMediaRepository.save(sm));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/social/{socialId}")
    public ResponseEntity<Void> deleteSocialMedia(@PathVariable Long socialId) {
        if (!socialMediaRepository.existsById(socialId)) {
            return ResponseEntity.notFound().build();
        }
        socialMediaRepository.deleteById(socialId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @RequestBody LeadRequest request) {
        return leadRepository.findById(id).map(lead -> {
            if (request.getName() != null) lead.setName(request.getName());
            if (request.getEmail() != null) lead.setEmail(request.getEmail());
            if (request.getPhone() != null) lead.setPhone(request.getPhone());
            
            if (request.getCompanyName() != null) {
                Company company = companyRepository.findByCompanyName(request.getCompanyName()).orElseGet(() -> {
                    Company c = new Company();
                    c.setCompanyName(request.getCompanyName());
                    if (request.getCompanySize() != null) {
                        c.setCompanySize(CompanySize.fromString(request.getCompanySize()));
                    } else {
                        c.setCompanySize(CompanySize.UNKNOWN);
                    }
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

            Lead savedLead = leadRepository.save(lead);
            return ResponseEntity.ok(savedLead);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/interactions/{interactionId}")
    public ResponseEntity<?> updateInteraction(@PathVariable Long id, @PathVariable Long interactionId, @RequestBody InteractionRequest req) {
        return interactionRepository.findById(interactionId).map(interaction -> {
            if (!interaction.getLeadId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Interação não pertence a este lead"));
            }
            if (!"Consultor Comercial".equals(interaction.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Não permitido. Edição apenas dos próprios comentários."));
            }
            if (interaction.getTimestamp().plusMinutes(15).isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "O tempo limite de 15 minutos para edição expirou."));
            }

            interaction.setDescription(req.getDescription());
            return ResponseEntity.ok(interactionRepository.save(interaction));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/interactions/{interactionId}")
    public ResponseEntity<?> deleteInteraction(@PathVariable Long id, @PathVariable Long interactionId) {
        return interactionRepository.findById(interactionId).map(interaction -> {
            if (!interaction.getLeadId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Interação não pertence a este lead"));
            }
            if (!"Consultor Comercial".equals(interaction.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Não permitido. Exclusão apenas dos próprios comentários."));
            }
            if (interaction.getTimestamp().plusMinutes(15).isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "O tempo limite de 15 minutos para exclusão expirou."));
            }

            interactionRepository.delete(interaction);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/proposal")
    public ResponseEntity<?> generateAndSendProposal(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return leadRepository.findById(id).map(lead -> {
            String clientName = payload.containsKey("clientName") ? payload.get("clientName").toString() : lead.getName();
            Double proposalValue = Double.valueOf(payload.getOrDefault("proposalValue", 0.0).toString());

            // 1. Generate PDF
            byte[] pdfBytes = proposalPdfService.generateProposal(clientName, proposalValue);

            // 2. Send Email
            emailService.sendProposalEmail(lead.getEmail(), clientName, pdfBytes);

            // 3. Update Lead Status
            String oldStatus = lead.getStatus();
            lead.setStatus("PROPOSAL_SENT");
            lead.setContactedAt(LocalDateTime.now());
            lead.setClosedRevenue(proposalValue);
            leadRepository.save(lead);

            // 4. Audit Log
            LeadInteraction audit = new LeadInteraction();
            audit.setLeadId(id);
            audit.setUsername("Sistema / Comercial");
            audit.setDescription(String.format("Proposta enviada via PDF. Valor: R$ %,.2f. Status: [%s -> PROPOSAL_SENT]", proposalValue, oldStatus));
            interactionRepository.save(audit);

            return ResponseEntity.ok(Map.of("message", "Proposta enviada com sucesso!", "newStatus", "PROPOSAL_SENT"));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
