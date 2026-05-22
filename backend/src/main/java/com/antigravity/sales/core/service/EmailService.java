package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.SystemSettings;
import com.antigravity.sales.core.repository.SystemSettingsRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

@Service
public class EmailService {

    private final TemplateEngine templateEngine;
    private final SystemSettingsRepository settingsRepository;

    public EmailService(TemplateEngine templateEngine, SystemSettingsRepository settingsRepository) {
        this.templateEngine = templateEngine;
        this.settingsRepository = settingsRepository;
    }

    public void sendProposalEmail(String toEmail, String clientName, byte[] pdfBytes) {
        SystemSettings settings = settingsRepository.findById(1L).orElse(new SystemSettings());

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getSmtpHost() != null ? settings.getSmtpHost() : "localhost");
        mailSender.setPort(settings.getSmtpPort() != null ? settings.getSmtpPort() : 1025);
        
        if (settings.getSmtpUsername() != null && !settings.getSmtpUsername().isEmpty()) {
            mailSender.setUsername(settings.getSmtpUsername());
            mailSender.setPassword(settings.getSmtpPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", settings.getSmtpAuth() != null ? settings.getSmtpAuth().toString() : "false");
        props.put("mail.smtp.starttls.enable", settings.getSmtpTls() != null ? settings.getSmtpTls().toString() : "false");
        props.put("mail.debug", "true");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(settings.getSmtpUsername() != null && !settings.getSmtpUsername().isEmpty() ? settings.getSmtpUsername() : "financeiro@nomedaempresa.com.br");
            helper.setTo(toEmail);
            helper.setSubject("Proposta Comercial - Ring Tecnologia");

            Context context = new Context();
            context.setVariable("clientName", clientName);
            String htmlContent = templateEngine.process("proposal-email", context);

            helper.setText(htmlContent, true);
            helper.addAttachment("Proposta_Comercial.pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("E-mail gerado com sucesso (simulado ou enviado).");
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao enviar e-mail. Verifique se o servidor SMTP (ex: MailHog na porta 1025) está rodando. Erro: " + e.getMessage());
            // Não lança exceção para não quebrar o fluxo (Kanban/Proposta) no ambiente local
        }
    }
}
