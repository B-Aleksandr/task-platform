package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.TaskCreatedFlowEvent;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.dto.TaskResponseDTO;
import com.butorin.taskservice.dto.TaskStreamEvent;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.butorin.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaProducerService kafkaProducerService;

    public TaskEntity getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public TaskEntity createTask(TaskRequestDTO taskRequestDTO) {
        if (taskRepository.existsByName(taskRequestDTO.getName())) {
            throw new RuntimeException("Задача с таким именем уже есть");
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setName(taskRequestDTO.getName());
        taskEntity.setDescription(taskRequestDTO.getDescription());
        taskEntity.setStatus(Status.NEW);
        taskRepository.save(taskEntity);

        return taskEntity;
    }

    public void sendTaskCreatedEvents(TaskEntity taskEntity) {
        kafkaProducerService.sendTaskCreated(taskEntity);

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent();
        taskStreamEvent.setId(taskEntity.getId());
        taskStreamEvent.setName(taskEntity.getName());
        taskStreamEvent.setDescription(taskEntity.getDescription());
        taskStreamEvent.setStatus(taskEntity.getStatus());
        taskStreamEvent.setAssigneeId(taskEntity.getAssigneeId());
        taskStreamEvent.setEventTime(LocalDateTime.now());
        kafkaProducerService.sendTaskStreamEvent(taskStreamEvent);

        TaskCreatedFlowEvent taskCreatedFlowEvent = new TaskCreatedFlowEvent();
        taskCreatedFlowEvent.setTaskId(taskEntity.getId());
        taskCreatedFlowEvent.setCreatedAt(LocalDateTime.now());
        kafkaProducerService.sendTaskCreatedFlowEvent(taskCreatedFlowEvent);
    }

    public TaskEntity changeTaskStatus(Long id, Status status) {
        TaskEntity taskEntity = taskRepository.findById(id).orElseThrow();
        taskEntity.setStatus(status);
        taskRepository.save(taskEntity);
        return taskEntity;
    }

    public TaskEntity assignUserToTask(Long taskId, String userId) {
        TaskEntity taskEntity = taskRepository.findById(taskId).orElseThrow();
        taskEntity.setAssigneeId(userId);
        taskRepository.save(taskEntity);

        kafkaProducerService.sendTaskAssigned(taskId,userId);

        return taskEntity;
    }

    public Page<TaskEntity> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public String completeTask (Long taskId, String userId) {
        TaskEntity taskEntity = taskRepository.findById(taskId).orElseThrow();
        taskEntity.setStatus(Status.DONE);
        taskRepository.save(taskEntity);
        kafkaProducerService.sendTaskCompletedToFlow(taskId, userId);
        return "Пользователь " + userId + " выполнил задачу " + taskId;
    }

}
