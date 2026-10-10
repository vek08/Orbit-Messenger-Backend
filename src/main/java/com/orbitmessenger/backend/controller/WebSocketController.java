package com.orbitmessenger.backend.controller;

import com.orbitmessenger.backend.dto.ChatMessageRequest;
import com.orbitmessenger.backend.dto.MessageDto;
import com.orbitmessenger.backend.security.CustomUserDetails;
import com.orbitmessenger.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            
            // Validate membership and save to database
            MessageDto savedMessage = messageService.saveMessage(
                    request.getConversationId(), 
                    userDetails.getUser(), 
                    request.getContent()
            );
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + request.getConversationId(), 
                    savedMessage
            );
        } else {
            throw new IllegalArgumentException("User not authenticated");
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, String> handleException(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return Map.of("error", ex.getMessage());
        }
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return Map.of("error", rse.getReason() != null ? rse.getReason() : "Unauthorized action");
        }
        // Do not expose stack traces for unexpected errors
        return Map.of("error", "An unexpected error occurred while processing the message");
    }
}
