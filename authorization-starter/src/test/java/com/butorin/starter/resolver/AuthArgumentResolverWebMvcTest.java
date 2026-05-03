package com.butorin.starter.resolver;

import com.butorin.starter.config.AuthAutoConfiguration;
import com.butorin.starter.support.AuthProbeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({AuthAutoConfiguration.class, AuthProbeController.class})
class AuthArgumentResolverWebMvcTest {

    /**
     * Локальная точка входа для слайса MVC (в модуле нет {@code @SpringBootApplication}).
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class WebMvcSliceBoot {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authAnnotation_resolvesUserIdAndRolesFromHeaders() throws Exception {
        mockMvc.perform(get("/probe/auth")
                        .header("user-id", "user-42")
                        .header("user-roles", "[\"ADMIN\",\"USER\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-42"))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("USER"));
    }

    @Test
    void authAnnotation_whenNoRolesHeader_returnsEmptyRoles() throws Exception {
        mockMvc.perform(get("/probe/auth").header("user-id", "solo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("solo"))
                .andExpect(jsonPath("$.roles.length()").value(0));
    }
}
