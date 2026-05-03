package com.butorin.authservice.controller;

import com.butorin.authservice.exception.DuplicateEmailException;
import com.butorin.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Слайс {@link WebMvcTest} для регистрации (покрытие контроллера без полного контекста приложения).
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerRegisterWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_returnsMessage() throws Exception {
        when(authService.register(eq("u"), eq("e@e.com"), eq("123456")))
                .thenReturn("Успешная регистрация");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"u","email":"e@e.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Успеш")));
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        when(authService.register(eq("u"), eq("e@e.com"), eq("123456")))
                .thenThrow(new DuplicateEmailException("Email already exists: e@e.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"u","email":"e@e.com","password":"123456"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void register_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"bad","password":"1"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
