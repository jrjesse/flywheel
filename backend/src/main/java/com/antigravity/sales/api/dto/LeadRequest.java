package com.antigravity.sales.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeadRequest {
    private String name;
    private String email;
    private String phone;
    private String role;
    private String companyName;
    private String companySize;
    private String bio;
    private String source;
    private java.math.BigDecimal mrr;
    private List<SocialMediaRequest> socialMedias;
}
