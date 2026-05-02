package com.butorin.taskservice.controller;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.dto.AuthorizationParams;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.dto.TaskResponseDTO;
import com.butorin.taskservice.entity.Status;
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
        return taskService.getAllTasks(pageable);
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping
    public TaskResponseDTO createTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        return taskService.createTask(taskRequestDTO);
    }

    @PatchMapping("/{id}/status")
    public TaskResponseDTO changeTaskStatus(@PathVariable Long id, @RequestParam Status status) {
        return taskService.changeTaskStatus(id,status);
    }

    @PatchMapping("/{taskId}/assignee/{userId}")
    public TaskResponseDTO assignUserToTask(@PathVariable Long taskId, @PathVariable Long userId) {
        return taskService.assignUserToTask(taskId,userId);
    }
}
