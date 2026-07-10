package com.antigravity.sales.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignLeadRequest {

    @NotNull
    private UUID userId;
}
