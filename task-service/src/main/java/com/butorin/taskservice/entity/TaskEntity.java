package com.butorin.taskservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;

    String description;

    @Enumerated(EnumType.STRING)
    Status status;
}
