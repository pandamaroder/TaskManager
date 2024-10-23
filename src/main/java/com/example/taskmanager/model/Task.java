package com.example.taskmanager.model;

import com.example.taskmanager.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Getter
@SuperBuilder
@Document(collection = "tasks")
@NoArgsConstructor
public class Task {


    public Task(String id, String name, String description, Instant createdAt, Instant updatedAt, TaskStatus status, String authorId, String assigneeId, Set<String> observerIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.observerIds = observerIds;
    }
    @Id
    private String id;
    private String name;
    private String description;
    Instant createdAt; // при создании объекта через конструктор
    Instant updatedAt;
    private TaskStatus status;
    private String authorId;
    private String assigneeId;
    private Set<String> observerIds;

    @ReadOnlyProperty
    private User author;
    @ReadOnlyProperty
    private User assignee;
    @ReadOnlyProperty
    private Set<User> observers;
}
