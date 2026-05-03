package com.butorin.accounting.service;

import com.butorin.accounting.entity.Balance;
import com.butorin.accounting.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BalanceService.class)
class BalanceServiceTest {

    @Autowired
    private BalanceService balanceService;

    @MockitoBean
    private BalanceRepository balanceRepository;

    @Test
    void getBalance_whenNoRecord_returnsZero() {
        when(balanceRepository.findByUserId("u1")).thenReturn(Optional.empty());

        assertThat(balanceService.getBalance("u1")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getBalance_whenRecordExists_returnsAmount() {
        when(balanceRepository.findByUserId("u-exists"))
                .thenReturn(Optional.of(new Balance(5L, "u-exists", new BigDecimal("42.5"))));

        assertThat(balanceService.getBalance("u-exists")).isEqualByComparingTo("42.5");
    }

    @Test
    void addAmount_createsNewBalance_whenUserNotFound() {
        when(balanceRepository.findByUserId("new-user")).thenReturn(Optional.empty());
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        String message = balanceService.addAmount("new-user", new BigDecimal("10"));

        assertThat(message).contains("10");
        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        Balance saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("new-user");
        assertThat(saved.getAmount()).isEqualByComparingTo("10");
    }

    @Test
    void addAmount_updatesExistingBalance() {
        Balance existing = new Balance(1L, "u2", new BigDecimal("5"));
        when(balanceRepository.findByUserId("u2")).thenReturn(Optional.of(existing));
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        balanceService.addAmount("u2", new BigDecimal("3"));

        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("8");
    }

    @Test
    void subtractAmount_debitsBalance() {
        Balance existing = new Balance(2L, "u3", new BigDecimal("20"));
        when(balanceRepository.findByUserId("u3")).thenReturn(Optional.of(existing));
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        String message = balanceService.subtractAmount("u3", new BigDecimal("7"));

        assertThat(message).contains("7");
        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("13");
    }

    @Test
    void subtractAmount_whenNoRecord_startsFromZero() {
        when(balanceRepository.findByUserId("new-sub")).thenReturn(Optional.empty());
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        balanceService.subtractAmount("new-sub", new BigDecimal("4"));

        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("new-sub");
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("-4");
    }
}
