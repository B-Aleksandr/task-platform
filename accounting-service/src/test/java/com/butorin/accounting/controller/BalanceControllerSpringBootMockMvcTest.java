package com.butorin.accounting.controller;

import com.butorin.accounting.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Дублирует сценарии {@link BalanceControllerTest}, но поднимает контроллер через
 * {@link SpringBootTest} + {@link AutoConfigureMockMvc} (часть IDE/агрегаторов
 * покрытия учитывает только такие тесты).
 */
@SpringBootTest(classes = {
        BalanceController.class,
        WebMvcAutoConfiguration.class,
        JacksonAutoConfiguration.class
})
@AutoConfigureMockMvc
class BalanceControllerSpringBootMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BalanceService balanceService;

    @Test
    void getBalance_returnsValueFromService() throws Exception {
        when(balanceService.getBalance("u1")).thenReturn(new BigDecimal("99.99"));

        mockMvc.perform(get("/balances/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(99.99));
    }

    @Test
    void addAmount_delegatesToService() throws Exception {
        when(balanceService.addAmount(eq("u1"), eq(new BigDecimal("10"))))
                .thenReturn("ok-add");

        mockMvc.perform(post("/balances/u1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok-add"));

        verify(balanceService).addAmount("u1", new BigDecimal("10"));
    }

    @Test
    void subtractAmount_delegatesToService() throws Exception {
        when(balanceService.subtractAmount(eq("u1"), eq(new BigDecimal("3"))))
                .thenReturn("ok-sub");

        mockMvc.perform(post("/balances/u1/substract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("3"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok-sub"));

        verify(balanceService).subtractAmount("u1", new BigDecimal("3"));
    }
}
