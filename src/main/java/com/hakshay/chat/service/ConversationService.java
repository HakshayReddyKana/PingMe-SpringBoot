package com.hakshay.chat.service;

import com.hakshay.chat.dto.ConversationDTO;
import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.ConversationRepo;
import com.hakshay.chat.repo.MessageRepo;
import com.hakshay.chat.repo.UserRepo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationService(ConversationRepo conversationRepo, UserRepo userRepo, MessageRepo messageRepo, SimpMessagingTemplate messagingTemplate) {
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.messagingTemplate = messagingTemplate;
    }

    public List<ConversationDTO> getConversationsForUser(User user) {
        List<Conversation> conversations = conversationRepo.findAllByParticipantId(user.getId());

        return conversations.stream().map(c -> {
            Message lastMessage = messageRepo.findFirstByConversationIdOrderByCreatedAtDesc(c.getId());
            long unreadCount = messageRepo.countByConversationIdAndStatusAndSenderIdNot(c.getId(), "sent", user.getId());

            return new ConversationDTO(
                    c.getId(), c.getType(), c.getName(), c.getCreatedAt(), c.getParticipants(), lastMessage, unreadCount,
                    c.getPendingParticipants(), c.getInitiatorId()
            );


        }).toList();
    }

    public Conversation createOrGetConversation(User creator, List<Long> participantIds, String type, String name) {
        // 1. Check for existing direct message
        if ("direct".equalsIgnoreCase(type) && participantIds.size() == 2) {
            Conversation existing = conversationRepo.findDirectConversation(participantIds.get(0), participantIds.get(1));
            if (existing != null) return existing;
        }

        // 2. Create new conversation
        Conversation conversation = new Conversation();
        conversation.setType(type);
        conversation.setName(name);
        conversation.setInitiatorId(creator.getId());

        // 3. Add participants
        List<User> users = userRepo.findByIdIn(participantIds);
        for (User u : users) {
            if (u.getId() == creator.getId()) {
                conversation.getParticipants().add(u); // Creator goes directly in
            } else {
                conversation.getPendingParticipants().add(u); // Everyone else is pending!
            }
        }

        // Ensure creator is in participants even if not in the participantIds list
        conversation.getParticipants().add(creator);

        return conversationRepo.save(conversation);
    }



    public ConversationDTO createOrGetConversationDTO(User creator, List<Long> participantIds, String type, String name) {

        // 1. Use your existing logic to get the raw entity
        Conversation c = createOrGetConversation(creator, participantIds, type, name);

        // 2. Fetch the latest stats
        Message lastMessage = messageRepo.findFirstByConversationIdOrderByCreatedAtDesc(c.getId());
        long unreadCount = messageRepo.countByConversationIdAndStatusAndSenderIdNot(c.getId(), "sent", creator.getId());

        // 3. Return the mapped DTO
        ConversationDTO dto = new ConversationDTO(
                c.getId(), c.getType(), c.getName(), c.getCreatedAt(), c.getParticipants(), lastMessage, unreadCount,
                c.getPendingParticipants(), c.getInitiatorId()
        );

        // 4. Real-time Broadcast: Tell all pending users and participants that a new conversation exists!
        c.getPendingParticipants().forEach(u -> 
            messagingTemplate.convertAndSend("/topic/user/" + u.getId() + "/conversations", dto)
        );
        c.getParticipants().forEach(u -> 
            messagingTemplate.convertAndSend("/topic/user/" + u.getId() + "/conversations", dto)
        );

        return dto;
    }

    public void acceptRequest(UUID id, User me) {
        Conversation conv = conversationRepo.findById(id).orElseThrow();
        // Remove from pending and add to participants
        if (conv.getPendingParticipants().removeIf(u -> u.getId() == me.getId())) {
            conv.getParticipants().add(me);
            conversationRepo.save(conv);
            
            // Broadcast the accepted state so the sender sees it instantly!
            Message lastMessage = messageRepo.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId());
            ConversationDTO dto = new ConversationDTO(
                conv.getId(), conv.getType(), conv.getName(), conv.getCreatedAt(), conv.getParticipants(), lastMessage, 0,
                conv.getPendingParticipants(), conv.getInitiatorId()
            );
            conv.getParticipants().forEach(u -> 
                messagingTemplate.convertAndSend("/topic/user/" + u.getId() + "/conversations", dto)
            );
        }
    }

    public void rejectRequest(UUID id, User me) {
        Conversation conv = conversationRepo.findById(id).orElseThrow();

        // Remove from pending
        conv.getPendingParticipants().removeIf(u -> u.getId() == me.getId());

        // If it's a direct message and was rejected, we delete the whole conversation
        if ("direct".equalsIgnoreCase(conv.getType())) {
            conversationRepo.delete(conv);
        } else {
            // For groups, we just save the conversation without them
            conversationRepo.save(conv);
        }
    }
}
