package com.butorin.accounting.service;

import com.butorin.accounting.entity.TaskCompletion;
import com.butorin.accounting.event.PaymentFlowEvent;
import com.butorin.accounting.repository.TaskCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledRewardService {

    private final TaskCompletionRepository taskCompletionRepository;
    private final BalanceService balanceService;
    private final KafkaTemplate<String, PaymentFlowEvent> kafkaTemplate;

    @Scheduled(cron = "0 0 0 */14 * ?") // каждые 14 дней в 00:00
    @Transactional
    public void processRewards() {
        List<TaskCompletion> unprocessed = taskCompletionRepository.findByRewardedFalse();

        if (unprocessed.isEmpty()) {
            System.out.println("Нет задач для начисления");
            return;
        }

        for (TaskCompletion completion : unprocessed) {
            balanceService.addAmount(completion.getUserId(), new BigDecimal("100"));

            PaymentFlowEvent event = new PaymentFlowEvent();
            event.setUserId(completion.getUserId());
            event.setAmount(new BigDecimal("100"));
            event.setDate(LocalDateTime.now());
            event.setTaskId(completion.getTaskId());

            kafkaTemplate.send("payment-flow", event);

            completion.setRewarded(true);
            taskCompletionRepository.save(completion);

            System.out.println("Начислено 100 пользователю " + completion.getUserId() +
                    " за задачу " + completion.getTaskId());
        }
    }
}
