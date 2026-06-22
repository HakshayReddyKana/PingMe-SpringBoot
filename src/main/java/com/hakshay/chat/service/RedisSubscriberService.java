package com.hakshay.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakshay.chat.model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriberService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisSubscriberService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    // This is called automatically by Redis whenever a message hits the "chat-topic" channel
    public void handleMessage(String messageJson) {
        try {
            Message message = objectMapper.readValue(messageJson, Message.class);

            // Broadcast it locally to the WebSocket clients connected to THIS specific container!
            messagingTemplate.convertAndSend("/topic/conversation/" + message.getConversation().getId(), message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This is called automatically by Redis whenever a receipt hits the "receipt-topic"
    public void handleReceipt(String receiptJson) {
        try {
            // Convert the JSON back into your custom ReadReceipt record
            com.hakshay.chat.service.MessageService.ReadReceipt receipt =
                    objectMapper.readValue(receiptJson, com.hakshay.chat.service.MessageService.ReadReceipt.class);

            // Broadcast it locally to WebSockets
            messagingTemplate.convertAndSend("/topic/conversation/" + receipt.conversationId() + "/receipts", receipt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
