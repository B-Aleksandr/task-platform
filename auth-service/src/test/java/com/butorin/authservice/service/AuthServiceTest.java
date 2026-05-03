package com.butorin.authservice.service;

import com.butorin.authservice.dto.TokenResponse;
import com.butorin.authservice.dto.UserCreatedFlowEvent;
import com.butorin.authservice.dto.UserStreamEvent;
import com.butorin.authservice.exception.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthService.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private KeycloakAuthService keycloakAuthService;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @Test
    void register_success_createsUserSendsKafkaAndReturnsMessage() {
        when(keycloakAuthService.getUserIdByUsername("john")).thenReturn("kc-id-1");

        String result = authService.register("john", "john@mail.com", "pwd");

        assertThat(result).contains("регистрац");
        verify(keycloakAuthService).createUser("john", "john@mail.com", "pwd");
        verify(keycloakAuthService).getUserIdByUsername("john");

        org.mockito.ArgumentCaptor<UserStreamEvent> streamCaptor =
                org.mockito.ArgumentCaptor.forClass(UserStreamEvent.class);
        verify(kafkaProducerService).sendUserStreamEvent(streamCaptor.capture());
        assertThat(streamCaptor.getValue().getId()).isEqualTo("kc-id-1");
        assertThat(streamCaptor.getValue().getEmail()).isEqualTo("john@mail.com");

        org.mockito.ArgumentCaptor<UserCreatedFlowEvent> flowCaptor =
                org.mockito.ArgumentCaptor.forClass(UserCreatedFlowEvent.class);
        verify(kafkaProducerService).sendUserCreatedFlowEvent(flowCaptor.capture());
        assertThat(flowCaptor.getValue().getUserId()).isEqualTo("kc-id-1");
    }

    @Test
    void register_whenKeycloakThrows_wrapsDuplicateEmail() {
        doThrow(new RuntimeException("conflict")).when(keycloakAuthService)
                .createUser(any(), any(), any());

        assertThatThrownBy(() -> authService.register("a", "dup@x.com", "p"))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("dup@x.com");
    }

    @Test
    void login_returnsAccessTokenFromKeycloak() {
        TokenResponse tr = new TokenResponse();
        tr.setAccessToken("at-xyz");
        when(keycloakAuthService.loginUser("u", "p")).thenReturn(tr);

        assertThat(authService.login("u", "p")).isEqualTo("at-xyz");
    }
}
