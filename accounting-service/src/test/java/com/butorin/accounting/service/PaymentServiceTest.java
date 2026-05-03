package com.butorin.accounting.service;

import com.butorin.accounting.entity.Balance;
import com.butorin.accounting.event.TaskCompletedEvent;
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

@SpringBootTest(classes = PaymentService.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private BalanceRepository balanceRepository;

    @Test
    void processReward_addsHundredToBalance() {
        TaskCompletedEvent event = new TaskCompletedEvent();
        event.setUserId("pay-user");

        when(balanceRepository.findByUserId("pay-user")).thenReturn(Optional.empty());
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processReward(event);

        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("pay-user");
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("100");
    }

    @Test
    void processReward_whenBalanceExists_addsHundredToExisting() {
        TaskCompletedEvent event = new TaskCompletedEvent();
        event.setUserId("existing-user");

        Balance existing = new Balance(9L, "existing-user", new BigDecimal("50"));
        when(balanceRepository.findByUserId("existing-user")).thenReturn(Optional.of(existing));
        when(balanceRepository.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processReward(event);

        org.mockito.ArgumentCaptor<Balance> captor = org.mockito.ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("150");
    }
}
