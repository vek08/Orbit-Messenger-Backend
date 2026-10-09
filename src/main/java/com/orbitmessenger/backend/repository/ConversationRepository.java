package com.orbitmessenger.backend.repository;

import com.orbitmessenger.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}
