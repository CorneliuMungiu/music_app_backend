package com.backend.musicApp.repository;

import com.backend.musicApp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
}