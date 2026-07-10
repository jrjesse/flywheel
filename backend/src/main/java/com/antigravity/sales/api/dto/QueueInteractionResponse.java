package com.antigravity.sales.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class QueueInteractionResponse {
    private UUID id;
    private Long leadId;
    private String leadName;
    private String status;
    private String channel;
    private UUID assignedAgentId;
    private Instant createdAt;
    private Instant updatedAt;
}
