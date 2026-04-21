package com.butorin.taskservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponseDTO {

    Long id;
    String name;
    String email;
}
