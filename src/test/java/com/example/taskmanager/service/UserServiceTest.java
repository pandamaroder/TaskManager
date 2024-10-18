package com.example.taskmanager.service;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.example.taskmanager.DataModelUtils.getEntriesCount;
import static com.example.taskmanager.DataModelUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserServiceTest extends BaseTestConfig {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository rut;

    @Autowired
    private UserService sut;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void testCreateUser() {
        final long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isZero();
        User user = prepareUser()
            .username("TestUser1")
            .email("test@example.com")
            .build();

        Mono<User> createdUserMono = sut.createUser(user);

        StepVerifier.create(createdUserMono)
            .assertNext(createdUser -> {
                assertThat(createdUser.getUsername()).isEqualTo("TestUser1");
                assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
                assertThat(createdUser.getId()).isNotNull();
            })
            .verifyComplete();
        long count = getEntriesCount(mongoTemplate, USERS);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testFindUserById() {
        User user = prepareUser()
            .username("TestUser2")
            .email("testuser@example.com")
            .build();

        User savedUser = rut.save(user).block();
        final long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isEqualTo(1);
        Mono<User> foundUserMono = sut.findUserById(savedUser.getId());

        StepVerifier.create(foundUserMono)
            .assertNext(foundUser -> {
                assertThat(foundUser).isNotNull();
                assertThat(foundUser.getUsername()).isEqualTo("TestUser2");
                assertThat(foundUser.getEmail()).isEqualTo("testuser@example.com");
            })
            .verifyComplete();
    }

    @Test
    public void testUpdateUser() {
        User user = prepareUser()
            .username("OriginalUser")
            .email("original@example.com")
            .build();

        User savedUser = rut.save(user).block();

        final long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isEqualTo(1);
        User updatedUser = prepareUser()
            .username("UpdatedUser")
            .email("updated@example.com")
            .build();

        Mono<User> updatedUserMono = sut.updateUser(savedUser.getId(), updatedUser);

        StepVerifier.create(updatedUserMono)
            .assertNext(userFromDB -> {
                assertThat(userFromDB.getUsername()).isEqualTo("UpdatedUser");
                assertThat(userFromDB.getEmail()).isEqualTo("updated@example.com");
            })
            .verifyComplete();

        final long countAfter = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore - countAfter)
            .isZero();
    }

    @Test
    public void testDeleteUserById() {

        User user = prepareUser()
            .username("UserToDelete")
            .email("delete@example.com")
            .build();
        User savedUser = rut.save(user).block();

        final long countBefore = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore)
            .isEqualTo(1);

        Mono<Void> deleteUserMono = sut.deleteUserById(savedUser.getId());

        StepVerifier.create(deleteUserMono)
            .verifyComplete();
        Mono<User> deletedUserMono = rut.findById(savedUser.getId());

        StepVerifier.create(deletedUserMono)
            .expectNextCount(0)
            .verifyComplete();


        final long countAfter = getEntriesCount(mongoTemplate, USERS);
        assertThat(countBefore - countAfter)
            .isPositive()
            .isEqualTo(1);
    }
}
