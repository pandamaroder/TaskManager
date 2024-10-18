package com.example.taskmanager.service;

import com.example.taskmanager.BaseConfig;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareTask;
import static com.example.taskmanager.DataModelUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;


public class UserServiceTest extends BaseConfig {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testUpdateTask() {
        User user = prepareUser().build();
        userRepository.save(user).block();

        Task taskInitial = prepareTask()
            .name("Task to update")
            .description("Initial description")
            .authorId(user.getId())  // Указываем автора
            .build();
        taskRepository.save(taskInitial).block();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setName("Updated Task Name");
        updatedTaskDTO.setDescription("Updated description");

        Mono<TaskDTO> updatedTask = taskService.updateTask(taskInitial.getId(), updatedTaskDTO);

        StepVerifier.create(updatedTask)
            .assertNext(updatedTaskFound -> {
                assertThat(updatedTaskFound).isNotNull();
                assertThat(updatedTaskFound.getName()).isEqualTo("Updated Task Name");
                assertThat(updatedTaskFound.getDescription()).isEqualTo("Updated description");
                assertThat(updatedTaskFound.getId()).isEqualTo(taskInitial.getId());
            })
            .verifyComplete();

        // Проверяем, что обновленная задача сохранена в базе данных
        Mono<Task> taskFromDB = taskRepository.findById(taskInitial.getId());
        StepVerifier.create(taskFromDB)
            .assertNext(updatedTaskFromDB -> {
                assertThat(updatedTaskFromDB.getName()).isEqualTo("Updated Task Name");
                assertThat(updatedTaskFromDB.getDescription()).isEqualTo("Updated description");
            })
            .verifyComplete();
    }

    @Test
    public void testCreateTask() {
        User user = prepareUser()
            .username("Test Author")
            .build();
        userRepository.save(user).block();

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("Test Task");
        taskDTO.setDescription("This is a test task.");

        final String userId = user.getId();
        Mono<TaskDTO> createdTaskMono = taskService.createTask(taskDTO, userId);

        StepVerifier.create(createdTaskMono)
            .assertNext(createdTask -> {
                assertThat(createdTask.getName()).isEqualTo("Test Task");
                assertThat(createdTask.getDescription()).isEqualTo("This is a test task.");
                assertThat(createdTask.getAuthorId()).isEqualTo(userId);
            })
            .verifyComplete();

        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count).isEqualTo(1);
    }

}
