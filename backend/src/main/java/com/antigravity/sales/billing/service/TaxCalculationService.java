package com.antigravity.sales.billing.service;

import com.antigravity.sales.billing.model.InvoiceEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxCalculationService {

    // Alíquotas simplificadas para Simples Nacional (exemplo) ou Lucro Presumido
    // Na vida real, isso dependeria da alíquota da cidade, faixa de faturamento, etc.
    private static final BigDecimal ISS_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal PIS_RATE = new BigDecimal("0.0065"); // 0.65%
    private static final BigDecimal COFINS_RATE = new BigDecimal("0.03"); // 3%
    private static final BigDecimal CSLL_RATE = new BigDecimal("0.01"); // 1%

    /**
     * Calcula os impostos de uma Invoice baseada no valor (amount).
     * @param invoice a fatura que precisa de cálculo
     */
    public void calculateTaxes(InvoiceEntity invoice) {
        if (invoice == null || invoice.getAmount() == null) {
            return;
        }

        BigDecimal amount = invoice.getAmount();

        invoice.setTaxIss(amount.multiply(ISS_RATE).setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxPis(amount.multiply(PIS_RATE).setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxCofins(amount.multiply(COFINS_RATE).setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxCsll(amount.multiply(CSLL_RATE).setScale(2, RoundingMode.HALF_UP));
    }
}
