package com.example.taskmanager;


import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.mongodb.client.MongoCollection;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.HashSet;

@UtilityClass
public class DataModelUtils {

    public static long getEntriesCount(final MongoTemplate mongoTemplate, final String collectionName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        return collection.countDocuments();
    }


    public static Task.TaskBuilder<?, ?> prepareTask() {
        return Task.builder()
            .description("This is a test task.")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .status(TaskStatus.NEW)
            .assigneeId("testAssigneeId")
            .observerIds(new HashSet<>());
    }

    public static User.UserBuilder<?, ?> prepareUser() {
        return User.builder()
            .email("testuser@example.com");
    }

    public static User.UserBuilder<?, ?> prepareUserWithPredefinedId() {
        return User.builder()
            .id(new ObjectId().toString())  // Генерация уникального идентификатора на клиенте
            .email("testuserWithId@example.com");
    }
}
