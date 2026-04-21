package com.butorin.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreatedFlowEvent {

    String userId;
    LocalDateTime createdAt;
    String eventType = "USER_CREATED";
}
