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

    // 1. Find all conversations for a specific user (either accepted or pending)
    @Query("SELECT DISTINCT c FROM Conversation c LEFT JOIN c.participants p LEFT JOIN c.pendingParticipants pp WHERE p.id = :userId OR pp.id = :userId")
    List<Conversation> findAllByParticipantId(@Param("userId") Long userId);

    // 2. Magic query: Check if a Direct Message already exists between two specific users (including pending state)
    @Query("""
    SELECT c
    FROM Conversation c
    WHERE c.type = 'direct'
      AND (
            :userOneId IN (
                SELECT p.id FROM c.participants p
            )
            OR
            :userOneId IN (
                SELECT pp.id FROM c.pendingParticipants pp
            )
      )
      AND (
            :userTwoId IN (
                SELECT p.id FROM c.participants p
            )
            OR
            :userTwoId IN (
                SELECT pp.id FROM c.pendingParticipants pp
            )
      )
""")
    Conversation findDirectConversation(
            @Param("userOneId") Long userOneId,
            @Param("userTwoId") Long userTwoId
    );
}

