package com.butorin.accounting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_completions")
@Getter
@Setter
public class TaskCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    private String userId;
    private LocalDateTime completedAt;
    private boolean rewarded = false; // начислено или нет
}
