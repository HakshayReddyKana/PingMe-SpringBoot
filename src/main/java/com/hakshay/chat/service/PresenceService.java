package com.hakshay.chat.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    // Store active connection counts per user to handle multiple tabs/refreshes
    private final ConcurrentHashMap<String, Integer> activeSessions = new ConcurrentHashMap<>();
    
    // Store explicit away statuses
    private final ConcurrentHashMap<String, Boolean> explicitAway = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof JwtAuthenticationToken jwtToken) {
            String username = jwtToken.getName();
            activeSessions.merge(username, 1, Integer::sum);
            // On new connection, reset to online by default
            explicitAway.remove(username);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof JwtAuthenticationToken jwtToken) {
            String username = jwtToken.getName();
            activeSessions.computeIfPresent(username, (k, v) -> v > 1 ? v - 1 : null);
            if (!activeSessions.containsKey(username)) {
                explicitAway.remove(username);
            }
        }
    }

    public void setUserAway(String username) {
        if (activeSessions.containsKey(username)) {
            explicitAway.put(username, true);
        }
    }

    public void setUserOnline(String username) {
        explicitAway.remove(username);
    }

    public boolean isUserOnline(String username) {
        return activeSessions.containsKey(username) && !explicitAway.containsKey(username);
    }

    public String getUserStatus(String username) {
        if (!activeSessions.containsKey(username)) {
            return "offline";
        }
        if (explicitAway.containsKey(username)) {
            return "away";
        }
        return "online";
    }
}
