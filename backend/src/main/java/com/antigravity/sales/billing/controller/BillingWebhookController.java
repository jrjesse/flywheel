package com.antigravity.sales.billing.controller;

import com.antigravity.sales.billing.model.InvoiceEntity;
import com.antigravity.sales.billing.repository.InvoiceRepository;
import com.antigravity.sales.billing.service.TaxCalculationService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class BillingWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TaxCalculationService taxCalculationService;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Trata apenas eventos de Invoice por enquanto
        if ("invoice.paid".equals(event.getType())) {
            Invoice stripeInvoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            
            if (stripeInvoice != null) {
                // Aqui criamos ou atualizamos a fatura local
                InvoiceEntity invoiceEntity = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId());
                if (invoiceEntity == null) {
                    invoiceEntity = new InvoiceEntity();
                    invoiceEntity.setStripeInvoiceId(stripeInvoice.getId());
                    // O valor vem em centavos, então dividimos por 100
                    invoiceEntity.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountPaid() / 100.0));
                    invoiceEntity.setCurrency(stripeInvoice.getCurrency());
                }
                
                invoiceEntity.setStatus("PAID");
                
                // Calcula os impostos
                taxCalculationService.calculateTaxes(invoiceEntity);
                
                // Salva a fatura e prepara os dados para a Nota Fiscal (fictício)
                invoiceRepository.save(invoiceEntity);
            }
        }

        return ResponseEntity.ok("Success");
    }
}
