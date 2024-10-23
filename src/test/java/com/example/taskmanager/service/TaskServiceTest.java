package com.example.taskmanager.service;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.example.taskmanager.DataModelUtils.*;
import static org.assertj.core.api.Assertions.assertThat;


public class TaskServiceTest extends BaseTestConfig {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    private TaskRepository rut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService sut;

    @Test
    public void testUpdate() {
        User user = new User(new ObjectId(), "d", "d@test.ru");
        userRepository.save(user).block();

        Task tInitial = prepareTask().name("No update")
            .id(new ObjectId())
            .description("Initial description")
            .authorId(user.id())
            .build();
        rut.save(tInitial).block();
        Task tUpdated = prepareTask().id(new ObjectId())
            .name("Updated Name")
            .description("Updated description")
            .authorId(user.id())
            .build();
        Mono<Task> updatedTask = sut.updateTask(tInitial.getId(), tUpdated);

        StepVerifier.create(updatedTask).assertNext(updatedTaskFound -> {
            assertThat(updatedTaskFound).isNotNull();
            assertThat(updatedTaskFound.getName()).isEqualTo("Updated Name");
            assertThat(updatedTaskFound.getDescription()).isEqualTo("Updated description");
            assertThat(updatedTaskFound.getId()).isEqualTo(tUpdated.getId());
        }).verifyComplete();
    }

    @Test
    public void testCreate() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore).isZero();

        User user = new User(new ObjectId(), "m", "m@test.ru");
        userRepository.save(user).block();


        Task newTestTask = prepareTask().name("No update").description("Initial description")
            .authorId(user.id())
            .build();


        final ObjectId userId = user.id();
        Mono<Task> createdTaskMono = sut.createTask(newTestTask, userId);

        StepVerifier.create(createdTaskMono).assertNext(createdTask -> {
            assertThat(createdTask.getName()).isEqualTo("No update");
            assertThat(createdTask.getDescription()).isEqualTo("Initial description");
            assertThat(createdTask.getAuthorId()).isEqualTo(userId);
        }).verifyComplete();

        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGetAllTasks() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore).isZero();

        User user = new User(new ObjectId(), "m", "m@test.ru");
        userRepository.save(user).block();

        Task testTask = prepareTask().name("No update")
            .description("Initial description")
            .authorId(user.id())  // Указываем автора
            .build();


        final ObjectId userId = user.id();
        Mono<Task> createdTaskMono = sut.createTask(testTask, userId);

        StepVerifier.create(createdTaskMono).assertNext(createdTask -> {
            assertThat(createdTask.getName()).isEqualTo("No update");
            assertThat(createdTask.getAuthorId()).isEqualTo(userId);
        }).verifyComplete();

        Flux<Task> allTasks = sut.getAllTasks();
        List<Task> block = allTasks.collectList().block();
        assertThat(block).hasSize(1);

        StepVerifier.create(allTasks)
            .expectNextMatches(task -> task.getName().equals("No update"))
            .verifyComplete();
    }

}
