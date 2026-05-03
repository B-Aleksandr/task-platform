package com.butorin.accounting.service;

import com.butorin.accounting.entity.Balance;
import com.butorin.accounting.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;

    public BigDecimal getBalance(String userId) {
        return balanceRepository.findByUserId(userId)
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    public String addAmount(String userId, BigDecimal amount) {
        Balance balance = balanceRepository.findByUserId(userId)
                .orElse(new Balance(null, userId, BigDecimal.ZERO));
        balance.setAmount(balance.getAmount().add(amount));
        balanceRepository.save(balance);
        return "На счет зачислено " + amount;
    }

    public String subtractAmount(String userId, BigDecimal amount) {
        Balance balance = balanceRepository.findByUserId(userId)
                .orElse(new Balance(null, userId, BigDecimal.ZERO));
        balance.setAmount(balance.getAmount().subtract(amount));
        balanceRepository.save(balance);
        return "Со счета списано " + amount;
    }
}
