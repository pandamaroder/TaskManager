package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = MongoInitializer.class)
public class BaseTestConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    public static String TASKS = "tasks";

    public static String USERS = "users";

    @BeforeEach
    public void setup() {
        mongoTemplate.dropCollection(TASKS);
        mongoTemplate.dropCollection(USERS);
    }


}
