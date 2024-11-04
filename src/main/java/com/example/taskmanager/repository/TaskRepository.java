package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends ReactiveMongoRepository<Task, ObjectId> {
}
