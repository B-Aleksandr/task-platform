package com.butorin.authservice.service;

import com.butorin.authservice.dto.TokenResponse;
import com.butorin.authservice.dto.UserCreatedFlowEvent;
import com.butorin.authservice.dto.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakAuthService keycloakAuthService;
    private final KafkaProducerService kafkaProducerService;

    public String register(String username, String password) {
        keycloakAuthService.createUser(username, password);
        String userIdByUsername = keycloakAuthService.getUserIdByUsername(username);

        UserStreamEvent userStreamEvent = new UserStreamEvent();
        userStreamEvent.setId(userIdByUsername);
        userStreamEvent.setName(username);
        userStreamEvent.setEmail(username);
        userStreamEvent.setEventTime(LocalDateTime.now());
        kafkaProducerService.sendUserStreamEvent(userStreamEvent);

        UserCreatedFlowEvent userCreatedFlowEvent = new UserCreatedFlowEvent();
        userCreatedFlowEvent.setUserId(userIdByUsername);
        userCreatedFlowEvent.setCreatedAt(LocalDateTime.now());
        kafkaProducerService.sendUserCreatedFlowEvent(userCreatedFlowEvent);

        return "Успешная регистрация";
    }

    public String login(String username, String password) {
        TokenResponse tokenResponse = keycloakAuthService.loginUser(username, password);
        return tokenResponse.getAccessToken();
    }
}
