package com.butorin.accounting.service;

import com.butorin.accounting.entity.Balance;
import com.butorin.accounting.event.TaskCompletedEvent;
import com.butorin.accounting.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    final BalanceRepository balanceRepository;

    public void processReward(TaskCompletedEvent taskCompletedEvent) {
        Balance balance = balanceRepository.findByUserId(taskCompletedEvent.getUserId())
                .orElse(new Balance(null,taskCompletedEvent.getUserId(), BigDecimal.ZERO));
        balance.setAmount(balance.getAmount().add(new BigDecimal("100")));
        balanceRepository.save(balance);
    }


}
