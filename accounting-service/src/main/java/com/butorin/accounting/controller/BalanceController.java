package com.butorin.accounting.controller;

import com.butorin.accounting.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{userId}")
    public BigDecimal getBalance(@PathVariable String userId) {
        return balanceService.getBalance(userId);
    }

    @PostMapping("/{userId}/add")
    public String addAmount(@PathVariable String userId, @RequestBody BigDecimal amount) {
        return balanceService.addAmount(userId, amount);
    }

    @PostMapping("/{userId}/substract")
    public String subtractAmount(@PathVariable String userId, @RequestBody BigDecimal amount) {
        return balanceService.subtractAmount(userId, amount);
    }
}
