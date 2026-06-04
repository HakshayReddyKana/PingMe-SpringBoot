package com.hakshay.chat.repo;

import com.hakshay.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation, UUID> {

    // 1. Find all conversations for a specific user (to show in the sidebar)
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId")
    List<Conversation> findAllByParticipantId(@Param("userId") Long userId);

    // 2. Magic query: Check if a Direct Message already exists between two specific users
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE c.type = 'direct' AND p1.id = :userOneId AND p2.id = :userTwoId")
    Conversation findDirectConversation(@Param("userOneId") Long userOneId, @Param("userTwoId") Long userTwoId);
}

