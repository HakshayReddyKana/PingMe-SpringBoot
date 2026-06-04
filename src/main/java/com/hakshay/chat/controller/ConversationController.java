package com.hakshay.chat.controller;

import com.hakshay.chat.dto.ConversationDTO;
import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.User;
import com.hakshay.chat.service.ConversationService;
import com.hakshay.chat.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    public ConversationController(ConversationService conversationService, UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @GetMapping
    public List<ConversationDTO> getMyConversations(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return List.of();
        }
        return conversationService.getConversationsForUser(user);
    }


    // DTO class
    public record CreateConvRequest(List<Long> participantIds, String type, String name) {}

    @PostMapping
    public ConversationDTO createConversation(@RequestBody CreateConvRequest request, Principal principal) {
        User creator = userService.getUserByUsername(principal.getName());

        // Let the service handle the creation and DTO mapping!
        return conversationService.createOrGetConversationDTO(
                creator, request.participantIds(), request.type(), request.name()
        );
    }
}
