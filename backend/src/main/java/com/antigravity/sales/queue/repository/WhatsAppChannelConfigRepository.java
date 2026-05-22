package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.WhatsAppChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhatsAppChannelConfigRepository extends JpaRepository<WhatsAppChannelConfig, UUID> {
    Optional<WhatsAppChannelConfig> findByClientId(UUID clientId);
}
