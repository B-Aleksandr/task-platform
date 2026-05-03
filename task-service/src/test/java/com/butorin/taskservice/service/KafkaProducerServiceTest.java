package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.TaskCreatedFlowEvent;
import com.butorin.taskservice.dto.TaskStreamEvent;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = KafkaProducerService.class)
@Import(JacksonAutoConfiguration.class)
class KafkaProducerServiceTest {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void sendTaskCreated_serializesAndSends() throws Exception {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-created"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        TaskEntity task = new TaskEntity(1L, "n", null, "d", Status.NEW);
        kafkaProducerService.sendTaskCreated(task);

        org.mockito.ArgumentCaptor<String> captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("task-created"), captor.capture());
        assertThat(captor.getValue()).isEqualTo(objectMapper.writeValueAsString(task));
    }

    @Test
    void sendTaskAssigned_sendsJsonPayload() {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-assigned"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        kafkaProducerService.sendTaskAssigned(9L, "u");

        verify(kafkaTemplate).send("task-assigned", "{\"taskId\":9,\"userId\":\"u\"}");
    }

    @Test
    void sendTaskStreamEvent_sendsToTaskStreamTopic() throws Exception {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-stream"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        TaskStreamEvent event = new TaskStreamEvent();
        event.setId(1L);
        event.setName("n");
        kafkaProducerService.sendTaskStreamEvent(event);

        verify(kafkaTemplate).send(eq("task-stream"), eq(objectMapper.writeValueAsString(event)));
    }

    @Test
    void sendTaskCreatedFlowEvent_sendsToTaskFlowTopic() throws Exception {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-flow"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        TaskCreatedFlowEvent event = new TaskCreatedFlowEvent();
        event.setTaskId(3L);
        kafkaProducerService.sendTaskCreatedFlowEvent(event);

        verify(kafkaTemplate).send(eq("task-flow"), eq(objectMapper.writeValueAsString(event)));
    }

    @Test
    void sendTaskCompletedToFlow_sendsCompletedEnvelope() {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-flow"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        kafkaProducerService.sendTaskCompletedToFlow(1L, "user-x");

        verify(kafkaTemplate).send("task-flow",
                "{\"taskId\":1,\"userId\":\"user-x\",\"eventType\":\"COMPLETED\"}");
    }

    @Test
    void sendTaskCompleted_sendsToCompletedTopic() {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.eq("task-completed"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        kafkaProducerService.sendTaskCompleted(2L, "u2");

        verify(kafkaTemplate).send("task-completed", "{\"taskId\":2,\"userId\":\"u2\"}");
    }
}
