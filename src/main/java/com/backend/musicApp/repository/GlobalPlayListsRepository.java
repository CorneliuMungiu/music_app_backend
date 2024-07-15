package com.backend.musicApp.repository;

import com.backend.musicApp.model.GlobalPlayLists;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GlobalPlayListsRepository extends MongoRepository<GlobalPlayLists, String> {
//    Optional findByName(String name);
}
