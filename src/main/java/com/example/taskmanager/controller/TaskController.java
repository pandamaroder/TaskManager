package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;



    @GetMapping
    public Flux<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskDTO>> getTaskById(@PathVariable String id) {
        return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    //Когда нужно передать параметры, связанные с фильтрацией, сортировкой,
    // пагинацией или другими опциональными данными, которые не идентифицируют ресурс.
    public Mono<TaskDTO> createTask(@RequestBody TaskDTO taskDTO, @RequestParam String authorId) {
        return taskService.createTask(taskDTO, authorId);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TaskDTO>> updateTask(@PathVariable String id, @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(id, taskDTO)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/add-observer/{observerId}")
    public Mono<ResponseEntity<TaskDTO>> addObserver(@PathVariable String id, @PathVariable String observerId) {
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
