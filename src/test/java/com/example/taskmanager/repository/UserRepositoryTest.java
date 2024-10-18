package com.example.taskmanager.repository;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends BaseTestConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void testGetAllUsers() {

        User user1 = prepareUser().username("Test 1").build();
        User user2 = prepareUser().username("Test 2").build();

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
            .expectNextMatches(user -> user.getUsername().equals("Test 1"))
            .expectNextMatches(user -> user.getUsername().equals("Test 2"))
            .verifyComplete();
    }

    @Test
    public void testFindUserById() {

        User user = prepareUser().username("User to find").build();
        userRepository.save(user).block();


        Mono<User> foundUser = userRepository.findById(user.getId());

        StepVerifier.create(foundUser)
            .assertNext(userFound -> {
                assertThat(userFound).isNotNull();
                assertThat(userFound.getUsername()).isEqualTo("User to find");
                assertThat(userFound.getId()).isEqualTo(user.getId());
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

        User user = prepareUser().username("User to delete").build();
        userRepository.save(user).block();

        long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isNotZero()
            .isEqualTo(1);

        Mono<Void> deletedUser = userRepository.deleteById(user.getId());

        StepVerifier.create(deletedUser)
            .verifyComplete();


        long count = getEntriesCount(mongoTemplate, USERS);
        assertThat(count)
            .isZero();
    }

    @Test
    public void testUpdateUser() {

        User user = prepareUser().username("User to update").build();
        userRepository.save(user).block();


        user.setUsername("Updated User Name");
        Mono<User> updatedUser = userRepository.save(user);


        StepVerifier.create(updatedUser)
            .assertNext(userUpdated -> {
                assertThat(userUpdated.getUsername()).isEqualTo("Updated User Name");
            })
            .verifyComplete();


        Mono<User> userFromDB = userRepository.findById(user.getId());
        StepVerifier.create(userFromDB)
            .assertNext(userFromDb -> {
                assertThat(userFromDb.getUsername()).isEqualTo("Updated User Name");
            })
            .verifyComplete();
    }
}
