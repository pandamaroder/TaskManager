package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.mappers.TaskMapper;
import com.example.taskmanager.mappers.UserMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public Flux<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
            .flatMap(this::mapTaskWithRelations);
    }

    public Mono<TaskDTO> getTaskById(String id) {
        return taskRepository.findById(id)
            .flatMap(this::mapTaskWithRelations);
    }

    public Mono<TaskDTO> createTask(TaskDTO taskDTO) {
        // Преобразуем DTO в сущность
        Task task = taskMapper.toEntity(taskDTO);

        // Задаем значения для final полей
        Task newTask = new Task(
            UUID.randomUUID().toString(),
            task.getName(),
            task.getDescription(),
            Instant.now(),
            Instant.now(),
            task.getStatus(),
            task.getAuthorId(),
            task.getAssigneeId(),
            task.getObserverIds()
        );

        // Сохраняем задачу в репозитории и возвращаем DTO
        return taskRepository.save(newTask)
            .flatMap(this::mapTaskWithRelations);  // метод для обработки связей
    }


    public Mono<TaskDTO> updateTask(String id, TaskDTO taskDTO) {
        // Логика обновления задачи
        return null;
    }

    public Mono<TaskDTO> addObserver(String id, String observerId) {
        return taskRepository.findById(id)
            .flatMap(task -> {
                // Логика добавления наблюдателя
                return null;
            });
    }

    public Mono<Void> deleteTask(String id) {
        return taskRepository.deleteById(id);
    }

    private Mono<TaskDTO> mapTaskWithRelations(Task task) {
        // Получаем Mono для автора и исполнителя задачи
        Mono<User> authorMono = userService.findUserById(task.getAuthorId());
        Mono<User> assigneeMono = userService.findUserById(task.getAssigneeId());


        // Получаем Flux для наблюдателей
        Flux<User> observersFlux = Flux.fromIterable(task.getObserverIds())
            .flatMap(userService::findUserById);
        //Tuple2
        // Объединяем все результаты
        return Mono.zip(authorMono, assigneeMono, observersFlux.collectList())
            .map(tuple -> {
                User author = tuple.getT1();
                User assignee = tuple.getT2();
                List<User> observers = tuple.getT3();

                TaskDTO taskDTO = taskMapper.toDTO(task);
                //taskDTO.setAuthor(author);
                //taskDTO.setAssignee(assignee);
                taskDTO.setObservers(new HashSet<>(observers.stream()
                    .map(userMapper::toDTO) // Преобразуем пользователей в UserDTO
                    .collect(Collectors.toSet())));

                return taskDTO;
            });
    }

}
