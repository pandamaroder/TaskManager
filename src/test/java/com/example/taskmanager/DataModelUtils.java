package com.example.taskmanager;


import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;

import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.MongoTemplate;
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


    public static Task.TaskBuilder<?, ?> prepareTask() {
        return Task.builder()
            .description("This is a test task.")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .status(TaskStatus.NEW)
            .assigneeId(new ObjectId())
            .observerIds(new HashSet<>());
    }

    public static User prepareUser() {
        return new User(new ObjectId(), "defaultUserName", "testuser@example.com");
    }
}
