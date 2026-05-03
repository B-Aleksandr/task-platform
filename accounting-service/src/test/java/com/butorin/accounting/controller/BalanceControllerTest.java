package com.butorin.accounting.controller;

import com.butorin.accounting.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(controllers = BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BalanceService balanceService;

    @Test
    void getBalance_whenUserHasFunds_returns200() throws Exception {
        when(balanceService.getBalance("user-1")).thenReturn(new BigDecimal("150.50"));

        mockMvc.perform(get("/balances/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150.5));
    }

    @Test
    void getBalance_whenUserUnknown_returns200WithZero() throws Exception {
        when(balanceService.getBalance("unknown")).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/balances/unknown"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void addAmount_returns200() throws Exception {
        when(balanceService.addAmount(eq("user-1"), eq(new BigDecimal("42"))))
                .thenReturn("На счет зачислено 42");

        mockMvc.perform(post("/balances/user-1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("42"))
                .andExpect(status().isOk())
                .andExpect(content().string("На счет зачислено 42"));

        verify(balanceService).addAmount("user-1", new BigDecimal("42"));
    }

    @Test
    void subtractAmount_returns200() throws Exception {
        when(balanceService.subtractAmount(eq("user-1"), eq(new BigDecimal("5"))))
                .thenReturn("Со счета списано 5");

        mockMvc.perform(post("/balances/user-1/substract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Со счета списано 5"));

        verify(balanceService).subtractAmount("user-1", new BigDecimal("5"));
    }
}
