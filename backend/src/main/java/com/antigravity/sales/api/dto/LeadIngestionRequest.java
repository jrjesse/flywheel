package com.antigravity.sales.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadIngestionRequest {
    @NotBlank @Size(max = 200)
    private String name;

    @Email @Size(max = 200)
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{8,20}$", message = "Invalid phone format")
    private String phone;

    @Size(max = 200)
    private String role;

    @Size(max = 200)
    private String companyName;

    @Size(max = 50)
    private String companySize;

    @Size(max = 5000)
    private String bio;

    @Size(max = 100)
    private String source;

    private BigDecimal mrr;
    private List<SocialMediaRequest> socialMedias;
}
