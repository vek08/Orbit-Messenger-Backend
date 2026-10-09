package com.orbitmessenger.backend.websocket;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    // Maps User ID -> Set of Session IDs
    private final ConcurrentHashMap<UUID, Set<String>> activeSessions = new ConcurrentHashMap<>();

    public void userConnected(UUID userId, String sessionId) {
        activeSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void userDisconnected(UUID userId, String sessionId) {
        Set<String> sessions = activeSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                activeSessions.remove(userId);
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        return activeSessions.containsKey(userId);
    }
}
