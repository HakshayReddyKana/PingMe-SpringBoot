package com.hakshay.chat.dto;

import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ConversationDTO(
        UUID id,
        String type,
        String name,
        Instant createdAt,
        Set<User> participants,
        Message lastMessage,
        long unreadCount
) {}
