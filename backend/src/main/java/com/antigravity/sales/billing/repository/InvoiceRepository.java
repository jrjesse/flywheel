package com.antigravity.sales.billing.repository;

import com.antigravity.sales.billing.model.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    List<InvoiceEntity> findByTenantId(Long tenantId);
    InvoiceEntity findByStripeInvoiceId(String stripeInvoiceId);
}
