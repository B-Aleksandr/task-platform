package com.butorin.accounting.event;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentFlowEvent {
    private String userId;
    private BigDecimal amount;
    private LocalDateTime date;
    private Long taskId;
}
