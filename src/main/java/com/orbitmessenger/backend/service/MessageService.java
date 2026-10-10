package com.orbitmessenger.backend.service;

import com.orbitmessenger.backend.dto.MessageDto;
import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.Message;
import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.ConversationRepository;
import com.orbitmessenger.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    public Page<MessageDto> getConversationMessages(UUID conversationId, User currentUser, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        
        conversationService.validateMembership(conversation, currentUser);

        Page<Message> messagePage = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, PageRequest.of(page, size));
        return messagePage.map(MessageDto::fromEntity);
    }
    
    public MessageDto saveMessage(UUID conversationId, User sender, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        conversationService.validateMembership(conversation, sender);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();
                
        message = messageRepository.save(message);
        
        // Sender automatically reads the conversation up to this point
        conversationService.markConversationAsRead(conversationId, sender);
        
        return MessageDto.fromEntity(message);
    }
}
