package com.example.taskmanager;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.MongoUtils.getEntriesCount;
import static com.example.taskmanager.MongoUtils.prepareTask;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ContextConfiguration(initializers = MongoInitializer.class) // Добавляем инициализатор

public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        mongoTemplate.dropCollection("tasks");
    }

    @Test
    public void testGetAllTasks() {

        Task task1 = prepareTask().name("Task 1").build();
        Task task2 = prepareTask().name("Task 2").build();

        taskRepository.save(task1).block();
        taskRepository.save(task2).block();

        // Валидация количества записей в коллекции
        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count)
            .isNotZero()
            .isPositive();
        assertThat(count)
            .isEqualTo(2);

        // Проверка данных в MongoDB реактивных потоков
        Flux<Task> tasks = taskRepository.findAll();

        StepVerifier.create(tasks)
            .expectNextMatches(task -> task.getName().equals("Task 1"))
            .expectNextMatches(task -> task.getName().equals("Task 2"))
            .verifyComplete();
    }

    @Test
    public void testDeleteTask() {
        // Сначала создаем задачу
        Task task = prepareTask().name("Task to delete").build();
        taskRepository.save(task).block();
        long countBefore = getEntriesCount(mongoTemplate, "tasks");
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);
        // Удаляем задачу
        Mono<Void> deletedTask = taskRepository.deleteById(task.getId());

        StepVerifier.create(deletedTask)
            .verifyComplete();

        // Проверяем, что в коллекции больше нет записей
        long count = getEntriesCount(mongoTemplate, "tasks");
        assertThat(count)
            .isZero();
    }

    @Test
    public void testFindById() {
        // Сначала создаем и сохраняем задачу
        Task task = prepareTask().name("Task to find").build();
        taskRepository.save(task).block(); //
        long countBefore = getEntriesCount(mongoTemplate, "tasks");
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);
        // Пытаемся найти задачу по ее ID
        Mono<Task> foundTask = taskRepository.findById(task.getId());

        // Проверяем, что задача найдена и ее свойства совпадают
        StepVerifier.create(foundTask)
            .assertNext(taskFound -> {
                assertThat(taskFound.getId()).isEqualTo(task.getId());
            });
        long countAfter = getEntriesCount(mongoTemplate, "tasks");
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(countAfter);
    }
}
