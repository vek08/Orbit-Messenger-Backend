package com.orbitmessenger.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ConversationDto {
    private UUID id;
    private boolean isGroup;
    private String name;
    private LocalDateTime createdAt;
    private List<UserDto> members;
    private MessageDto latestMessage;
    private int unreadCount;
}
