package com.hakshay.chat.service;

import com.hakshay.chat.model.User;
import com.hakshay.chat.repo.UserRepo;
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

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findUserById(id).orElse(null);
    }
}
