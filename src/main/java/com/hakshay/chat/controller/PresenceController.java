package com.hakshay.chat.controller;

import com.hakshay.chat.service.PresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @MessageMapping("/chat/presence")
    public void handlePresenceUpdate(JwtAuthenticationToken authentication, @Payload Map<String, String> payload) {
        if (authentication == null) return;
        
        String username = authentication.getName();
        String status = payload.get("status");

        if ("away".equals(status)) {
            presenceService.setUserAway(username);
        } else if ("online".equals(status)) {
            presenceService.setUserOnline(username);
        }
    }
}
