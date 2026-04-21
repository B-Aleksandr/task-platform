package com.butorin.authservice.service;

import com.butorin.authservice.dto.UserCreatedFlowEvent;
import com.butorin.authservice.dto.UserStreamEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String USER_STREAM_TOPIC = "user-stream";
    private static final String USER_FLOW_TOPIC = "user-flow";

    public void sendUserStreamEvent(UserStreamEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_STREAM_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Возникла ошибка при отправке события создания задачи",e);
        }
    }

    public void sendUserCreatedFlowEvent(UserCreatedFlowEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_FLOW_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Возникла ошибка при отправке события создания задачи",e);
        }
    }
}
