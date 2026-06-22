package com.hakshay.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.repo.MessageRepo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RedisSubscriberService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final MessageRepo messageRepo;

    public RedisSubscriberService(SimpMessagingTemplate messagingTemplate, MessageRepo messageRepo) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepo = messageRepo;
        this.objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    public void handleMessage(String messageJson) {
        try {
            JsonNode payload = objectMapper.readTree(messageJson);
            UUID messageId = UUID.fromString(payload.get("messageId").asText());
            UUID conversationId = UUID.fromString(payload.get("conversationId").asText());

            // Fetch the fully populated, clean Message object directly from Postgres!
            Message message = messageRepo.findById(messageId).orElse(null);

            if (message != null) {
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReceipt(String receiptJson) {
        try {
            com.hakshay.chat.service.MessageService.ReadReceipt receipt =
                    objectMapper.readValue(receiptJson, com.hakshay.chat.service.MessageService.ReadReceipt.class);

            messagingTemplate.convertAndSend("/topic/conversation/" + receipt.conversationId() + "/receipts", receipt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
