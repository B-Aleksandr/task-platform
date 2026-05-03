package com.butorin.accounting.service;

import com.butorin.accounting.entity.TaskCompletion;
import com.butorin.accounting.repository.TaskCompletionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TaskEventConsumer.class)
@Import(JacksonAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class TaskEventConsumerTest {

    @Autowired
    private TaskEventConsumer taskEventConsumer;

    @MockitoBean
    private TaskCompletionRepository taskCompletionRepository;

    @Test
    void handleTaskCompleted_whenEventTypeCompleted_savesTaskCompletion() {
        String message = """
                {"eventType":"COMPLETED","taskId":99,"userId":"user-x"}
                """;

        taskEventConsumer.handleTaskCompleted(message);

        org.mockito.ArgumentCaptor<TaskCompletion> captor =
                org.mockito.ArgumentCaptor.forClass(TaskCompletion.class);
        verify(taskCompletionRepository).save(captor.capture());
        TaskCompletion saved = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(saved.getTaskId()).isEqualTo(99L);
        org.assertj.core.api.Assertions.assertThat(saved.getUserId()).isEqualTo("user-x");
        org.assertj.core.api.Assertions.assertThat(saved.isRewarded()).isFalse();
        org.assertj.core.api.Assertions.assertThat(saved.getCompletedAt()).isNotNull();
    }

    @Test
    void handleTaskCompleted_whenNotCompleted_doesNotSave() {
        taskEventConsumer.handleTaskCompleted("{\"eventType\":\"OTHER\"}");

        verify(taskCompletionRepository, never()).save(any());
    }

    @Test
    void handleTaskCompleted_whenInvalidJson_doesNotSave() {
        taskEventConsumer.handleTaskCompleted("not-json");

        verify(taskCompletionRepository, never()).save(any());
    }
}
