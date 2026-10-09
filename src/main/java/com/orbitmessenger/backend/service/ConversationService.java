package com.orbitmessenger.backend.service;

import com.orbitmessenger.backend.dto.ConversationDto;
import com.orbitmessenger.backend.dto.CreateDirectRequest;
import com.orbitmessenger.backend.dto.CreateGroupRequest;
import com.orbitmessenger.backend.dto.UserDto;
import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.ConversationMember;
import com.orbitmessenger.backend.entity.User;
import com.orbitmessenger.backend.repository.ConversationMemberRepository;
import com.orbitmessenger.backend.repository.ConversationRepository;
import com.orbitmessenger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public List<ConversationDto> getUserConversations(User currentUser) {
        List<Conversation> conversations = memberRepository.findConversationsByUser(currentUser);
        return conversations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ConversationDto createDirectConversation(User currentUser, CreateDirectRequest request) {
        if (currentUser.getId().equals(request.getTargetUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a conversation with yourself");
        }

        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        // Check if direct conversation already exists
        List<Conversation> existingConvos = memberRepository.findConversationsByUser(currentUser);
        for (Conversation c : existingConvos) {
            if (!c.isGroup()) {
                List<ConversationMember> members = memberRepository.findByConversation(c);
                if (members.size() == 2 && members.stream().anyMatch(m -> m.getUser().getId().equals(targetUser.getId()))) {
                    return mapToDto(c); // Return existing
                }
            }
        }

        Conversation conversation = Conversation.builder()
                .isGroup(false)
                .createdBy(currentUser)
                .build();
        conversation = conversationRepository.save(conversation);

        memberRepository.save(ConversationMember.builder().conversation(conversation).user(currentUser).build());
        memberRepository.save(ConversationMember.builder().conversation(conversation).user(targetUser).build());

        return mapToDto(conversation);
    }

    public ConversationDto createGroupConversation(User currentUser, CreateGroupRequest request) {
        List<User> membersToAdd = new ArrayList<>();
        membersToAdd.add(currentUser);

        for (UUID memberId : request.getMemberIds()) {
            if (!memberId.equals(currentUser.getId())) {
                User user = userRepository.findById(memberId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found: " + memberId));
                if (!membersToAdd.contains(user)) {
                    membersToAdd.add(user);
                }
            }
        }

        if (membersToAdd.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group must have at least 2 members");
        }

        Conversation conversation = Conversation.builder()
                .isGroup(true)
                .name(request.getName())
                .createdBy(currentUser)
                .build();
        conversation = conversationRepository.save(conversation);

        for (User u : membersToAdd) {
            memberRepository.save(ConversationMember.builder().conversation(conversation).user(u).build());
        }

        return mapToDto(conversation);
    }

    public List<UserDto> getConversationMembers(UUID conversationId, User currentUser) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        
        validateMembership(conversation, currentUser);

        return memberRepository.findByConversation(conversation).stream()
                .map(m -> UserDto.fromEntity(m.getUser()))
                .collect(Collectors.toList());
    }

    private ConversationDto mapToDto(Conversation conversation) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setGroup(conversation.isGroup());
        dto.setName(conversation.getName());
        dto.setCreatedAt(conversation.getCreatedAt());

        List<UserDto> memberDtos = memberRepository.findByConversation(conversation).stream()
                .map(m -> UserDto.fromEntity(m.getUser()))
                .collect(Collectors.toList());
        dto.setMembers(memberDtos);

        return dto;
    }
    
    public void validateMembership(Conversation conversation, User user) {
        memberRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation"));
    }
}
