package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = MongoInitializer.class)
public class BaseTestConfig {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ReactiveMongoTemplate mongoTemplate;

    public static String TASKS = "tasks";

    public static String USERS = "users";

    @BeforeEach
    public void setup() {
        mongoTemplate.dropCollection(TASKS).block();
        mongoTemplate.dropCollection(USERS).block();
    }


}
