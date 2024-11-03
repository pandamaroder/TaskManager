package com.example.taskmanager.model;

import com.example.taskmanager.TaskStatus;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "tasks")
public record Task(
    @Id
    String id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt,
    TaskStatus status,
    String authorId,
    String assigneeId,
    Set<String> observerIds,
    @ReadOnlyProperty User author,
    @ReadOnlyProperty User assignee,
    @ReadOnlyProperty Set<User> observers
) {
    public static Task withAuthor(Task task, String authorId, User author) {
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
            author, // author
            null, // assignee
            new HashSet<>() // observers
        );
    }

    public static Task withAuthorId(String authorId) {
        return new Task(
            ObjectId.get().toHexString(),
            "Default Name",
            "Default Description",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            authorId,
            null,
            new HashSet<>(),
            null,
            null,
            new HashSet<>()
        );
    }
}

