package com.antigravity.sales.billing.service;

import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeIntegrationService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Cria um Customer no Stripe contendo apenas dados permitidos pela LGPD.
     * Dados sensíveis como CPF ficam apenas no banco local, enquanto no Stripe vai o ID do tenant local e e-mail.
     */
    public Customer createCustomer(Long tenantId, String email, String name) {
        try {
            // Utilizamos metadata para mapear com o tenant local
            Map<String, String> metadata = new HashMap<>();
            metadata.put("tenantId", tenantId.toString());

            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .putAllMetadata(metadata)
                    .build();

            return Customer.create(params);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar Customer no Stripe", e);
        }
    }
}
