package com.orbitmessenger.backend.repository;

import com.orbitmessenger.backend.entity.ConversationMember;
import com.orbitmessenger.backend.entity.Conversation;
import com.orbitmessenger.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    List<ConversationMember> findByUser(User user);
    List<ConversationMember> findByConversation(Conversation conversation);
    Optional<ConversationMember> findByConversationAndUser(Conversation conversation, User user);
    
    @Query("SELECT cm.conversation FROM ConversationMember cm WHERE cm.user = :user")
    List<Conversation> findConversationsByUser(@Param("user") User user);
}
