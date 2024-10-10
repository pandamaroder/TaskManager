package com.example.taskmanager;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
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
import static com.example.taskmanager.MongoUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ContextConfiguration(initializers = MongoInitializer.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        mongoTemplate.dropCollection("users");
    }

    @Test
    public void testGetAllUsers() {

        User user1 = prepareUser().username("User 1").build();
        User user2 = prepareUser().username("User 2").build();

        userRepository.save(user1).block();
        userRepository.save(user2).block();

        // Валидация количества записей в коллекции
        long count = getEntriesCount(mongoTemplate, "users");
        assertThat(count)
            .isNotZero()
            .isPositive();
        assertThat(count)
            .isEqualTo(2);

        // Проверка данных в MongoDB реактивных потоков
        Flux<User> users = userRepository.findAll();

        StepVerifier.create(users)
            .expectNextMatches(user -> user.getUsername().equals("User 1"))
            .expectNextMatches(user -> user.getUsername().equals("User 2"))
            .verifyComplete();
    }

    @Test
    public void testFindUserById() {
        // Сначала создаем и сохраняем пользователя
        User user = prepareUser().username("User to find").build();
        userRepository.save(user).block();

        // Ищем пользователя по ID
        Mono<User> foundUser = userRepository.findById(user.getId());

        // Проверяем, что пользователь был найден
        StepVerifier.create(foundUser)
            .assertNext(userFound -> {
                assertThat(userFound).isNotNull();
                assertThat(userFound.getUsername()).isEqualTo("User to find");
                assertThat(userFound.getId()).isEqualTo(user.getId());
            })
            .verifyComplete();

        // Проверяем количество записей в MongoDB для валидации
        long count = getEntriesCount(mongoTemplate, "users");
        assertThat(count)
            .isNotZero()
            .isPositive()
            .isEqualTo(1);
    }

    @Test
    public void testDeleteUser() {
        // Сначала создаем пользователя
        User user = prepareUser().username("User to delete").build();
        userRepository.save(user).block();

        long countBefore = getEntriesCount(mongoTemplate, "users");
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);

        // Удаляем пользователя
        Mono<Void> deletedUser = userRepository.deleteById(user.getId());

        StepVerifier.create(deletedUser)
            .verifyComplete();

        // Проверяем, что в коллекции больше нет записей
        long count = getEntriesCount(mongoTemplate, "users");
        assertThat(count)
            .isZero();
    }

    @Test
    public void testUpdateUser() {
        // Создаем и сохраняем пользователя
        User user = prepareUser().username("User to update").build();
        userRepository.save(user).block();

        // Обновляем данные пользователя
        user.setUsername("Updated User Name");
        Mono<User> updatedUser = userRepository.save(user);

        // Проверяем обновление
        StepVerifier.create(updatedUser)
            .assertNext(userUpdated -> {
                assertThat(userUpdated.getUsername()).isEqualTo("Updated User Name");
            })
            .verifyComplete();

        // Проверяем, что данные обновились в MongoDB
        Mono<User> userFromDB = userRepository.findById(user.getId());
        StepVerifier.create(userFromDB)
            .assertNext(userFromDb -> {
                assertThat(userFromDb.getUsername()).isEqualTo("Updated User Name");
            })
            .verifyComplete();
    }
}
