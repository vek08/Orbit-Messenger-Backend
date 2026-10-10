package com.orbitmessenger.backend.websocket;

import com.orbitmessenger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceService {
    // Maps User ID -> Set of Session IDs
    private final ConcurrentHashMap<UUID, Set<String>> activeSessions = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public void userConnected(UUID userId, String sessionId) {
        activeSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void userDisconnected(UUID userId, String sessionId) {
        Set<String> sessions = activeSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                activeSessions.remove(userId);
                
                userRepository.findById(userId).ifPresent(user -> {
                    user.setLastSeen(LocalDateTime.now());
                    userRepository.save(user);
                });
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        return activeSessions.containsKey(userId);
    }
}
