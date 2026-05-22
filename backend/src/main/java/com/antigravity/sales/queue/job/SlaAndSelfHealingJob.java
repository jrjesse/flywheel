package com.antigravity.sales.queue.job;

import com.antigravity.sales.queue.model.InteractionQueue;
import com.antigravity.sales.queue.model.InteractionStatus;
import com.antigravity.sales.queue.model.InteractionStatusLog;
import com.antigravity.sales.queue.repository.InteractionQueueRepository;
import com.antigravity.sales.queue.repository.InteractionStatusLogRepository;
import com.antigravity.sales.queue.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SlaAndSelfHealingJob {

    private static final Logger log = LoggerFactory.getLogger(SlaAndSelfHealingJob.class);

    private final InteractionQueueRepository queueRepository;
    private final InteractionStatusLogRepository logRepository;
    private final QueueService queueService;

    public SlaAndSelfHealingJob(InteractionQueueRepository queueRepository, 
                                InteractionStatusLogRepository logRepository,
                                QueueService queueService) {
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.queueService = queueService;
    }

    // Runs every 10 seconds to check for Debounce timeouts
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void selfHealDebounce() {
        // Find interactions in RECEBIDO that haven't been updated in 45 seconds
        Instant threshold = Instant.now().minus(45, ChronoUnit.SECONDS);
        List<InteractionQueue> readyToRoute = queueRepository.findByStatusAndUpdatedAtBefore(InteractionStatus.RECEBIDO, threshold);
        
        for (InteractionQueue interaction : readyToRoute) {
            log.info("Debounce completed for Interaction {}. Routing...", interaction.getId());
            queueService.attemptRoute(interaction);
        }
    }

    // Runs every 1 minute to check SLA of waiting chats
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkSla() {
        // Find interactions in AGUARDANDO_ATENDIMENTO for more than 5 minutes
        Instant threshold = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<InteractionQueue> breached = queueRepository.findByStatusAndUpdatedAtBefore(InteractionStatus.AGUARDANDO_ATENDIMENTO, threshold);
        
        for (InteractionQueue interaction : breached) {
            log.warn("SLA breached for Interaction {}. Moving to TRANSBORDADO.", interaction.getId());
            InteractionStatus oldStatus = interaction.getStatus();
            interaction.setStatus(InteractionStatus.TRANSBORDADO);
            interaction.setUpdatedAt(Instant.now());
            queueRepository.save(interaction);
            
            InteractionStatusLog logEntry = new InteractionStatusLog(interaction, oldStatus, InteractionStatus.TRANSBORDADO, "SLA Breached (5 min)");
            logRepository.save(logEntry);
        }
    }
}
