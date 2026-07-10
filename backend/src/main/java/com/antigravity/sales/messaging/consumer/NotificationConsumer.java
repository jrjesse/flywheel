package com.antigravity.sales.messaging.consumer;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.repository.LeadRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Profile("!test")
public class NotificationConsumer {

    private final LeadRepository leadRepository;

    public NotificationConsumer(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @KafkaListener(topics = "lead_events", groupId = "sales-automation-group")
    public void consumeLeadEvent(Lead lead) {
        System.out.println("Recebido evento para contactar o lead via WhatsApp: " + lead.getName());

        // Simulando a chamada para API do WhatsApp (ex: Twilio / Zenvia / WABA)
        try {
            Thread.sleep(1000); // tempo de processamento
            System.out.println("✅ Mensagem WhatsApp enviada com sucesso para " + lead.getPhone());
            
            // Atualizar o status do lead
            leadRepository.findById(lead.getId()).ifPresent(savedLead -> {
                savedLead.setStatus("CONTACTED");
                savedLead.setContactedAt(LocalDateTime.now());
                leadRepository.save(savedLead);
            });

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar WhatsApp para lead: " + lead.getId());
        }
    }
}
