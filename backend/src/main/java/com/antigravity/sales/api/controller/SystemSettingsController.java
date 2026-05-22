package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.SystemSettings;
import com.antigravity.sales.core.repository.SystemSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/settings/proposals")
@CrossOrigin(origins = "*")
public class SystemSettingsController {

    private final SystemSettingsRepository repository;
    private final String UPLOAD_DIR = "uploads/templates/";

    public SystemSettingsController(SystemSettingsRepository repository) {
        this.repository = repository;
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @GetMapping
    public ResponseEntity<SystemSettings> getSettings() {
        SystemSettings settings = repository.findById(1L).orElse(new SystemSettings());
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<SystemSettings> updateSettings(@RequestBody SystemSettings newSettings) {
        newSettings.setId(1L);
        SystemSettings saved = repository.save(newSettings);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/template")
    public ResponseEntity<?> uploadTemplate(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo vazio");
            }

            String filename = "Template_Proposta.pdf";
            Path targetPath = Paths.get(UPLOAD_DIR + filename).toAbsolutePath();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            SystemSettings settings = repository.findById(1L).orElse(new SystemSettings());
            settings.setTemplateFilePath(targetPath.toString());
            repository.save(settings);

            return ResponseEntity.ok(settings);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}
