package com.butorin.taskservice.dto;

import com.butorin.taskservice.entity.Status;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskResponseDTO {

    Long id;
    String name;
    String description;
    Status status;
}
