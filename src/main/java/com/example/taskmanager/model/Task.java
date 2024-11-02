package com.example.taskmanager.model;

import com.example.taskmanager.TaskStatus;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "tasks")
public record Task(
    ObjectId id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt,
    TaskStatus status,
    ObjectId authorId,
    ObjectId assigneeId,
    Set<ObjectId> observerIds,
    @ReadOnlyProperty User author,
    @ReadOnlyProperty User assignee,
    @ReadOnlyProperty Set<User> observers
) {

    public static Task createTaskWithAuthor(Task task, ObjectId authorId, User author) {
        return new Task(
            task.id(),
            task.name(),
            task.description(),
            Instant.now(),
            Instant.now(),
            task.status(),
            authorId,
            null,
            new HashSet<>(),
            author,
            null,
            new HashSet<>()
        );
    }
}

