package com.backend.musicApp.repository;

import com.backend.musicApp.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ImageRepository extends MongoRepository<Image, String> {
    boolean existsImageById(String id);
}
