package com.antigravity.sales.billing.model;

import com.antigravity.sales.security.crypto.AesGcmConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId; // Associado à empresa no nosso sistema

    private String stripeInvoiceId;

    private BigDecimal amount;
    
    private String currency;

    private String status; // DRAFT, OPEN, PAID, UNCOLLECTIBLE, VOID

    // Campos criptografados para LGPD
    @Convert(converter = AesGcmConverter.class)
    private String customerCpfCnpj;

    @Convert(converter = AesGcmConverter.class)
    private String customerAddress;

    // Impostos calculados
    private BigDecimal taxIss;
    private BigDecimal taxPis;
    private BigDecimal taxCofins;
    private BigDecimal taxCsll;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
