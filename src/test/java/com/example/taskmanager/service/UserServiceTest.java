package com.example.taskmanager.service;

import com.example.taskmanager.BaseTestConfig;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
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

    @Test
    public void testCreateUser() {
        final long countBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore).isZero();
        User user = new User(new ObjectId(), "TestUser1", "test@example.com");

        Mono<User> createdUserMono = sut.createUser(user);

        StepVerifier.create(createdUserMono).assertNext(createdUser -> {
            assertThat(createdUser.username()).isEqualTo("TestUser1");
            assertThat(createdUser.email()).isEqualTo("test@example.com");
            assertThat(createdUser.id()).isNotNull();
        }).verifyComplete();
        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testFindUserById() {
        User user = new User(new ObjectId(), "TestUser2", "testuser@example.com");

        User savedUser = rut.save(user).block();
        final long countBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore).isEqualTo(1);
        Mono<User> foundUserMono = sut.findUserById(savedUser.id());

        StepVerifier.create(foundUserMono).assertNext(foundUser -> {
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.username()).isEqualTo("TestUser2");
            assertThat(foundUser.email()).isEqualTo("testuser@example.com");
        }).verifyComplete();
    }

    @Test
    public void testUpdateUser() {
        User user = prepareUser();

        User savedUser = rut.save(user).block();

        final long countBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore).isEqualTo(1);
        User updatedUser = new User(new ObjectId(), "UpdatedUser", "updated@test.ru");

        Mono<User> userMono = sut.updateUser(savedUser.id(), updatedUser);
        User block = userMono.block();
        assertThat(block.id())
            .isNotNull()
            .isEqualTo(user.id());

        assertThat(block.username())
            .isEqualTo(updatedUser.username());

        final long countAfter = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore - countAfter).isZero();
    }

    @Test
    public void testDeleteUserById() {
        User user = new User(new ObjectId(), "UserToDelete", "delete@example.com");
        User savedUser = rut.save(user).block();

        final long countBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore).isEqualTo(1);

        Mono<Void> deleteUserMono = sut.deleteUserById(savedUser.id());

        StepVerifier.create(deleteUserMono).verifyComplete();

        Mono<User> deletedUserMono = rut.findById(savedUser.id());

        StepVerifier.create(deletedUserMono).expectNextCount(0).verifyComplete();

        final long countAfter = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore - countAfter).isPositive().isEqualTo(1);
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new User(new ObjectId(), "Test 1", "test1@example.com");
        User user2 = new User(new ObjectId(), "Test 2", "test2@example.com");

        rut.save(user1).block();
        rut.save(user2).block();

        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isNotZero().isPositive();
        assertThat(count).isEqualTo(2);

        Flux<User> allUsers = sut.findAllUsers();

        StepVerifier.create(allUsers)
            .expectNextMatches(userDTO -> userDTO.username().equals("Test 1"))
            .expectNextMatches(userDTO -> userDTO.username().equals("Test 2"))
            .verifyComplete();
    }
}
