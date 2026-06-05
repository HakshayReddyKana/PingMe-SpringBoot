package com.hakshay.chat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "conversation_pending_participants",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> pendingParticipants = new java.util.HashSet<>();

    @Column(nullable = false)
    private String type; // "direct" or "group"

    private String name; // Only used for groups

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // This creates the join table for participants
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
}
