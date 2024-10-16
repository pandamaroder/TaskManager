package com.example.taskmanager.service;

import com.example.taskmanager.BaseTest;
import com.example.taskmanager.MongoInitializer;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.MongoUtils.getEntriesCount;
import static com.example.taskmanager.MongoUtils.prepareTask;
import static com.example.taskmanager.MongoUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;


public class TaskServiceTest extends BaseTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskService taskService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        // Очищаем коллекцию перед каждым тестом
        mongoTemplate.dropCollection("tasks");
    }


    @Test
    @Ignore
    public void testUpdateTask() {
        // Создаем и сохраняем пользователя
        User user = prepareUser().build();
        userRepository.save(user).block();

        // Создаем и сохраняем задачу с привязкой к автору
        Task task = prepareTask()
            .name("Task to update")
            .description("Initial description")
            .authorId(user.getId())  // Указываем автора
            .build();
        taskRepository.save(task).block();

        // Создаем DTO с новыми значениями для обновления
        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setName("Updated Task Name");
        updatedTaskDTO.setDescription("Updated description");

        // Обновляем задачу через сервис
        Mono<TaskDTO> updatedTask = taskService.updateTask(task.getId(), updatedTaskDTO);

        // Проверяем обновление
        StepVerifier.create(updatedTask)
            .assertNext(updatedTaskFound -> {
                assertThat(updatedTaskFound).isNotNull();
                assertThat(updatedTaskFound.getName()).isEqualTo("Updated Task Name");
                assertThat(updatedTaskFound.getDescription()).isEqualTo("Updated description");
                assertThat(updatedTaskFound.getId()).isEqualTo(task.getId());
            })
            .verifyComplete();

        // Проверяем, что обновленная задача сохранена в базе данных
        Mono<Task> taskFromDB = taskRepository.findById(task.getId());
        StepVerifier.create(taskFromDB)
            .assertNext(updatedTaskFromDB -> {
                assertThat(updatedTaskFromDB.getName()).isEqualTo("Updated Task Name");
                assertThat(updatedTaskFromDB.getDescription()).isEqualTo("Updated description");
            })
            .verifyComplete();
    }

}
