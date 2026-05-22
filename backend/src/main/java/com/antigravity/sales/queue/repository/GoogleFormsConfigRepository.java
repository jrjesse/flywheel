package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.GoogleFormsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoogleFormsConfigRepository extends JpaRepository<GoogleFormsConfig, UUID> {
    Optional<GoogleFormsConfig> findByClientId(UUID clientId);
    Optional<GoogleFormsConfig> findByWebhookToken(String webhookToken);
}
