package com.orbitmessenger.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateDirectRequest {
    @NotNull(message = "Target user ID is required")
    private UUID targetUserId;
}
