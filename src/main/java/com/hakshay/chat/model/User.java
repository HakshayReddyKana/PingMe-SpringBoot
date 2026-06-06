package com.hakshay.chat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;

    User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_color")
    private String avatarColor = "#6c63ff";

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String status = "offline";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "blocked_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_user_id")
    )
    private Set<User> blockedUsers = new java.util.HashSet<>();
}
