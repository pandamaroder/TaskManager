package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "observers", ignore = true)
    Task toEntity(TaskDTO taskDTO);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "observers", ignore = true)
    TaskDTO toDTO(Task task);

    UserDTO toUserDTO(User user);  // Этот метод можно добавить

    User toUser(UserDTO userDTO);
}
