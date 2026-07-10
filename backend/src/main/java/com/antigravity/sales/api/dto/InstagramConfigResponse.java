package com.antigravity.sales.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InstagramConfigResponse {
    private UUID clientId;
    private String instagramAccountId;
    private String pageId;
    private boolean hasAccessToken;
    private boolean hasAppSecret;
    private boolean hasVerifyToken;
    private boolean active;
}
