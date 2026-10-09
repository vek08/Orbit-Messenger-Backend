package com.orbitmessenger.backend.controller;

import com.orbitmessenger.backend.dto.ConversationDto;
import com.orbitmessenger.backend.dto.CreateDirectRequest;
import com.orbitmessenger.backend.dto.CreateGroupRequest;
import com.orbitmessenger.backend.dto.UserDto;
import com.orbitmessenger.backend.security.CustomUserDetails;
import com.orbitmessenger.backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getConversations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getUserConversations(userDetails.getUser()));
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationDto> createDirect(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateDirectRequest request) {
        return new ResponseEntity<>(conversationService.createDirectConversation(userDetails.getUser(), request), HttpStatus.CREATED);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationDto> createGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGroupRequest request) {
        return new ResponseEntity<>(conversationService.createGroupConversation(userDetails.getUser(), request), HttpStatus.CREATED);
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<List<UserDto>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId) {
        return ResponseEntity.ok(conversationService.getConversationMembers(conversationId, userDetails.getUser()));
    }
}
