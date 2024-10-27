package com.example.taskmanager.service;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.TaskStatus;
import com.example.taskmanager.exeption.TaskNotFoundException;
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

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareDifferentUser;
import static com.example.taskmanager.DataModelUtils.prepareTask;
import static com.example.taskmanager.DataModelUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TaskServiceTest extends BaseTestConfig {

    @Autowired
    private TaskRepository rut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService sut;

    @Test
    public void testUpdate() {
        User user = prepareUser();
        userRepository.save(user).block();

        Task tInitial = prepareTask();

        Task block1 = rut.save(tInitial).block();
        assertThat(block1)
            .isNotNull();
        assertThat(block1.id())
            .isEqualTo(tInitial.id());
        final ObjectId initialTaskId = tInitial.id();
        Task block2 = rut.findById(initialTaskId).block();
        assertThat(block2)
            .isNotNull();

        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isEqualTo(1);

        Task tUpdated = new Task(
            new ObjectId(),
            "Updated Name",
            "Updated description",
            tInitial.createdAt(),
            Instant.now(),
            TaskStatus.IN_PROGRESS,
            user.id(),
            null,
            new HashSet<>(),
            user,
            null,
            new HashSet<>()
        );

        Mono<Task> updatedTask = sut.updateTask(tInitial.id(), tUpdated);
        Task block = updatedTask.block();
        assertThat(block)
            .isNotNull();
        assertThat(block.id())
            .isEqualTo(tInitial.id());

    }

    @Test
    public void testCreate() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isZero();

        User user = prepareUser();
        userRepository.save(user).block();

        Task newTestTask = new Task(
            new ObjectId(),
            "Init",
            "dfdfdf",
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

        final ObjectId userId = user.id();
        Task createdTask = sut.createTask(newTestTask, userId).block();
        assertThat(createdTask)
            .isNotNull();
        assertThat(createdTask.id())
            .isNotNull()
                .isEqualTo(newTestTask.id());
        assertThat(createdTask.authorId())
            .isNotNull()
            .isEqualTo(userId);
        assertThat(createdTask.name())
            .isEqualTo(newTestTask.name());

        long countAfter = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countAfter - countBefore).isPositive()
            .isEqualTo(1);
    }

    @Test
    public void testGetAllTasks() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isZero();

        User user = new User(new ObjectId(), "m", "m@test.ru");
        userRepository.save(user).block();

        Task testTask = new Task(
            new ObjectId(),
            "No update",
            "gjghkghfgh",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            user.id(),
            null,
            new HashSet<>(),
            user,
            null,
            new HashSet<>()
        );

        final ObjectId userId = user.id();
        Mono<Task> createdTaskMono = sut.createTask(testTask, userId);

        StepVerifier.create(createdTaskMono).assertNext(createdTask -> {
            assertThat(createdTask.name()).isEqualTo("No update");
            assertThat(createdTask.authorId()).isEqualTo(userId);
        }).verifyComplete();

        Flux<Task> allTasks = sut.getAllTasks();
        List<Task> block = allTasks.collectList().block();
        assertThat(block).hasSize(1);

        StepVerifier.create(allTasks)
            .expectNextMatches(task -> task.name().equals("No update"))
            .verifyComplete();
    }

    @Test
    public void testAddObserverSuccessfully() {
        User observer = prepareUser();
        User author = prepareDifferentUser();
        userRepository.save(observer).block();
        userRepository.save(author).block();
        Task initialTask = new Task(
            new ObjectId(),
            "Initial Task",
            "Task description",
            Instant.now(),
            Instant.now(),
            TaskStatus.IN_PROGRESS,
            author.id(),
            null,
            new HashSet<>(),
            author,
            null,
            new HashSet<>()
        );

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> updatedTaskMono = sut.addObserver(savedTask.id(), observer.id());

        Task updatedTask = updatedTaskMono.block();
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.observerIds())
            .contains(observer.id());

        assertThat(updatedTask.observers())
            .isNotNull()
            .contains(observer);
    }

    @Test
    public void testAddExistingObserverThrowsException() {
        User observer = new User(new ObjectId(), "Observer", "observer@test.com");
        userRepository.save(observer).block();

        Task initialTask = new Task(
            new ObjectId(),
            "Initial Task",
            "Task description",
            Instant.now(),
            Instant.now(),
            TaskStatus.IN_PROGRESS,
            new ObjectId(),
            new ObjectId(),
            new HashSet<>(Set.of(observer.id())),
            null,
            null,
            new HashSet<>()
        );

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> result = sut.addObserver(savedTask.id(), observer.id());

        assertThat(result).isNotNull();
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> result.block());
    }

    @Test
    public void testAddObserverToNonExistentTask() {
        User observer = new User(new ObjectId(), "Observer", "observer@test.com");
        userRepository.save(observer).block();

        Mono<Task> result = sut.addObserver(new ObjectId(), observer.id());

        assertThat(result).isNotNull();
        assertThatThrownBy(result::block)
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task or observer not found");

    }

    @Test
    public void testAddObserverWithNonExistentObserver() {
        Task initialTask = new Task(
            new ObjectId(),
            "Initial Task",
            "Task description",
            Instant.now(),
            Instant.now(),
            TaskStatus.IN_PROGRESS,
            new ObjectId(),
            new ObjectId(),
            new HashSet<>(),
            null,
            null,
            new HashSet<>()
        );

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> result = sut.addObserver(savedTask.id(), new ObjectId());

        assertThat(result).isNotNull();
        assertThatThrownBy(result::block)
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task or observer not found");
    }

}
