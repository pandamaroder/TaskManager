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

    public static long getEntriesCount(final ReactiveMongoTemplate mongoTemplate, final String collectionName) {
        Long block = mongoTemplate
            .getCollection(collectionName)
            .flatMap(e -> Mono.from(e.countDocuments()))
            .block();
        return Objects.requireNonNullElse(block, 0L);

    }

    public static Task prepareTask() {
        return new Task(
            ObjectId.get().toHexString(),
            "TestTask",
            "This is a test task.",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            null,
            null,
            new HashSet<>(),
            null, // author
            null, // assignee
            new HashSet<>() // observers
        );

    }

    public static Task prepareDifferentTask() {
        return new Task(
            ObjectId.get().toHexString(),
            "TestTask2",
            "This is a test task2.",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            null,
            null,
            new HashSet<>(),
            null, // author
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

    public static User prepareUser() {

        return new User(ObjectId.get().toHexString(), "defaultUserName", "testuser@example.com");
    }

    public static User prepareDifferentUser() {

        return new User(ObjectId.get().toHexString(), "defaultUserName2", "testuser2@example.com");
    }

}
