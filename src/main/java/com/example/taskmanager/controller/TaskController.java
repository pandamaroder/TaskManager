package com.example.taskmanager.controller;

import com.example.taskmanager.exeption.UserNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    @GetMapping
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Task>> getTaskById(@PathVariable String id) {
        return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<Task> createTask(@RequestBody Task task, @RequestParam String authorId) {

        return taskService.createTask(task, authorId);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Task>> updateTask(@PathVariable String id, @RequestBody Task task) {
        return taskService.updateTask(id, task)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/add-observer/{observerId}")
    public Mono<ResponseEntity<Task>> addObserver(@PathVariable String id, @PathVariable String observerId) {
        return taskService.addObserver(id, observerId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id) {
        return taskService.deleteTask(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
