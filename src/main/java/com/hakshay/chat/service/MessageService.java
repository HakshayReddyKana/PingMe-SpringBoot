package com.hakshay.chat.service;

import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.ConversationRepo;
import com.hakshay.chat.repo.MessageRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepo messageRepo;
    private final ConversationRepo conversationRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public record ReadReceipt(Long userId, UUID conversationId, String status) {}


    public MessageService(MessageRepo messageRepo, ConversationRepo conversationRepo, SimpMessagingTemplate messagingTemplate) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.messagingTemplate = messagingTemplate;
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
            // Because ReadReceipt is a custom Object, Java knows EXACTLY which method to call!
            ReadReceipt receipt = new ReadReceipt(user.getId(), conversationId, "read");
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/receipts", receipt);
        }
    }
}
