package com.butorin.accounting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.butorin.accounting.entity.TaskCompletion;
import com.butorin.accounting.repository.TaskCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskCompletionRepository taskCompletionRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task-flow", groupId = "accounting-group")
    public void handleTaskCompleted(String message) {
        try {
            var json = objectMapper.readTree(message);
            String eventType = json.get("eventType").asText();

            if ("COMPLETED".equals(eventType)) {
                Long taskId = json.get("taskId").asLong();
                String userId = json.get("userId").asText();

                TaskCompletion completion = new TaskCompletion();
                completion.setTaskId(taskId);
                completion.setUserId(userId);
                completion.setCompletedAt(LocalDateTime.now());
                completion.setRewarded(false);

                taskCompletionRepository.save(completion);
                System.out.println("✅ Сохранено выполнение задачи " + taskId);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
