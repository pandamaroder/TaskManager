package com.example.taskmanager.service;

import com.example.taskmanager.exeption.TaskNotFoundException;
import com.example.taskmanager.exeption.UserNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Mono<Task> getTaskById(ObjectId id) {
        if (id == null) {
            return Mono.empty();
        }
        return taskRepository.findById(id).flatMap(this::mapTaskWithRelations);
    }

    public Mono<Task> createTask(Task task, ObjectId authorId) {

        return userRepository.findById(authorId).map(author -> {
            Task task1 = new Task(task.id(),
                task.name(),
                task.description(),
                Instant.now(),
                Instant.now(),
                task.status(),
                authorId,
                null,
                new HashSet<>(), author, // author
                null, // assignee
                new HashSet<>());
            return task1;
        }).flatMap(taskRepository::save).switchIfEmpty(Mono.error(new UserNotFoundException("User not found, you can't create task")));
    }

    public Mono<Task> updateTask(ObjectId taskId, Task task) {
        return taskRepository.findById(taskId).flatMap(existingTask -> {
            Mono<User> authorMono = userService.findUserById(task.authorId())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Author not found")));
            Mono<User> assigneeMono = userService.findUserById(task.assigneeId())
                .switchIfEmpty(Mono.defer(() -> {
                    User newAssignee = new User(new ObjectId(), "Default Assignee", "default.assignee@test.com");
                    return userRepository.save(newAssignee);
                }));

            return Mono.zip(authorMono, assigneeMono).flatMap(tuple -> {
                User author = tuple.getT1();
                User assignee = tuple.getT2();

                Task updatedTask = new Task(existingTask.id(),
                    task.name(),
                    task.description(),
                    existingTask.createdAt(),
                    Instant.now(),
                    task.status(),
                    task.authorId(),
                    task.assigneeId(),
                    existingTask.observerIds(),
                    author,
                    assignee,
                    existingTask.observers());

                return taskRepository.save(updatedTask);
            });
        }).switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found")));
    }

    public Mono<Void> deleteTask(ObjectId id) {
        if (id == null) {
            return Mono.empty();
        }
        return taskRepository.deleteById(id);
    }

    public Mono<Task> addObserver(ObjectId taskId, ObjectId observerId) {
        return taskRepository.findById(taskId)
            .flatMap(task -> {
                if (task == null) {
                    return Mono.error(new TaskNotFoundException("Task not found"));
                }

                return userRepository.findById(observerId)
                    .flatMap(observer -> {
                        if (observer == null) {
                            return Mono.error(new TaskNotFoundException("Observer not found"));
                        }

                        if (task.observerIds().contains(observerId)) {
                            return Mono.error(new IllegalArgumentException("Observer already added"));
                        }

                        var updatedTask = new Task(task.id(),
                            task.name(),
                            task.description(),
                            task.createdAt(),
                            Instant.now(), // Обновляем время обновления
                            task.status(),
                            task.authorId(),
                            task.assigneeId(),
                            Stream.concat(task.observerIds().stream(),
                                Stream.of(observerId)).collect(Collectors.toSet()), // Добавляем нового наблюдателя
                            task.author(),
                            task.assignee(),
                            task.observers());

                        return taskRepository.save(updatedTask);
                    });
            })
            .flatMap(this::mapTaskWithRelations) // Применяем метод для сопоставления задачи с отношениями
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Task or observer not found"))); // Обработка случая, если задача или наблюдатель не найдены
    }

    private Mono<Task> mapTaskWithRelations(Task task) {
        final Mono<User> authorMono = userService.findUserById(task.authorId())
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Author not found")));

        final Mono<User> assigneeMono = userService.findUserById(task.assigneeId())
            .switchIfEmpty(Mono.defer(() -> {
                User newAssignee = new User(new ObjectId(), "Default Assignee", "default.assignee@test.com");
                return userRepository.save(newAssignee);
            }));

        Flux<User> observersFlux = Flux.fromIterable(task.observerIds())
            .flatMap(userService::findUserById)
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Observer not found")));

        return Mono.zip(authorMono, assigneeMono, observersFlux.collectList()).map(tuple -> {
            User author = tuple.getT1();
            User assignee = tuple.getT2();
            List<User> observers = tuple.getT3();

            return new Task(task.id(),
                task.name(),
                task.description(),
                task.createdAt(),
                Instant.now(),
                task.status(),
                task.authorId(),
                task.assigneeId(),
                observers.stream().map(User::id).collect(Collectors.toSet()),
                author,
                assignee,
                new HashSet<>(observers));
        });
    }
}
