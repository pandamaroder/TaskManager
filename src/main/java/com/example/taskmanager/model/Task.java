package com.example.taskmanager.model;

import com.example.taskmanager.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import nonapi.io.github.classgraph.json.Id;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Getter
@SuperBuilder
@Document(collection = "tasks")
@AllArgsConstructor
public class Task {

    private ObjectId id  = new ObjectId();;
    private String name;
    private String description;
    Instant createdAt; // при создании объекта через конструктор
    Instant updatedAt;
    private TaskStatus status;
    private ObjectId authorId;
    private ObjectId assigneeId;
    private Set<ObjectId> observerIds;

    @ReadOnlyProperty
    private User author;
    @ReadOnlyProperty
    private User assignee;
    @ReadOnlyProperty
    private Set<User> observers;

    public Task() {
        this.id = new ObjectId(); // Или любое другое значение по умолчанию
    }

}
