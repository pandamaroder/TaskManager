package com.example.taskmanager.service;

import com.example.taskmanager.BaseTestConfig;
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


public class TaskServiceTest extends BaseTestConfig {

    @Autowired
    private TaskRepository rut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService sut;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testUpdate() {
        User user = prepareUser().build();
        userRepository.save(user).block();

        Task tInitial = prepareTask()
            .name("No update")
            .description("Initial description")
            .authorId(user.getId())  // Указываем автора
            .build();
        rut.save(tInitial).block();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setName("Updated Name");
        updatedTaskDTO.setDescription("Updated description");

        Mono<TaskDTO> updatedTask = sut.updateTask(tInitial.getId(), updatedTaskDTO);

        StepVerifier.create(updatedTask)
            .assertNext(updatedTaskFound -> {
                assertThat(updatedTaskFound).isNotNull();
                assertThat(updatedTaskFound.getName()).isEqualTo("Updated Name");
                assertThat(updatedTaskFound.getDescription()).isEqualTo("Updated description");
                assertThat(updatedTaskFound.getId()).isEqualTo(tInitial.getId());
            })
            .verifyComplete();

        // Проверяем, что обновленная задача сохранена в базе данных
        Mono<Task> taskFromDB = rut.findById(tInitial.getId());
        StepVerifier.create(taskFromDB)
            .assertNext(updatedTaskFromDB -> {
                assertThat(updatedTaskFromDB.getName()).isEqualTo("Updated Name");
                assertThat(updatedTaskFromDB.getDescription()).isEqualTo("Updated description");
            })
            .verifyComplete();
    }

    @Test
    public void testCreate() {
        final long countBefore = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore)
            .isZero();

        User user = prepareUser()
            .username("TestAuthor")
            .build();
        userRepository.save(user).block();

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("TestTask");
        taskDTO.setDescription("This is a test.");

        final String userId = user.getId();
        Mono<TaskDTO> createdTaskMono = sut.createTask(taskDTO, userId);

        StepVerifier.create(createdTaskMono)
            .assertNext(createdTask -> {
                assertThat(createdTask.getName()).isEqualTo("TestTask");
                assertThat(createdTask.getDescription()).isEqualTo("This is a test.");
                assertThat(createdTask.getAuthorId()).isEqualTo(userId);
            })
            .verifyComplete();

        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count).isEqualTo(1);
    }

}
