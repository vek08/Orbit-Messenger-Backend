package com.orbitmessenger.backend.controller;

import com.orbitmessenger.backend.dto.MessageDto;
import com.orbitmessenger.backend.security.CustomUserDetails;
import com.orbitmessenger.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
            
        return ResponseEntity.ok(messageService.getConversationMessages(conversationId, userDetails.getUser(), page, size));
    }
}
