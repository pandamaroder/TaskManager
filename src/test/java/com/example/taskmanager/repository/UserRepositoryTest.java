package com.example.taskmanager.repository;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends BaseTestConfig {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGetAllUsers() {

        User user1 = new User(new ObjectId(), "Test 1", "testUser1@test.ru");
        User user2 = new User(new ObjectId(), "Test 2", "testUser2@test.ru");

        userRepository.save(user1).block();
        userRepository.save(user2).block();

        long count = getEntriesCount(mongoTemplate, USERS);
        assertThat(count)
            .isNotZero()
            .isPositive();
        assertThat(count)
            .isEqualTo(2);


        Flux<User> users = userRepository.findAll();

        StepVerifier.create(users)
            .expectNextMatches(user -> user.username().equals("Test 1"))
            .expectNextMatches(user -> user.username().equals("Test 2"))
            .verifyComplete();
    }

    @Test
    public void testFindUserById() {

        User user = new User(new ObjectId(), "Test Test", "testTest@test.ru");
        userRepository.save(user).block();


        Mono<User> foundUser = userRepository.findById(user.id());

        StepVerifier.create(foundUser)
            .assertNext(userFound -> {
                assertThat(userFound).isNotNull();
                assertThat(userFound.username()).isEqualTo("Test Test");
                assertThat(userFound.id()).isEqualTo(user.id());
            })
            .verifyComplete();


        long count = getEntriesCount(mongoTemplate, USERS);
        assertThat(count)
            .isNotZero()
            .isPositive()
            .isEqualTo(1);
    }

    @Test
    public void testDeleteUser() {

        User user = new User(new ObjectId(), "d", "d@test.ru");
        userRepository.save(user).block();

        long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);

        Mono<Void> deletedUser = userRepository.deleteById(user.id());

        StepVerifier.create(deletedUser)
            .verifyComplete();


        long count = getEntriesCount(mongoTemplate, USERS);
        assertThat(count)
            .isZero();
    }

    @Test
    public void testUpdateUser() {

        User user = new User(new ObjectId(), "Updated User Name", "b@test.ru");
        userRepository.save(user).block();


        Mono<User> updatedUser = userRepository.save(user);


        StepVerifier.create(updatedUser)
            .assertNext(userUpdated -> {
                assertThat(userUpdated.username()).isEqualTo("Updated User Name");
            })
            .verifyComplete();


        Mono<User> userFromDB = userRepository.findById(user.id());
        StepVerifier.create(userFromDB)
            .assertNext(userFromDb -> {
                assertThat(userFromDb.username()).isEqualTo("Updated User Name");
            })
            .verifyComplete();
    }
}
