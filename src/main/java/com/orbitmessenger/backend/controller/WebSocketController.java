package com.orbitmessenger.backend.controller;

import com.orbitmessenger.backend.dto.ChatMessageRequest;
import com.orbitmessenger.backend.dto.MessageDto;
import com.orbitmessenger.backend.security.CustomUserDetails;
import com.orbitmessenger.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

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
        }
    }
}
