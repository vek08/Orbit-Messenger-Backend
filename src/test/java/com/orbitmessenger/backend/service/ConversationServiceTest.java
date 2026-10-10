package com.orbitmessenger.backend.service;

import com.orbitmessenger.backend.dto.ConversationDto;
import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.ConversationMember;
import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.ConversationMemberRepository;
import com.orbitmessenger.backend.repository.ConversationRepository;
import com.orbitmessenger.backend.repository.MessageRepository;
import com.orbitmessenger.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ConversationService conversationService;

    private User currentUser;
    private UUID conversationId;
    private Conversation conversation;
    private ConversationMember member;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        conversationId = UUID.randomUUID();
        conversation = new Conversation();
        conversation.setId(conversationId);
        
        member = new ConversationMember();
        member.setConversation(conversation);
        member.setUser(currentUser);
    }

    @Test
    void markConversationAsRead_Success() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(memberRepository.findByConversationAndUser(conversation, currentUser)).thenReturn(Optional.of(member));

        assertNull(member.getLastReadAt());

        conversationService.markConversationAsRead(conversationId, currentUser);

        assertNotNull(member.getLastReadAt());
        verify(memberRepository).save(member);
    }

    @Test
    void markConversationAsRead_Unauthorized_ThrowsResponseStatusException() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(memberRepository.findByConversationAndUser(conversation, currentUser)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> conversationService.markConversationAsRead(conversationId, currentUser));
            
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void getUserConversations_EmptyConversations_ReturnsEmptyList() {
        when(memberRepository.findConversationsByUser(currentUser)).thenReturn(Collections.emptyList());
        
        List<ConversationDto> result = conversationService.getUserConversations(currentUser);
        
        assertTrue(result.isEmpty());
        verify(messageRepository, never()).findLatestMessagesForConversationIds(any());
        verify(messageRepository, never()).getUnreadCountsByUser(any());
    }
}
