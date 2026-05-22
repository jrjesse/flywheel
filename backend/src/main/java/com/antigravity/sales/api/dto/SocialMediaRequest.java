package com.antigravity.sales.api.dto;

import com.antigravity.sales.core.model.SocialMediaType;
import lombok.Data;

@Data
public class SocialMediaRequest {
    private SocialMediaType type;
    private String url;
}
