package com.butorin.taskservice.service;

import com.butorin.taskservice.dto.UserRequestDTO;
import com.butorin.taskservice.dto.UserResponseDTO;
import com.butorin.taskservice.entity.UserEntity;
import com.butorin.taskservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        List<UserResponseDTO> userResponseDTOS = new ArrayList<>();
        for (UserEntity user : users) {
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(user.getId());
            userResponseDTO.setName(user.getName());
            userResponseDTO.setEmail(user.getEmail());
            userResponseDTOS.add(userResponseDTO);
        }
        return userResponseDTOS;
    }

    public UserResponseDTO getUserById(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setName(user.getName());
        userResponseDTO.setEmail(user.getEmail());
        return userResponseDTO;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(userRequestDTO.getName());
        userEntity.setEmail(userRequestDTO.getEmail());
        userRepository.save(userEntity);
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userEntity.getId());
        userResponseDTO.setName(userRequestDTO.getName());
        userResponseDTO.setEmail(userRequestDTO.getEmail());
        return userResponseDTO;
    }
}
