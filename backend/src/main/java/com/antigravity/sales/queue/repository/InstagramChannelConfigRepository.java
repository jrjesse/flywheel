package com.antigravity.sales.queue.repository;

import com.antigravity.sales.queue.model.InstagramChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstagramChannelConfigRepository extends JpaRepository<InstagramChannelConfig, UUID> {
    Optional<InstagramChannelConfig> findByClientId(UUID clientId);
}
