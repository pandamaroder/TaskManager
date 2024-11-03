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
import static com.example.taskmanager.DataModelUtils.prepareDifferentUser;
import static com.example.taskmanager.DataModelUtils.prepareUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends BaseTestConfig {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {

        User user = prepareUser();
        userRepository.save(user).block();


        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isNotZero().isPositive();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGetAllUsers() {

        User user1 = prepareUser();
        User user2 = prepareDifferentUser();

        userRepository.save(user1).block();
        userRepository.save(user2).block();

        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isNotZero().isPositive();
        assertThat(count).isEqualTo(2);

        Flux<User> users = userRepository.findAll();

        StepVerifier.create(users).expectNextMatches(user -> user.username().equals("defaultUserName")).expectNextMatches(user -> user.username().equals("defaultUserName2")).verifyComplete();
    }

    @Test
    public void testFindUserById() {

        User user = prepareUser();
        userRepository.save(user).block();

        Mono<User> foundUser = userRepository.findById(new ObjectId(user.id()));

        StepVerifier.create(foundUser).assertNext(userFound -> {
            assertThat(userFound).isNotNull();
            assertThat(userFound.username()).isEqualTo("defaultUserName");
            assertThat(userFound.id()).isEqualTo(user.id());
        }).verifyComplete();

        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isNotZero().isPositive().isEqualTo(1);
    }

    @Test
    public void testDeleteUser() {

        User user = prepareUser();
        userRepository.save(user).block();

        long countBefore = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(countBefore).isNotZero().isEqualTo(1);

        Mono<Void> deletedUser = userRepository.deleteById(new ObjectId(user.id()));

        StepVerifier.create(deletedUser).verifyComplete();

        long count = getEntriesCount(mongoTemplate, USERS_COLLECTION);
        assertThat(count).isZero();
    }

    @Test
    public void testUpdateUser() {

        User user = prepareDifferentUser();
        userRepository.save(user).block();

        Mono<User> updatedUser = userRepository.save(user);

        StepVerifier.create(updatedUser).assertNext(userUpdated -> {
            assertThat(userUpdated.username()).isEqualTo("defaultUserName2");
        }).verifyComplete();

        Mono<User> userFromDbase = userRepository.findById(new ObjectId(user.id()));
        User block = userFromDbase.block();
        assertThat(block).isNotNull();
        assertThat(block.id()).isNotNull().isEqualTo(user.id());

    }
}
