package com.example.taskmanager.repository;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareTask;
import static org.assertj.core.api.Assertions.assertThat;


public class TaskRepositoryTest extends BaseTestConfig {

    @Autowired
    private TaskRepository taskRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testGetAllTasks() {
        final long count1 = getEntriesCount(mongoTemplate, TASKS);
        assertThat(count1)
            .isZero();
        final Task task1 = prepareTask().name("Test 1").build();
        final Task task2 = prepareTask().name("Test 2").build();

        taskRepository.save(task1).block();
        taskRepository.save(task2).block();
        Flux<Task> tasks = taskRepository.findAll();

        StepVerifier.create(tasks)
            .expectNextMatches(task -> task.getName().equals("Test 1"))
            .expectNextMatches(task -> task.getName().equals("Test 2"))
            .verifyComplete();
        // Валидация количества записей в коллекции
        final long count = getEntriesCount(mongoTemplate, TASKS);
        assertThat(count)
            .isNotZero()
            .isPositive();
        assertThat(count)
            .isEqualTo(2);

    }

    @Test
    public void testDeleteTask() {

        final Task task = prepareTask().name("to delete").build();
        taskRepository.save(task).block();
        final long countBefore = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);
        Mono<Void> deletedTask = taskRepository.deleteById(task.getId());

        StepVerifier.create(deletedTask)
            .verifyComplete();

        final long count = getEntriesCount(mongoTemplate, TASKS);
        assertThat(count)
            .isZero();
    }

    @Test
    public void testFindById() {
        // Сначала создаем и сохраняем задачу
        final Task task = prepareTask().name("to find").build();
        taskRepository.save(task).block(); //
        final long countBefore = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);

        Mono<Task> foundTask = taskRepository.findById(task.getId());

        StepVerifier.create(foundTask)
            .assertNext(taskFound -> {
                assertThat(taskFound.getId()).isEqualTo(task.getId());
            });
        final long countAfter = getEntriesCount(mongoTemplate, TASKS);
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(countAfter);
    }
}
