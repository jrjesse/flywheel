package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndActiveTrue(String email);
    List<User> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
}
