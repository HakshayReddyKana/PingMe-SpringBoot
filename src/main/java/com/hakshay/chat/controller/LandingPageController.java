package com.hakshay.chat.controller;

import com.hakshay.chat.model.User;
import com.hakshay.chat.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/landingPage")
public class LandingPageController {

    @Autowired
    private final MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LandingPageController(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @GetMapping("/guest")
    public String guestLanding() {
        return "Welcome to the Landing Page, Guest!!!";
    }

    // 1. Update the DTO that receives the JSON payload
    public record RegisterRequest(
            String username,
            String password,
            String displayName,
            String bio,
            String avatarColor
    ) {}


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        User newUser = new User();
        newUser.setUsername(request.username());

        newUser.setPassword(new BCryptPasswordEncoder(12).encode(request.password()));
        // Safely set optional fields
        if (request.displayName() != null && !request.displayName().isEmpty()) {
            newUser.setDisplayName(request.displayName());
        }
        if (request.bio() != null && !request.bio().isEmpty()) {
            newUser.setBio(request.bio());
        }
        if (request.avatarColor() != null && !request.avatarColor().isEmpty()) {
            newUser.setAvatarColor(request.avatarColor());
        } else {
            newUser.setAvatarColor("#6c63ff"); // Default fallback
        }
        myUserDetailsService.save(newUser);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        Instant now = Instant.now();
        long expiry = 36000L; // 10 hours

        String scope = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
