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

    public Mono<User> findUserById(ObjectId id) {
        if (id == null) {
            return Mono.empty();
        }
        return userRepository.findById(id);
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> updateUser(ObjectId id, User user) {
        return userRepository.findById(id).flatMap(existingUser -> {
            // Создание нового объекта User с существующим id и обновлёнными данными
            User updatedUser = new User(existingUser.id(), user.username(), user.email());
            return userRepository.save(updatedUser);
        });
    }

    public Mono<Void> deleteUserById(ObjectId id) {
        return userRepository.deleteById(id);
    }
}
