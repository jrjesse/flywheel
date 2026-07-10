package com.antigravity.sales.core.model;

import com.antigravity.sales.security.crypto.AesGcmConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSettings {
    @Id
    private Long id = 1L; // Only one row for global settings

    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;

    @Convert(converter = AesGcmConverter.class)
    private String smtpPassword;
    private Boolean smtpAuth = true;
    private Boolean smtpTls = true;

    private String templateFilePath;

    private Double revenueGoal = 100000.0;
    private Integer contactGoal = 50;
}
