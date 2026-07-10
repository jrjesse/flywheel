package com.antigravity.sales.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingsResponse {
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private boolean hasSmtpPassword;
    private Boolean smtpAuth;
    private Boolean smtpTls;
    private String templateFilePath;
    private Double revenueGoal;
    private Integer contactGoal;
}
