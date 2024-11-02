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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.taskmanager.DataModelUtils.*;
import static com.example.taskmanager.model.Task.withAuthor;
import static com.example.taskmanager.model.Task.withCreatorTaskId;
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

        Task tInitial = withCreatorTaskId(user.id());

        Task block1 = rut.save(tInitial).block();
        assertThat(block1).isNotNull();
        assertThat(block1.id()).isEqualTo(tInitial.id());
        final String initialTaskId = tInitial.id();
        Task block2 = rut.findById(new ObjectId(initialTaskId)).block();
        assertThat(block2).isNotNull();

        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isEqualTo(1);

        Task tUpdated = withCreatorTaskId(user.id());

        Mono<Task> updatedTask = sut.updateTask(tInitial.id(), tUpdated);
        Task block = updatedTask.block();
        assertThat(block).isNotNull();
        assertThat(block.id()).isEqualTo(tInitial.id());

    }

    @Test
    public void testCreate() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isZero();

        User user = prepareUser();
        userRepository.save(user).block();

        Task newTestTask = prepareTask();

        final String userId = user.id();
        Task createdTask = withAuthor(newTestTask, userId, user);
        Task savedTask = sut.createTask(createdTask, userId).block();

        // Проверки
        assertThat(savedTask).isNotNull();
        assertThat(savedTask.id()).isNotNull().isEqualTo(newTestTask.id());
        assertThat(savedTask.authorId()).isNotNull().isEqualTo(userId);
        assertThat(savedTask.name()).isEqualTo(newTestTask.name());

        // Проверка увеличения числа записей в коллекции
        long countAfter = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countAfter - countBefore).isPositive().isEqualTo(1);
    }

    @Test
    public void testGetAllTasksWithAuthors() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isZero();

        User user = prepareUser();
        userRepository.save(user).block();

        Task testTask = prepareTask();
        final String userId = user.id();

        Task taskWithAuthor = sut.createTask(testTask, userId).block();

        Flux<Task> allTasks = sut.getAllTasks();
        List<Task> block = allTasks.collectList().block();
        assertThat(block).hasSize(1);
        assertThat(block.get(0).name()).isEqualTo(taskWithAuthor.name());

    }

    @Test
    public void testAddObserverSuccessfully() {
        User observer = prepareUser();
        User author = prepareDifferentUser();
        userRepository.save(observer).block();
        userRepository.save(author).block();

        Task initialTask = withCreatorTaskId(author.id());

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> updatedTaskMono = sut.addObserver(savedTask.id(), observer.id());

        Task updatedTask = updatedTaskMono.block();
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.observerIds()).contains(observer.id());

        assertThat(updatedTask.observers()).isNotNull().contains(observer);
    }

    @Test
    public void testAddExistingObserverThrowsException() {
        User observer = prepareUser();
        userRepository.save(observer).block();
        User author = prepareDifferentUser();
        userRepository.save(author).block();
        Task initialTask =  new Task(
            ObjectId.get().toHexString(),
            "TestTask2",
            "This is a test task2.",
            Instant.now(),
            Instant.now(),
            TaskStatus.NEW,
            author.id(),
            null,
            Stream.of(observer.id()).collect(Collectors.toSet()),

            null, // author
            null, // assignee
            new HashSet<>() // observers
        );

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> result = sut.addObserver(savedTask.id(), observer.id());

        assertThat(result).isNotNull();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> result.block());
    }

    @Test
    public void testAddObserverToNonExistentTask() {
        User observer = prepareUser();
        userRepository.save(observer).block();

        Mono<Task> result = sut.addObserver(ObjectId.get().toHexString(), observer.id());

        assertThat(result).isNotNull();
        assertThatThrownBy(result::block).isInstanceOf(TaskNotFoundException.class).hasMessageContaining("Task or observer not found");

    }

    @Test
    public void testAddObserverWithNonExistentObserver() {
        Task initialTask = prepareTask();

        Task savedTask = rut.save(initialTask).block();

        Mono<Task> result = sut.addObserver(savedTask.id(), ObjectId.get().toHexString());

        assertThat(result).isNotNull();
        assertThatThrownBy(result::block).isInstanceOf(TaskNotFoundException.class).hasMessageContaining("Task or observer not found");
    }

}
