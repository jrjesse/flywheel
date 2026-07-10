package com.antigravity.sales.api.dto;

import lombok.Data;

@Data
public class InstagramConfigRequest {
    private String accessToken;
    private String instagramAccountId;
    private String pageId;
    private String appSecret;
    private String verifyToken;
    private boolean active = true;
}
