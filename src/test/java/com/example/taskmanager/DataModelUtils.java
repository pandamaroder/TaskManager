package com.example.taskmanager;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;

@UtilityClass
public class DataModelUtils {

    public static long getEntriesCount(final ReactiveMongoTemplate mongoTemplate2, final String collectionName) {
        Long block = mongoTemplate2
            .getCollection(collectionName)
            .flatMap(e -> Mono.from(e.countDocuments()))
            .block();
        return Objects.requireNonNullElse(block, 0L);

    }

    public static Task prepareTask() {
        return new Task(
            new ObjectId(),
            "TestTask",
            "This is a test task.",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            new ObjectId(),
            new ObjectId(),
            new HashSet<>(),
            null, // author
            null, // assignee
            new HashSet<>() // observers
        );

    }

    public static Task prepareDifferentTask() {
        return new Task(
            new ObjectId(),
            "TestTask2",
            "This is a test task2.",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            new ObjectId(),
            new ObjectId(),
            new HashSet<>(),
            null, // author
            null, // assignee
            new HashSet<>() // observers
        );

    }

    public static User prepareUser() {

        return new User(new ObjectId(), "defaultUserName", "testuser@example.com");
    }

    public static User prepareDifferentUser() {

        return new User(new ObjectId(), "defaultUserName2", "testuser2@example.com");
    }
}
