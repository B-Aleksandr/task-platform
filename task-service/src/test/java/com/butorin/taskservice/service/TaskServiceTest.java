package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.TaskCreatedFlowEvent;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.dto.TaskStreamEvent;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.butorin.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TaskService.class)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @Test
    void createTask_whenNameUnique_savesAndReturns() {
        when(taskRepository.existsByName("uniq")).thenReturn(false);
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> {
            TaskEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        TaskEntity created = taskService.createTask(new TaskRequestDTO("uniq", "desc"));

        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getStatus()).isEqualTo(Status.NEW);
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void createTask_whenDuplicateName_throws() {
        when(taskRepository.existsByName("dup")).thenReturn(true);

        assertThatThrownBy(() -> taskService.createTask(new TaskRequestDTO("dup", "d")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void sendTaskCreatedEvents_sendsAllKafkaMessages() {
        TaskEntity task = new TaskEntity(7L, "n", null, "d", Status.NEW);

        taskService.sendTaskCreatedEvents(task);

        verify(kafkaProducerService).sendTaskCreated(task);
        org.mockito.ArgumentCaptor<TaskStreamEvent> streamCap =
                org.mockito.ArgumentCaptor.forClass(TaskStreamEvent.class);
        verify(kafkaProducerService).sendTaskStreamEvent(streamCap.capture());
        assertThat(streamCap.getValue().getId()).isEqualTo(7L);

        org.mockito.ArgumentCaptor<TaskCreatedFlowEvent> flowCap =
                org.mockito.ArgumentCaptor.forClass(TaskCreatedFlowEvent.class);
        verify(kafkaProducerService).sendTaskCreatedFlowEvent(flowCap.capture());
        assertThat(flowCap.getValue().getTaskId()).isEqualTo(7L);
    }

    @Test
    void assignUserToTask_savesAndNotifiesKafka() {
        TaskEntity task = new TaskEntity(1L, "t", null, "d", Status.NEW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskEntity result = taskService.assignUserToTask(1L, "worker");

        assertThat(result.getAssigneeId()).isEqualTo("worker");
        verify(kafkaProducerService).sendTaskAssigned(1L, "worker");
    }

    @Test
    void completeTask_setsDoneAndSendsFlow() {
        TaskEntity task = new TaskEntity(2L, "t", "w", "d", Status.IN_PROGRESS);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String msg = taskService.completeTask(2L, "w");

        assertThat(msg).contains("w").contains("2");
        verify(kafkaProducerService).sendTaskCompletedToFlow(2L, "w");
    }

    @Test
    void getAllTasks_delegatesToRepository() {
        Page<TaskEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);

        assertThat(taskService.getAllTasks(PageRequest.of(0, 5))).isSameAs(page);
    }

    @Test
    void changeTaskStatus_updatesEntity() {
        TaskEntity task = new TaskEntity(3L, "t", null, "d", Status.NEW);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskEntity updated = taskService.changeTaskStatus(3L, Status.CANCELLED);

        assertThat(updated.getStatus()).isEqualTo(Status.CANCELLED);
    }
}
