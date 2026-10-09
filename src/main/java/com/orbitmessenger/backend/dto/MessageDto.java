package com.orbitmessenger.backend.dto;

import com.orbitmessenger.backend.entity.Message;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageDto {
    private UUID id;
    private UUID conversationId;
    private UserDto sender;
    private String content;
    private LocalDateTime createdAt;

    public static MessageDto fromEntity(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSender(UserDto.fromEntity(message.getSender()));
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
