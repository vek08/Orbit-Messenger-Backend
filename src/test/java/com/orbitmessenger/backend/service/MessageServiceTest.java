package com.orbitmessenger.backend.service;

import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.Message;
import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.ConversationRepository;
import com.orbitmessenger.backend.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void saveMessage_EmptyContent_ThrowsIllegalArgumentException() {
        User sender = new User();
        UUID conversationId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(conversationId, sender, "   "));
            
        assertEquals("Message content cannot be empty", ex.getMessage());
    }

    @Test
    void saveMessage_ConversationNotFound_ThrowsIllegalArgumentException() {
        User sender = new User();
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(conversationId, sender, "Hello"));
            
        assertEquals("Conversation not found", ex.getMessage());
    }

    @Test
    void saveMessage_Unauthorized_ThrowsResponseStatusException() {
        User sender = new User();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation"))
            .when(conversationService).validateMembership(conversation, sender);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> messageService.saveMessage(conversationId, sender, "Hello"));
            
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You are not a member of this conversation", ex.getReason());
    }

    @Test
    void saveMessage_Success() {
        User sender = new User();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        doNothing().when(conversationService).validateMembership(conversation, sender);

        Message savedMessage = Message.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .content("Hello")
                .build();
                
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        var result = messageService.saveMessage(conversationId, sender, "Hello");

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }
}
