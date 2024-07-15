package com.backend.musicApp.repository;

import com.backend.musicApp.model.PlayList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlayListRepository extends MongoRepository<PlayList, String> {

}
