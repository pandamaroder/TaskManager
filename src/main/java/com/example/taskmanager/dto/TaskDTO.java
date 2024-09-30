package com.example.taskmanager.dto;

import com.example.taskmanager.TaskStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class TaskDTO {
    private String id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private TaskStatus status;
    private String authorId;
    private String assigneeId;
    private Set<String> observerIds;

    private UserDTO author;
    private UserDTO assignee;
    private Set<UserDTO> observers;
}
