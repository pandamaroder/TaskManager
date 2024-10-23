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
import java.util.Set;
import java.util.stream.Collectors;

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
        return taskRepository.findById(id).flatMap(this::mapTaskWithRelations);
    }

    public Mono<Task> createTask(Task task, ObjectId authorId) {
        return userRepository.findById(authorId).map(author -> {

            Task task1 = Task.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .authorId(authorId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status(task.getStatus())
                .build();
            return task1;
        }).flatMap(taskRepository::save)
            .switchIfEmpty(Mono.error(new UserNotFoundException("User not found, you can't create task")));
    }

    public Mono<Task> updateTask(ObjectId taskId, Task task) {
        return taskRepository.findById(taskId).flatMap(existingTask -> {

                Task updatedTask = Task.builder().id(task.getId()).name(task.getName()).description(task.getDescription()).createdAt(existingTask.getCreatedAt()).updatedAt(Instant.now()).status(existingTask.getStatus()).authorId(existingTask.getAuthorId()).assigneeId(existingTask.getAssigneeId()).observerIds(existingTask.getObserverIds()).build();
                return taskRepository.save(updatedTask);
            })// Преобразуем обновленную задачу обратно в DTO
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found")));
    }

    public Mono<Void> deleteTask(ObjectId id) {
        return taskRepository.deleteById(id);
    }

    public Mono<Task> addObserver(ObjectId taskId, ObjectId observerId) {
        return taskRepository.findById(taskId).flatMap(task -> userRepository.findById(observerId).flatMap(observer -> {
            // Проверяем, есть ли уже наблюдатель
            if (task.getObserverIds().contains(observerId)) {
                return Mono.error(new IllegalArgumentException("Observer already added"));
            }

            Task updatedTask = Task.builder()
                .name(task.getName())
                .description(task.getDescription())
                .createdAt(task.getCreatedAt())
                .updatedAt(Instant.now())
                .status(task.getStatus())
                .authorId(task.getAuthorId())
                .assigneeId(task.getAssigneeId())
                .observerIds(new HashSet<>(task.getObserverIds()))
                .build();

            updatedTask.getObserverIds().add(observerId);

            return taskRepository.save(updatedTask);
        }))
            .flatMap(savedTask -> mapTaskWithRelations(savedTask))
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Task or observer not found")));
    }


    private Mono<Task> mapTaskWithRelations(Task task) {
        Mono<User> authorMono = userService.findUserById(task.getAuthorId());
        Mono<User> assigneeMono = userService.findUserById(task.getAssigneeId());
        Flux<User> observersFlux = Flux.fromIterable(task.getObserverIds()).flatMap(userService::findUserById);

        return Mono.zip(authorMono, assigneeMono, observersFlux.collectList()).map(tuple -> {
            User author = tuple.getT1();
            User assignee = tuple.getT2();
            List<User> observers = tuple.getT3();

            Set<ObjectId> observerIds = observers.stream().map(User::id)
                .collect(Collectors.toSet());

            // Создание нового экземпляра Task с обновленными полями
            return new Task(
                task.getId(),
                task.getName(),          // Оригинальное имя
                task.getDescription(),   // Оригинальное описание
                task.getCreatedAt(),     // Оригинальная дата создания
                Instant.now(),           // Обновляемая дата
                task.getStatus(),        // Оригинальный статус
                task.getAuthorId(),      // Оригинальный авторский ID
                task.getAssigneeId(),    // Оригинальный ID назначенного
                observerIds,             // Обновленные ID наблюдателей
                author,                  // Обновленный автор
                assignee,                // Обновленный назначенный
                new HashSet<>(observers) // Обновленные наблюдатели
            );
        });
    }
}
