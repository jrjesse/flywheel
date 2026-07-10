package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.LeadRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("!test")
public class SlaTrackerService {

    private final LeadRepository leadRepository;

    public SlaTrackerService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void checkSlaBreaches() {
        System.out.println("🔍 Verificando SLAs de leads pendentes...");
        List<Lead> pendingLeads = leadRepository.findByStatus("PENDING");
        
        LocalDateTime slaThreshold = LocalDateTime.now().minusMinutes(5);

        for (Lead lead : pendingLeads) {
            if (lead.getCreatedAt().isBefore(slaThreshold)) {
                System.out.println("⚠️ SLA BREACH DETECTADO: O Lead " + lead.getName());
                lead.setStatus("DELAYED"); // Força o Atrasado no Banco!
                leadRepository.save(lead);
            }
        }
    }
}
