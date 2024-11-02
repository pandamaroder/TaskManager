package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Flux<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> findUserById(String id) {
        if (id == null) {
            return Mono.empty();
        }
        return userRepository.findById(new ObjectId(id));
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> updateUser(String id, User user) {
        return userRepository.findById(new ObjectId(id)).flatMap(existingUser -> {

            User updatedUser = new User(existingUser.id(), user.username(), user.email());
            return userRepository.save(updatedUser);
        });
    }

    public Mono<Void> deleteUserById(String id) {
        return userRepository.deleteById(new ObjectId(id));
    }
}
