package com.hakshay.chat.controller;


import com.hakshay.chat.model.User;
import com.hakshay.chat.service.MyUserDetailsService;
import com.hakshay.chat.service.PresenceService;
import com.hakshay.chat.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MyUserDetailsService userDetailsService;
    private final PresenceService presenceService;

    public UserController(UserService userService, @Qualifier("myUserDetailsService") MyUserDetailsService userDetailsService, PresenceService presenceService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.presenceService = presenceService;
    }

    @PostMapping("/{blockedUserId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long blockedUserId, Principal principal) {
        User me = userService.getUserByUsername(principal.getName());
        User userToBlock = userService.getUserById(blockedUserId);

        if (userToBlock != null) {
            me.getBlockedUsers().add(userToBlock);
            userDetailsService.save(me);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{unblockedUserId}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long unblockedUserId, Principal principal) {
        User me = userService.getUserByUsername(principal.getName());
        User userToUnblock = userService.getUserById(unblockedUserId);

        if (userToUnblock != null) {
            me.getBlockedUsers().removeIf(u -> u.getId().equals(userToUnblock.getId()));
            userDetailsService.save(me);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public Page<User> searchUsers(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(); // Return empty page if query is null
        }
        return userService.searchUsers(query);
    }


    // Update the presence endpoint
    @GetMapping("/presence")
    public Map<Long, String> getPresence(@RequestParam List<Long> ids) {
        Map<Long, String> presenceMap = new HashMap<>();

        // Loop through the requested IDs and check their real status
        for (Long id : ids) {
            User user = userService.getUserById(id); // Ensure you have this method in your UserService!
            if (user != null) {
                presenceMap.put(id, presenceService.getUserStatus(user.getUsername()));
            } else {
                presenceMap.put(id, "offline");
            }
        }
        return presenceMap;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return ResponseEntity.notFound().build(); // Triggers the onboarding redirect!
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/oauth-register")
    public User oauthRegister(@RequestBody User request, Principal principal) {
        User newUser = new User();

        // 1. The database username MUST be the Google Email so the JWT finds it next time!
        newUser.setUsername(principal.getName());

        // 2. Save the "Username" they picked on the frontend into their Display Name
        // (Or if you want it saved somewhere else, you can map it to another field)
        newUser.setDisplayName(request.getDisplayName());

        // 3. Save the Avatar Color and Bio they picked
        newUser.setBio(request.getBio());
        newUser.setAvatarColor(request.getAvatarColor());

        // 4. Hash the auto-generated dummy password sent from the frontend
        newUser.setPassword(new BCryptPasswordEncoder(12).encode(request.getPassword()));

        // Save using your existing UserRepo (you may need to add a createUser or save method in UserService)
        return userDetailsService.save(newUser);
    }
}
