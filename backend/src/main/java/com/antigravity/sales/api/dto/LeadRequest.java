package com.antigravity.sales.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadRequest {
    private String name;
    private String email;
    private String phone;
    private String role;
    private String companyName;
    private String companySize;
    private String bio;
    private String source;
    private BigDecimal mrr;
    private List<SocialMediaRequest> socialMedias;
}
