package com.hakshay.chat.service;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;

    public PresenceService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof JwtAuthenticationToken jwtToken) {
            String username = jwtToken.getName();
            // Store their status globally in Redis
            redisTemplate.opsForHash().put("presence", username, "online");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof JwtAuthenticationToken jwtToken) {
            String username = jwtToken.getName();
            // Remove them globally from Redis
            redisTemplate.opsForHash().delete("presence", username);
        }
    }

    public void setUserAway(String username) {
        redisTemplate.opsForHash().put("presence", username, "away");
    }

    public void setUserOnline(String username) {
        redisTemplate.opsForHash().put("presence", username, "online");
    }

    public boolean isUserOnline(String username) {
        Object status = redisTemplate.opsForHash().get("presence", username);
        return status != null && "online".equals(status.toString());
    }

    public String getUserStatus(String username) {
        Object status = redisTemplate.opsForHash().get("presence", username);
        if (status == null) {
            return "offline";
        }
        return status.toString();
    }
}
