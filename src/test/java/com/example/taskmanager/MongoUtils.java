package com.example.taskmanager;


import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.mongodb.client.MongoCollection;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.HashSet;

@UtilityClass
public class MongoUtils {

    public static long getEntriesCount(final MongoTemplate mongoTemplate, final String collectionName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        return collection.countDocuments();
    }


    public static Task.TaskBuilder<?, ?> prepareTask() {
        return Task.builder()
            .id("testTaskId")
            .description("This is a test task.")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .status(TaskStatus.NEW)
            .authorId("testAuthorId")
            .assigneeId("testAssigneeId")
            .observerIds(new HashSet<>());
    }

    public static User.UserBuilder<?, ?> prepareUser() {
        return User.builder()
            .id("testUserId")
            .username("Test User")
            .email("testuser@example.com");
    }
}
