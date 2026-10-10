package com.orbitmessenger.backend.repository;

import com.orbitmessenger.backend.entity.Message;
import com.orbitmessenger.backend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);
    
    Optional<Message> findTopByConversationOrderByCreatedAtDesc(Conversation conversation);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT DISTINCT ON (conversation_id) * FROM messages WHERE conversation_id IN :conversationIds ORDER BY conversation_id, created_at DESC", 
        nativeQuery = true)
    java.util.List<Message> findLatestMessagesForConversationIds(@org.springframework.data.repository.query.Param("conversationIds") java.util.List<UUID> conversationIds);

    @org.springframework.data.jpa.repository.Query(
        "SELECT m.conversation.id, COUNT(m.id) " +
        "FROM Message m JOIN ConversationMember cm ON m.conversation = cm.conversation " +
        "WHERE cm.user = :user " +
        "AND m.sender != :user " +
        "AND (m.createdAt > cm.lastReadAt OR cm.lastReadAt IS NULL) " +
        "GROUP BY m.conversation.id")
    java.util.List<Object[]> getUnreadCountsByUser(@org.springframework.data.repository.query.Param("user") com.orbitmessenger.backend.entity.User user);
}
