package com.hakshay.chat.service;

import com.hakshay.chat.dto.ConversationDTO;
import com.hakshay.chat.model.Conversation;
import com.hakshay.chat.model.Message;
import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.ConversationRepo;
import com.hakshay.chat.repo.MessageRepo;
import com.hakshay.chat.repo.UserRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;

    public ConversationService(ConversationRepo conversationRepo, UserRepo userRepo, MessageRepo messageRepo) {
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    public List<ConversationDTO> getConversationsForUser(User user) {
        List<Conversation> conversations = conversationRepo.findAllByParticipantId(user.getId());

        return conversations.stream().map(c -> {
            Message lastMessage = messageRepo.findFirstByConversationIdOrderByCreatedAtDesc(c.getId());
            long unreadCount = messageRepo.countByConversationIdAndStatusAndSenderIdNot(c.getId(), "sent", user.getId());

            return new com.hakshay.chat.dto.ConversationDTO(
                    c.getId(), c.getType(), c.getName(), c.getCreatedAt(), c.getParticipants(), lastMessage, unreadCount
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

        // 3. Add participants
        List<User> participants = userRepo.findByIdIn(participantIds);
        conversation.getParticipants().addAll(participants);

        // Make sure creator is in the chat even if not passed in list
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
        return new ConversationDTO(
                c.getId(), c.getType(), c.getName(), c.getCreatedAt(), c.getParticipants(), lastMessage, unreadCount
        );
    }
}
