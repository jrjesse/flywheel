package com.antigravity.sales.security;

import com.antigravity.sales.core.model.TenantWebhookSecret;
import com.antigravity.sales.core.repository.TenantWebhookSecretRepository;
import com.antigravity.sales.queue.model.GoogleFormsConfig;
import com.antigravity.sales.security.crypto.WebhookTokenHasher;
import com.antigravity.sales.queue.repository.GoogleFormsConfigRepository;
import com.antigravity.sales.queue.repository.WhatsAppChannelConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebhookAuthService {

    private static final String LEAD_SECRET_HEADER = "X-Webhook-Secret";
    private static final String META_SIGNATURE_HEADER = "X-Hub-Signature-256";

    private final TenantWebhookSecretRepository webhookSecretRepository;
    private final WhatsAppChannelConfigRepository whatsAppConfigRepository;
    private final GoogleFormsConfigRepository googleFormsConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebhookTokenHasher tokenHasher;

    public WebhookAuthService(
            TenantWebhookSecretRepository webhookSecretRepository,
            WhatsAppChannelConfigRepository whatsAppConfigRepository,
            GoogleFormsConfigRepository googleFormsConfigRepository,
            PasswordEncoder passwordEncoder,
            WebhookTokenHasher tokenHasher) {
        this.webhookSecretRepository = webhookSecretRepository;
        this.whatsAppConfigRepository = whatsAppConfigRepository;
        this.googleFormsConfigRepository = googleFormsConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenHasher = tokenHasher;
    }

    public Optional<UUID> resolveTenantFromLeadWebhook(HttpServletRequest request) {
        String secret = request.getHeader(LEAD_SECRET_HEADER);
        if (secret == null || secret.isBlank()) {
            return Optional.empty();
        }
        return webhookSecretRepository.findAll().stream()
                .filter(entry -> passwordEncoder.matches(secret, entry.getSecretHash()))
                .map(TenantWebhookSecret::getTenantId)
                .findFirst();
    }

    public boolean validateWhatsAppSignature(HttpServletRequest request, String payload, UUID tenantId) {
        String signatureHeader = request.getHeader(META_SIGNATURE_HEADER);
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        String expectedPrefix = "sha256=";
        String provided = signatureHeader.substring(expectedPrefix.length());

        return whatsAppConfigRepository.findByClientId(tenantId)
                .map(config -> {
                    if (config.getAppSecret() == null) {
                        return false;
                    }
                    try {
                        Mac mac = Mac.getInstance("HmacSHA256");
                        mac.init(new SecretKeySpec(config.getAppSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                        String computed = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
                        return constantTimeEquals(computed, provided);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    public Optional<UUID> resolveTenantFromGoogleFormsToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return googleFormsConfigRepository.findByWebhookTokenHash(tokenHasher.hash(token))
                .filter(GoogleFormsConfig::isActive)
                .map(GoogleFormsConfig::getClientId);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
