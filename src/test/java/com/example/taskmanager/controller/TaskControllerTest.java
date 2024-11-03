package com.example.taskmanager.controller;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.taskmanager.DataModelUtils.*;
import static com.example.taskmanager.model.Task.withAuthorId;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskControllerTest extends BaseTestConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    public void testCreateTask() {
        User testUser = prepareUser();

        String userId = userService.createUser(testUser).block().id();

        User block1 = userService.findUserById(userId).block();
        assertThat(block1)
            .isNotNull();
        assertThat(block1.id())
            .isEqualTo(userId);

        final long countUsersBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        final long countTaskBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countTaskBefore).isEqualTo(0);
        assertThat(countUsersBefore).isEqualTo(1);

        Task tInitial = prepareTask();

        Task createdTask = webTestClient.post()
            .uri("/tasks?authorId=" + userId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tInitial)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Task.class)
            .returnResult()
            .getResponseBody();

        final long countTaskAfter = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countTaskAfter).isEqualTo(1);
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.name())
            .isEqualTo(tInitial.name());
        assertThat(createdTask.author().username())
            .isEqualTo("Test");
        Task initTaskAfterSave = mongoTemplate.findById(tInitial.id(), Task.class).block();
        Task createdTaskAfterSave = mongoTemplate.findById(createdTask.id(), Task.class).block();
        assertThat(createdTask.id())
            .isEqualTo(tInitial.id());
        assertThat(createdTask.authorId()).isEqualTo(userId);


        Task block = taskService.getTaskById(createdTask.id()).block();
        assertThat(block.authorId())
            .isEqualTo(userId);
    }

    @Test
    public void testGetAllTasksVerifyReturnTask() {
        User testUser = new User(ObjectId.get().toHexString(), "Test", "test@test.ru");

        String userId = userService.createUser(testUser).block().id();

        Task taskWithAuthor = withAuthorId(userId);
        taskRepository.save(taskWithAuthor).block();
        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count).isEqualTo(1);

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
                assertThat(tasks.get(0).name()).isEqualTo("TestControllerCreateTask");
                assertThat(tasks.get(0).authorId()).isEqualTo(userId);
            });
    }
}
