package com.hakshay.chat.controller;

import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.service.MessageService;
import com.hakshay.chat.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<Message> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return messageService.getMessages(conversationId, page, size);
    }

    // DTO class
    public record SendMessageRequest(String content, String type) {}

    @MessageMapping("/chat.sendMessage")
    public void sendMessageSTOMP(@Payload Message message, Principal principal) {
        messageService.processAndSendMessage(message, principal.getName());
    }

    @PostMapping
    public ResponseEntity<?> sendMessageREST(@PathVariable UUID conversationId, @RequestBody SendMessageRequest request, Principal principal) {
        try {
            Message message = new Message();
            Conversation conv = new Conversation();
            conv.setId(conversationId);
            message.setConversation(conv);
            message.setContent(request.content());
            message.setType(request.type());
            
            Message saved = messageService.processAndSendMessage(message, principal.getName());
            return ResponseEntity.ok(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/read")
    public void markAsRead(@PathVariable UUID conversationId, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        messageService.markAsRead(conversationId, user);
    }

}
