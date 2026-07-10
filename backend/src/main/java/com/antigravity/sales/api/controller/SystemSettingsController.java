package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.SystemSettingsResponse;
import com.antigravity.sales.core.model.SystemSettings;
import com.antigravity.sales.core.repository.SystemSettingsRepository;
import com.antigravity.sales.core.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/settings/proposals")
public class SystemSettingsController {

    private static final byte[] PDF_MAGIC = new byte[] { 0x25, 0x50, 0x44, 0x46 }; // %PDF

    private final SystemSettingsRepository repository;
    private final AuditService auditService;
    private final Path uploadDir;
    private final long maxFileSizeBytes;

    public SystemSettingsController(
            SystemSettingsRepository repository,
            AuditService auditService,
            @Value("${app.upload.max-file-size-bytes}") long maxFileSizeBytes) throws IOException {
        this.repository = repository;
        this.auditService = auditService;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.uploadDir = Paths.get("uploads/templates").toAbsolutePath();
        Files.createDirectories(uploadDir);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        SystemSettings settings = repository.findById(1L).orElse(new SystemSettings());
        return ResponseEntity.ok(toResponse(settings));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsResponse> updateSettings(@RequestBody SystemSettings newSettings) {
        SystemSettings existing = repository.findById(1L).orElse(new SystemSettings());
        existing.setId(1L);
        if (newSettings.getSmtpHost() != null) existing.setSmtpHost(newSettings.getSmtpHost());
        if (newSettings.getSmtpPort() != null) existing.setSmtpPort(newSettings.getSmtpPort());
        if (newSettings.getSmtpUsername() != null) existing.setSmtpUsername(newSettings.getSmtpUsername());
        if (newSettings.getSmtpPassword() != null && !newSettings.getSmtpPassword().isBlank()) {
            existing.setSmtpPassword(newSettings.getSmtpPassword());
        }
        if (newSettings.getSmtpAuth() != null) existing.setSmtpAuth(newSettings.getSmtpAuth());
        if (newSettings.getSmtpTls() != null) existing.setSmtpTls(newSettings.getSmtpTls());
        if (newSettings.getRevenueGoal() != null) existing.setRevenueGoal(newSettings.getRevenueGoal());
        if (newSettings.getContactGoal() != null) existing.setContactGoal(newSettings.getContactGoal());

        SystemSettings saved = repository.save(existing);
        auditService.log("UPDATE", "SYSTEM_SETTINGS", "1");
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadTemplate(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo vazio");
            }
            if (file.getSize() > maxFileSizeBytes) {
                return ResponseEntity.badRequest().body("Arquivo excede o tamanho máximo permitido");
            }
            if (!isPdf(file)) {
                return ResponseEntity.badRequest().body("Apenas arquivos PDF são permitidos");
            }

            Path targetPath = uploadDir.resolve("Template_Proposta.pdf");
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            SystemSettings settings = repository.findById(1L).orElse(new SystemSettings());
            settings.setTemplateFilePath(targetPath.toString());
            repository.save(settings);
            auditService.log("UPDATE", "PROPOSAL_TEMPLATE", "1");

            return ResponseEntity.ok(toResponse(settings));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo");
        }
    }

    private boolean isPdf(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return false;
        }
        byte[] header = file.getInputStream().readNBytes(4);
        if (header.length < 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (header[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private SystemSettingsResponse toResponse(SystemSettings settings) {
        return SystemSettingsResponse.builder()
                .smtpHost(settings.getSmtpHost())
                .smtpPort(settings.getSmtpPort())
                .smtpUsername(settings.getSmtpUsername())
                .hasSmtpPassword(settings.getSmtpPassword() != null && !settings.getSmtpPassword().isBlank())
                .smtpAuth(settings.getSmtpAuth())
                .smtpTls(settings.getSmtpTls())
                .templateFilePath(settings.getTemplateFilePath())
                .revenueGoal(settings.getRevenueGoal())
                .contactGoal(settings.getContactGoal())
                .build();
    }
}
