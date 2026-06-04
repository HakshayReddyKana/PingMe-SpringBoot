package com.hakshay.chat.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    long id;
    String username;
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
}
