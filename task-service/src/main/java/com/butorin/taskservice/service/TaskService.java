package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.TaskCreatedFlowEvent;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.dto.TaskResponseDTO;
import com.butorin.taskservice.dto.TaskStreamEvent;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.butorin.taskservice.entity.UserEntity;
import com.butorin.taskservice.repository.TaskRepository;
import com.butorin.taskservice.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public TaskResponseDTO getTaskById(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id).orElseThrow();
        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId(taskEntity.getId());
        taskResponseDTO.setName(taskEntity.getName());
        taskResponseDTO.setDescription(taskEntity.getDescription());
        taskResponseDTO.setStatus(taskEntity.getStatus());
        return taskResponseDTO;
    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        if (taskRepository.existsByName(taskRequestDTO.getName())) {
            throw new RuntimeException("Задача с таким именем уже есть");
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setName(taskRequestDTO.getName());
        taskEntity.setDescription(taskRequestDTO.getDescription());
        taskEntity.setStatus(Status.NEW);
        taskRepository.save(taskEntity);

        kafkaProducerService.sendTaskCreated(taskEntity);

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent();
        taskStreamEvent.setId(taskEntity.getId());
        taskStreamEvent.setName(taskEntity.getName());
        taskStreamEvent.setDescription(taskEntity.getDescription());
        taskStreamEvent.setStatus(taskEntity.getStatus());
        taskStreamEvent.setAssigneeId(taskEntity.getUser() != null ? taskEntity.getUser().getId() : null);
        taskStreamEvent.setEventTime(LocalDateTime.now());
        kafkaProducerService.sendTaskStreamEvent(taskStreamEvent);

        TaskCreatedFlowEvent taskCreatedFlowEvent = new TaskCreatedFlowEvent();
        taskCreatedFlowEvent.setTaskId(taskEntity.getId());
        taskCreatedFlowEvent.setCreatedAt(LocalDateTime.now());
        kafkaProducerService.sendTaskCreatedFlowEvent(taskCreatedFlowEvent);

        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId(taskEntity.getId());
        taskResponseDTO.setName(taskEntity.getName());
        taskResponseDTO.setDescription(taskEntity.getDescription());
        taskResponseDTO.setStatus(taskEntity.getStatus());
        return taskResponseDTO;
    }

    public TaskResponseDTO changeTaskStatus(Long id, Status status) {
        TaskEntity taskEntity = taskRepository.findById(id).orElseThrow();
        taskEntity.setStatus(status);
        taskRepository.save(taskEntity);
        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId(taskEntity.getId());
        taskResponseDTO.setName(taskEntity.getName());
        taskResponseDTO.setDescription(taskEntity.getDescription());
        taskResponseDTO.setStatus(taskEntity.getStatus());
        return taskResponseDTO;
    }

    public TaskResponseDTO assignUserToTask(Long taskId, Long userId) {
        TaskEntity taskEntity = taskRepository.findById(taskId).orElseThrow();
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        taskEntity.setUser(userEntity);
        taskRepository.save(taskEntity);

        kafkaProducerService.sendTaskAssigned(taskId,userId);

        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId(taskEntity.getId());
        taskResponseDTO.setName(taskEntity.getName());
        taskResponseDTO.setDescription(taskEntity.getDescription());
        taskResponseDTO.setStatus(taskEntity.getStatus());
        return taskResponseDTO;
    }

    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        Page<TaskEntity> taskPage = taskRepository.findAll(pageable);

        List<TaskResponseDTO> taskResponseDTOs = new ArrayList<>();
        for (TaskEntity entity : taskPage.getContent()) {
            TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
            taskResponseDTO.setId(entity.getId());
            taskResponseDTO.setName(entity.getName());
            taskResponseDTO.setDescription(entity.getDescription());
            taskResponseDTO.setStatus(entity.getStatus());
            taskResponseDTOs.add(taskResponseDTO);
        }
        return new PageImpl<>(taskResponseDTOs, pageable, taskPage.getTotalElements());
    }
}
