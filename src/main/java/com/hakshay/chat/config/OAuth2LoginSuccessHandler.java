package com.hakshay.chat.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.client.url}")
    private String clientUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            email = oauth2User.getAttribute("login");
        }
        if (email == null) {
            email = oauth2User.getAttribute("sub");
        }
        // Final safety check
        email = Objects.requireNonNullElse(email, "unknown_user");
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(36000L))
                .subject(email)
                .claim("scope", "ROLE_USER")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        Cookie authCookie = new Cookie("auth_token", token);
        authCookie.setHttpOnly(true);
//        authCookie.setSecure(true); // Only send over HTTPS
        authCookie.setPath("/");
        authCookie.setMaxAge(36000); //  JWT expiration
//        authCookie.setAttribute("SameSite", "Lax"); // Critical for CSRF protection
        response.addCookie(authCookie);

        getRedirectStrategy().sendRedirect(request, response, clientUrl + "/dashboard");
    }
}