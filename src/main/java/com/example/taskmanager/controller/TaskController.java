package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
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

    @GetMapping
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Task>> getTaskById(@PathVariable String id) {
        return taskService.getTaskById(new ObjectId(id))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    //@RequestParam Когда нужно передать параметры, связанные с фильтрацией, сортировкой,
    // пагинацией или другими опциональными данными, которые не идентифицируют ресурс.
    public Mono<Task> createTask(@RequestBody Task Task, @RequestParam String authorId) {
        return taskService.createTask(Task, new ObjectId(authorId));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Task>> updateTask(@PathVariable String id, @RequestBody Task task) {
        return taskService.updateTask(new ObjectId(id), task)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/add-observer/{observerId}")
    public Mono<ResponseEntity<Task>> addObserver(@PathVariable String id, @PathVariable String observerId) {
        return taskService.addObserver(new ObjectId(id), new ObjectId(observerId))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id) {
        return taskService.deleteTask(new ObjectId(id))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
