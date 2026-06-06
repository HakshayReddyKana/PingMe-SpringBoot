package com.hakshay.chat.service;

import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username); // Make sure you have this in UserRepo!
    }

    public Page<User> searchUsers(String query) {
        // Return max 10 users to prevent OOM errors and data leakage
        return userRepo.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                query, query, org.springframework.data.domain.PageRequest.of(0, 10)
        );
    }


    public User getUserById(Long id) {
        return userRepo.findUserById(id).orElse(null);
    }
}
