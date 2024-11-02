package com.example.taskmanager.controller;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.TaskStatus;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.model.Task.createTaskWithAuthor;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskControllerTest extends BaseTestConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private ObjectId userId;


    @Test
    public void testCreateTask() {
        AtomicReference<ObjectId> taskId = null;
        User block1 = userService.findUserById(userId).block();
        assertThat(block1)
            .isNotNull();
        assertThat(block1.id())
            .isEqualTo(userId);

        final long countUsersBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        final long countTaskBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countTaskBefore).isEqualTo(0);
        assertThat(countUsersBefore).isEqualTo(1);

        Task tInitial = new Task(
            new ObjectId(),
            "No update",
            "Initial description",
            now(),
            now(),
            TaskStatus.NEW,
            null, // authorId изначально не указываем
            null,
            new HashSet<>(),
            null,
            null,
            new HashSet<>()
        );

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
                assertThat(createdTask.name())
                    .isEqualTo(tInitial.name());
                assertThat(createdTask.author().username())
                    .isEqualTo("Test");
                taskId.set(createdTask.id());
                assertThat(createdTask.authorId()).isEqualTo(userId);
            });

        Task block = taskService.getTaskById(taskId.get()).block();
        assertThat(block.authorId())
            .isEqualTo(userId);
    }

    @Test
    public void testGetAllTasksVerifyReturnTask() {
        User testUser = new User(new ObjectId(), "Test", "test@test.ru");

        userId = userService.createUser(testUser).block().id();
        Task newTestTask = new Task(
            new ObjectId(),
            "TestControllerCreateTask",
            "Initial description",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            null,
            null,
            new HashSet<>(),
            null,
            null,
            new HashSet<>()
        );

        Task taskWithAuthor = createTaskWithAuthor(newTestTask, userId, testUser);
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
