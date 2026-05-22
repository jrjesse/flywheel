package com.antigravity.sales.messaging.producer;

import com.antigravity.sales.core.model.Lead;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "lead_events";

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLeadReceivedEvent(Lead lead) {
        // Envia o lead inteiro ou um DTO. Para simplicidade enviando a entidade serializada.
        kafkaTemplate.send(TOPIC, lead.getId().toString(), lead);
    }
}
