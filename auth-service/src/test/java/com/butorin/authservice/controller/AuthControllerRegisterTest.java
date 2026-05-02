package com.butorin.authservice.controller;

import com.butorin.authservice.exception.DuplicateEmailException;
import com.butorin.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.mvc.servlet.path=/api")
@AutoConfigureMockMvc
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerUser_returnsOkAndSuccessPayload() throws Exception {
        when(authService.register(eq("testuser"), eq("test@test.com"), eq("123456")))
                .thenReturn("Успешная регистрация");

        mockMvc.perform(post("/api/auth/register")
                        .servletPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "testuser",
                  "email": "test@test.com",
                  "password": "123456"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Успеш")));
    }

    @Test
    void registerUser_withInvalidPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .servletPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "testuser",
                  "email": "",
                  "password": "123"
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_withDuplicateEmail_returnsConflict() throws Exception {
        when(authService.register(eq("testuser"), eq("test@test.com"), eq("123456")))
                .thenThrow(new DuplicateEmailException("Email already exists: test@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .servletPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "testuser",
                  "email": "test@test.com",
                  "password": "123456"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }
}
