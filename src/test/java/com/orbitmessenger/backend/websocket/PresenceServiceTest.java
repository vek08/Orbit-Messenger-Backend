package com.orbitmessenger.backend.websocket;

import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PresenceService presenceService;

    private UUID userId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void userConnected_SingleSession_IsOnline() {
        presenceService.userConnected(userId, "session-1");
        
        assertTrue(presenceService.isUserOnline(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    void userConnected_MultipleSessions_IsOnline() {
        presenceService.userConnected(userId, "session-1");
        presenceService.userConnected(userId, "session-2");
        
        assertTrue(presenceService.isUserOnline(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    void userDisconnected_MultipleSessions_RemainsOnline() {
        presenceService.userConnected(userId, "session-1");
        presenceService.userConnected(userId, "session-2");
        
        presenceService.userDisconnected(userId, "session-1");
        
        assertTrue(presenceService.isUserOnline(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    void userDisconnected_FinalSession_GoesOfflineAndUpdatesLastSeen() {
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        presenceService.userConnected(userId, "session-1");
        
        presenceService.userDisconnected(userId, "session-1");
        
        assertFalse(presenceService.isUserOnline(userId));
        verify(userRepository, times(1)).save(user);
        assertNotNull(user.getLastSeen());
    }
    
    @Test
    void userDisconnected_DuplicateEvent_Safe() {
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        presenceService.userConnected(userId, "session-1");
        
        presenceService.userDisconnected(userId, "session-1");
        presenceService.userDisconnected(userId, "session-1"); // Duplicate
        
        assertFalse(presenceService.isUserOnline(userId));
        // Should only save once, because the set is removed entirely after the first disconnect
        verify(userRepository, times(1)).save(user);
    }
}
