package com.antigravity.sales.api.controller;

import com.antigravity.sales.core.model.Tenant;
import com.antigravity.sales.core.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "*")
public class TenantController {
    
    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(savedTenant);
    }
}
