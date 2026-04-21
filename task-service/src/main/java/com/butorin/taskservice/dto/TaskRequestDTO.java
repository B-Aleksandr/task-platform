package com.butorin.taskservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskRequestDTO {

    String name;
    String description;
}
