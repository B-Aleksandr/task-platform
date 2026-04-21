package com.butorin.taskservice.dto;

import com.butorin.taskservice.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskStreamEvent {
    Long id;
    String name;
    String description;
    Status status;
    Long assigneeId;
    LocalDateTime eventTime;
}
