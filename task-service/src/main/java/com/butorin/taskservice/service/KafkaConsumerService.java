package com.butorin.taskservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "task-created", groupId = "task-service-group")
    public void listenTaskCreated(String message) {
        log.info("Получено сообщение из топика task-created: {}", message);
    }

    @KafkaListener(topics = "task-assigned", groupId = "task-service-group")
    public void listenTaskAssigned(String message) {
        log.info("Получено сообщение из топика task-assigned: {}", message);
    }
}
