package com.hakshay.chat.repo;


import com.hakshay.chat.model.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepo extends JpaRepository<Message, UUID> {

    // Find all messages for a conversation, newest first, with pagination
    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'read' WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'read'")
    int markMessagesAsRead(@Param("conversationId") UUID conversationId, @Param("userId") Long userId);

    // Find the latest message for a conversation
    Message findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    // Count how many messages in a conversation are "sent" and NOT from me
    long countByConversationIdAndStatusAndSenderIdNot(UUID conversationId, String status, Long senderId);

}
