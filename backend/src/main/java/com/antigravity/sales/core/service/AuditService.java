package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.AuditLog;
import com.antigravity.sales.core.repository.AuditLogRepository;
import com.antigravity.sales.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String resourceType, String resourceId) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        TenantContext.getTenantId().ifPresent(entry::setTenantId);
        TenantContext.getUserId().ifPresent(entry::setUserId);

        HttpServletRequest request = currentRequest();
        if (request != null) {
            entry.setIpAddress(request.getRemoteAddr());
            entry.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        }

        auditLogRepository.save(entry);
        log.info("audit action={} resourceType={} resourceId={} tenantId={} userId={}",
                action, resourceType, resourceId, entry.getTenantId(), entry.getUserId());
    }

    public void logAuthFailure(String action, String reason) {
        log.warn("auth_failure action={} reason={} ip={}", action, reason, currentIp());
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        return request != null ? request.getRemoteAddr() : "unknown";
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
