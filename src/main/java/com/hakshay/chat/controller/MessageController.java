package com.hakshay.chat.controller;

import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.service.MessageService;
import com.hakshay.chat.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
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

    @PostMapping
    public Message sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody SendMessageRequest request,
            Principal principal) {
        User sender = userService.getUserByUsername(principal.getName());

        // 1. Save it to the database
        Message savedMessage = messageService.saveMessage(conversationId, sender, request.content(), request.type());

        // 2. BROADCAST it to the WebSocket topic instantly!
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);

        return savedMessage;
    }

    @PostMapping("/read")
    public void markAsRead(@PathVariable UUID conversationId, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        messageService.markAsRead(conversationId, user);
    }
}
