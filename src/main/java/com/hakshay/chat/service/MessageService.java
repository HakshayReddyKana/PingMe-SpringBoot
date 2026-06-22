package com.hakshay.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.ConversationRepo;
import com.hakshay.chat.repo.MessageRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepo messageRepo;
    private final ConversationRepo conversationRepo;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public record ReadReceipt(Long userId, UUID conversationId, String status) {}


    public MessageService(MessageRepo messageRepo, ConversationRepo conversationRepo, UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    public Message processAndSendMessage(Message message, String senderUsername) {
        User me = userService.getUserByUsername(senderUsername);

        // message.getConversation() might be null depending on STOMP payload,
        // so ensure the frontend sends the conversation ID!
        UUID convId = message.getConversation() != null ? message.getConversation().getId() : null;
        if (convId == null) {
            throw new IllegalArgumentException("Conversation ID is missing in the message payload");
        }

        Conversation conv = conversationRepo.findById(convId).orElseThrow();
        // 1. Prevent sending if YOU are still pending!
        if (conv.getPendingParticipants().stream().anyMatch(u -> Objects.equals(u.getId(), me.getId()))) {
            throw new SecurityException("You must accept the invitation before sending messages.");
        }
        
        // 1.b Prevent sending if it's a direct message and the other person is still pending!
        if ("direct".equalsIgnoreCase(conv.getType()) && !conv.getPendingParticipants().isEmpty()) {
            throw new SecurityException("You cannot send messages until the other user accepts the invitation.");
        }

        // 2. Prevent sending if blocked by any active participant!
        for (User participant : conv.getParticipants()) {
            if (!participant.getId().equals(me.getId())) {
                if (participant.getBlockedUsers().contains(me)) {
                    throw new SecurityException("You have been blocked by a user in this chat.");
                }
            }
        }
        // 3. Save message
        message.setSender(me);
        message.setConversation(conv);
        message.setStatus("sent");
        Message savedMessage = messageRepo.save(message);

        // 4. Broadcast to REDIS instead of STOMP!
        try {
            String jsonMessage = objectMapper.writeValueAsString(savedMessage);
            redisTemplate.convertAndSend("chat-topic", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedMessage;


//         4. Broadcast over STOMP
//        messagingTemplate.convertAndSend("/topic/conversation/" + conv.getId(), savedMessage);
    }
    public Page<Message> getMessages(UUID conversationId, int page, int size) {
        return messageRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size));
    }

    public Message saveMessage(UUID conversationId, User sender, String content, String type) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setType(type != null ? type : "text");
        message.setStatus("sent");

        return messageRepo.save(message);
    }

    @Transactional
    public void markAsRead(UUID conversationId, User user) {
        int updatedCount = messageRepo.markMessagesAsRead(conversationId, user.getId());

        if (updatedCount > 0) {
            try {
                ReadReceipt receipt = new ReadReceipt(user.getId(), conversationId, "read");
                String jsonReceipt = objectMapper.writeValueAsString(receipt);
                // Publish to our new secondary Redis topic!
                redisTemplate.convertAndSend("receipt-topic", jsonReceipt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
