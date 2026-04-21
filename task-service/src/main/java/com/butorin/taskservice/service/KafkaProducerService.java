package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.TaskCreatedFlowEvent;
import com.butorin.taskservice.dto.TaskStreamEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TASK_CREATED_TOPIC = "task-created";
    private static final String TASK_ASSIGNED_TOPIC = "task-assigned";
    private static final String TASK_STREAM_TOPIC = "task-stream";
    private static final String TASK_FLOW_TOPIC = "task-flow";

    public void sendTaskCreated(Object task) {
        try {
            String message = objectMapper.writeValueAsString(task);
            kafkaTemplate.send(TASK_CREATED_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Возникла ошибка при отправке события создания задачи",e);
        }
    }

    public void sendTaskAssigned(Long taskId, Long userId) {
        String message = String.format("{\"taskId\":%d,\"userId\":%d}", taskId, userId);
        kafkaTemplate.send(TASK_ASSIGNED_TOPIC, message);
    }

    public void sendTaskStreamEvent(TaskStreamEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TASK_STREAM_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Возникла ошибка при отправке события создания задачи",e);
        }
    }

    public void sendTaskCreatedFlowEvent(TaskCreatedFlowEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TASK_FLOW_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Возникла ошибка при отправке события создания задачи",e);
        }
    }
}
