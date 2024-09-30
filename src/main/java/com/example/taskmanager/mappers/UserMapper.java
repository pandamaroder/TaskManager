package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}
