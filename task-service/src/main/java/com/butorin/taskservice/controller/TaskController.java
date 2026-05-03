package com.butorin.taskservice.controller;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.dto.AuthorizationParams;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.dto.TaskResponseDTO;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.butorin.taskservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Page<TaskResponseDTO> getAllTasks(@Auth AuthorizationParams auth,
                                             @PageableDefault(size = 10) Pageable pageable) {
        System.out.println("=== User from headers ===");
        System.out.println("User ID: " + auth.getUserId());
        System.out.println("Roles: " + auth.getRoles());
        return taskService.getAllTasks(pageable).map(this::mapToDTO);
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(@PathVariable Long id) {
        return mapToDTO(taskService.getTaskById(id));
    }

    @PostMapping
    public TaskResponseDTO createTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        TaskEntity task = taskService.createTask(taskRequestDTO);
        taskService.sendTaskCreatedEvents(task);
        return mapToDTO(task);
    }

    @PostMapping("/{taskId}/complete")
    public TaskResponseDTO completeTask(@PathVariable Long taskId, @Auth AuthorizationParams auth) {
        taskService.completeTask(taskId, auth.getUserId());
        return getTaskById(taskId);
    }

    @PatchMapping("/{id}/status")
    public TaskResponseDTO changeTaskStatus(@PathVariable Long id, @RequestParam Status status) {
        return mapToDTO(taskService.changeTaskStatus(id,status));
    }

    @PatchMapping("/{taskId}/assignee/{userId}")
    public TaskResponseDTO assignUserToTask(@PathVariable Long taskId, @PathVariable String userId) {
        System.out.println("✅ Задача " + taskId + " назначена пользователю " + userId);
        return mapToDTO(taskService.assignUserToTask(taskId,userId));
    }

    private TaskResponseDTO mapToDTO(TaskEntity entity) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
