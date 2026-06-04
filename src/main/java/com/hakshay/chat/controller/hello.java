package com.hakshay.chat.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class hello {

    @GetMapping("/hello")
    ResponseEntity<String> greet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok("Hello, " + username + "!!!");
    }

    @GetMapping("/")
    ResponseEntity<String> isRunning(HttpSession session) {
        return ResponseEntity.ok("The App is UP!!!... Session ID: "+session.getId());
    }
}
