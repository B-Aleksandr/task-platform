package com.butorin.accounting.service;

import com.butorin.accounting.entity.TaskCompletion;
import com.butorin.accounting.event.PaymentFlowEvent;
import com.butorin.accounting.repository.TaskCompletionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ScheduledRewardService.class)
class ScheduledRewardServiceTest {

    @Autowired
    private ScheduledRewardService scheduledRewardService;

    @MockitoBean
    private TaskCompletionRepository taskCompletionRepository;

    @MockitoBean
    private BalanceService balanceService;

    @SuppressWarnings("unchecked")
    @MockitoBean
    private KafkaTemplate<String, PaymentFlowEvent> kafkaTemplate;

    @Test
    void processRewards_whenNoPending_doesNothing() {
        when(taskCompletionRepository.findByRewardedFalse()).thenReturn(List.of());

        scheduledRewardService.processRewards();

        verify(balanceService, never()).addAmount(any(), any());
        verify(kafkaTemplate, never()).send(any(String.class), any(PaymentFlowEvent.class));
    }

    @Test
    void processRewards_whenUnrewarded_addsBalanceSendsKafkaAndMarksRewarded() {
        TaskCompletion completion = new TaskCompletion();
        completion.setId(10L);
        completion.setTaskId(200L);
        completion.setUserId("reward-user");
        completion.setRewarded(false);

        when(taskCompletionRepository.findByRewardedFalse()).thenReturn(List.of(completion));
        when(kafkaTemplate.send(eq("payment-flow"), any(PaymentFlowEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(taskCompletionRepository.save(any(TaskCompletion.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduledRewardService.processRewards();

        verify(balanceService).addAmount("reward-user", new BigDecimal("100"));

        org.mockito.ArgumentCaptor<PaymentFlowEvent> eventCaptor =
                org.mockito.ArgumentCaptor.forClass(PaymentFlowEvent.class);
        verify(kafkaTemplate).send(eq("payment-flow"), eventCaptor.capture());
        PaymentFlowEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo("reward-user");
        assertThat(event.getAmount()).isEqualByComparingTo("100");
        assertThat(event.getTaskId()).isEqualTo(200L);
        assertThat(event.getDate()).isNotNull();

        org.mockito.ArgumentCaptor<TaskCompletion> completionCaptor =
                org.mockito.ArgumentCaptor.forClass(TaskCompletion.class);
        verify(taskCompletionRepository).save(completionCaptor.capture());
        assertThat(completionCaptor.getValue().isRewarded()).isTrue();
    }

    @Test
    void processRewards_processesMultipleUnrewarded() {
        TaskCompletion first = new TaskCompletion();
        first.setId(1L);
        first.setTaskId(10L);
        first.setUserId("a");
        first.setRewarded(false);

        TaskCompletion second = new TaskCompletion();
        second.setId(2L);
        second.setTaskId(20L);
        second.setUserId("b");
        second.setRewarded(false);

        when(taskCompletionRepository.findByRewardedFalse()).thenReturn(List.of(first, second));
        when(kafkaTemplate.send(eq("payment-flow"), any(PaymentFlowEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(taskCompletionRepository.save(any(TaskCompletion.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduledRewardService.processRewards();

        verify(balanceService).addAmount("a", new BigDecimal("100"));
        verify(balanceService).addAmount("b", new BigDecimal("100"));
        verify(kafkaTemplate, times(2)).send(eq("payment-flow"), any(PaymentFlowEvent.class));
        verify(taskCompletionRepository, times(2)).save(any(TaskCompletion.class));
        assertThat(first.isRewarded()).isTrue();
        assertThat(second.isRewarded()).isTrue();
    }
}
