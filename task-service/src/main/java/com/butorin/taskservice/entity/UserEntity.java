package com.butorin.taskservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<TaskEntity> tasks = new ArrayList<>();
}
