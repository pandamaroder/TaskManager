package com.example.taskmanager.repository;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareDifferentTask;
import static com.example.taskmanager.DataModelUtils.prepareTask;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskRepositoryTest extends BaseTestConfig {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testGetAllTasks() {
        final long count1 = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(count1).isZero();

        final Task task1 = prepareTask();
        final Task task2 = prepareDifferentTask();

        taskRepository.save(task1).block();
        taskRepository.save(task2).block();

        final long count = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(count).isEqualTo(2);

        Flux<Task> allTasks = taskRepository.findAll();
        List<Task> block = allTasks.collectList().block();
        assertThat(block).hasSize(2);
    }

    @Test
    public void testDeleteTask() {
        final Task task = prepareTask();
        taskRepository.save(task).block();

        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isNotZero().isEqualTo(1);

        Mono<Void> deletedTask = taskRepository.deleteById(task.id());

        StepVerifier.create(deletedTask).verifyComplete();

        final long count = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(count).isZero();
    }

    @Test
    public void testFindById() {
        final Task task = prepareTask();
        taskRepository.save(task).block();

        final long countBefore = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isNotZero().isEqualTo(1);

        Mono<Task> foundTask = taskRepository.findById(task.id());

        StepVerifier.create(foundTask)
            .assertNext(taskFound -> assertThat(taskFound.id()).isEqualTo(task.id()))
            .verifyComplete();

        final long countAfter = getEntriesCount(mongoTemplate, TASKS_COLLECTION);
        assertThat(countBefore).isNotZero().isEqualTo(countAfter);
    }
}
