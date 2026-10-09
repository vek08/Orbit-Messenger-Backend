package com.orbitmessenger.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    @NotEmpty(message = "Member IDs are required")
    private List<UUID> memberIds;
}
