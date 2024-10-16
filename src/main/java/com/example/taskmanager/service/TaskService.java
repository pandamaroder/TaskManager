package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.exeption.TaskNotFoundException;
import com.example.taskmanager.exeption.UserNotFoundException;
import com.example.taskmanager.mappers.TaskMapper;
import com.example.taskmanager.mappers.UserMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
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
    private final UserRepository userRepository;
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

    public Mono<TaskDTO> createTask(TaskDTO taskDTO, String authorId) {
        return userRepository.findById(authorId)
            .map(author -> buildTaskFromDTO(taskDTO, authorId))
            // Сохранение задачи
            .flatMap(taskRepository::save)
            // Преобразование сохраненной задачи в DTO
            .map(savedTask -> mapToDTO(savedTask, authorId))
            //Этот метод используется для обработки пустых значений в Mono или Flux.
            //Mono<User> userMono = userRepository.findById(userId)
            //.switchIfEmpty(Mono.error(new UserNotFoundException("User not found")));
            .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")));
    }


    public Mono<TaskDTO> updateTask(String taskId, TaskDTO taskDTO) {
        return taskRepository.findById(taskId)
            .flatMap(existingTask -> {
                // Создаем новый экземпляр Task с обновленными значениями
                Task updatedTask = Task.builder()
                    .id(existingTask.getId())
                    .name(taskDTO.getName())
                    .description(taskDTO.getDescription())
                    .createdAt(existingTask.getCreatedAt()) // Сохраняем оригинальную дату создания
                    .updatedAt(Instant.now()) // Устанавливаем новое время обновления
                    .status(existingTask.getStatus()) // Сохраняем оригинальный статус
                    .authorId(existingTask.getAuthorId()) // Сохраняем оригинальный авторский ID
                    .assigneeId(existingTask.getAssigneeId()) // Сохраняем оригинальный ID назначенного
                    .observerIds(existingTask.getObserverIds()) // Сохраняем оригинальные IDs наблюдателей
                    .build();
                return taskRepository.save(updatedTask);
            })
            .map(savedTask -> mapToDTO(savedTask, savedTask.getAuthorId())) // Используем authorId из сохраненной задачи
            .switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found"))); // Обработка случая, если задача не найдена
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

    private Task buildTaskFromDTO(TaskDTO taskDTO, String authorId) {
        Instant now = Instant.now();
        return Task.builder()
            .name(taskDTO.getName())
            .description(taskDTO.getDescription())
            .authorId(authorId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private TaskDTO mapToDTO(Task task, String authorId) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setName(task.getName());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setCreatedAt(task.getCreatedAt());
        taskDTO.setUpdatedAt(task.getUpdatedAt());
        taskDTO.setAuthorId(authorId); // или task.getAuthorId(), если нужно
        taskDTO.setStatus(task.getStatus());
        // Здесь можно добавить и другие необходимые поля

        return taskDTO;
    }

}
