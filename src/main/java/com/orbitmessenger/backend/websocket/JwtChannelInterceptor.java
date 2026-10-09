package com.orbitmessenger.backend.websocket;

import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.ConversationRepository;
import com.orbitmessenger.backend.security.CustomUserDetails;
import com.orbitmessenger.backend.security.JwtService;
import com.orbitmessenger.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final PresenceService presenceService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userEmail = jwtService.extractUsername(token);
                if (userEmail != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        accessor.setUser(auth);
                        CustomUserDetails customDetails = (CustomUserDetails) userDetails;
                        presenceService.userConnected(customDetails.getUser().getId(), accessor.getSessionId());
                    } else {
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                }
            } else {
                throw new IllegalArgumentException("Missing JWT token");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/conversation.")) {
                try {
                    String conversationIdStr = destination.substring("/topic/conversation.".length());
                    UUID conversationId = UUID.fromString(conversationIdStr);
                    
                    UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                    if (auth == null) throw new IllegalArgumentException("Not authenticated");
                    
                    CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
                    User currentUser = customUserDetails.getUser();
                    
                    Conversation conversation = conversationRepository.findById(conversationId)
                            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
                    
                    conversationService.validateMembership(conversation, currentUser);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unauthorized subscription: " + e.getMessage());
                }
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
                presenceService.userDisconnected(customUserDetails.getUser().getId(), accessor.getSessionId());
            }
        }
        return message;
    }
}
