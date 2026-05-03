package com.butorin.taskservice.controller;

import com.butorin.starter.config.AuthAutoConfiguration;
import com.butorin.taskservice.dto.TaskRequestDTO;
import com.butorin.taskservice.entity.Status;
import com.butorin.taskservice.entity.TaskEntity;
import com.butorin.taskservice.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@Import(AuthAutoConfiguration.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getAllTasks_returnsPage() throws Exception {
        TaskEntity entity = new TaskEntity(1L, "t1", null, "d1", Status.NEW);
        when(taskService.getAllTasks(any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/tasks")
                        .header("user-id", "viewer")
                        .header("user-roles", "[\"USER\"]")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("t1"))
                .andExpect(jsonPath("$.content[0].status").value("NEW"));
    }

    @Test
    void createTask_persistsAndReturnsDto() throws Exception {
        TaskEntity saved = new TaskEntity(5L, "New task", null, "Desc", Status.NEW);
        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequestDTO("New task", "Desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New task"));

        verify(taskService).sendTaskCreatedEvents(saved);
    }

    @Test
    void assignUserToTask_updatesAssignee() throws Exception {
        TaskEntity assigned = new TaskEntity(2L, "x", "user-9", "d", Status.NEW);
        when(taskService.assignUserToTask(2L, "user-9")).thenReturn(assigned);

        mockMvc.perform(patch("/tasks/2/assignee/user-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(taskService).assignUserToTask(2L, "user-9");
    }

    @Test
    void completeTask_marksDone() throws Exception {
        TaskEntity done = new TaskEntity(3L, "done", "u1", "d", Status.DONE);
        when(taskService.completeTask(3L, "u1")).thenReturn("ok");
        when(taskService.getTaskById(3L)).thenReturn(done);

        mockMvc.perform(post("/tasks/3/complete")
                        .header("user-id", "u1")
                        .header("user-roles", "[\"USER\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(taskService).completeTask(3L, "u1");
        verify(taskService).getTaskById(3L);
    }

    @Test
    void changeTaskStatus_updatesStatus() throws Exception {
        TaskEntity updated = new TaskEntity(4L, "n", null, "d", Status.IN_PROGRESS);
        when(taskService.changeTaskStatus(eq(4L), eq(Status.IN_PROGRESS))).thenReturn(updated);

        mockMvc.perform(patch("/tasks/4/status").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
