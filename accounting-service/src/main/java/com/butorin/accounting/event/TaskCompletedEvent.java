package com.butorin.accounting.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCompletedEvent {
    private Long taskId;
    private String userId;
    private String eventType;
    private LocalDateTime completedAt;
}
