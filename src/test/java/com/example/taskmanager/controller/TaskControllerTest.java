package com.example.taskmanager.controller;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareTask;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskControllerTest extends BaseTestConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private ObjectId userId;


    @BeforeEach
    public void setup() {

        User testUser = new User(new ObjectId(), "Test", "test@test.ru");

        userService.createUser(testUser).block();
        userId = testUser.id();


        //taskService.createTask(tInitial, userId).block();
    }

    @Test
    public void testCreateTask() {
        Task tInitial = prepareTask().name("No update")
            .id(new ObjectId())
            .description("Initial description")
            .build();
        taskService.createTask(tInitial, userId);
        webTestClient.post()
            .uri("/tasks?authorId=" + userId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tInitial)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Task.class)
            .consumeWith(response -> {
                Task createdTask = response.getResponseBody();
                assertThat(createdTask).isNotNull();
                //assertThat(createdTask.getName()).isEqualTo("TestControllerCreateTask");
                //assertThat(createdTask.getDescription()).isEqualTo("This is a test.");
                assertThat(createdTask.getAuthorId()).isEqualTo(userId);
            });
    }

    @Test
    public void testGetAllTasksVerifyReturnTask() {

        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count).isEqualTo(0);
        webTestClient.get()
            .uri("/tasks")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Task.class)
            .consumeWith(response -> {
                List<Task> tasks = response.getResponseBody();
                assertThat(tasks).isNotNull();
                assertThat(tasks).hasSize(1);
                assertThat(tasks.get(0).getName()).isEqualTo("TestControllerCreateTask");
                assertThat(tasks.get(0).getAuthorId()).isEqualTo(userId);
            });
    }


}
