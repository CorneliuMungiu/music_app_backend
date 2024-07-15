package com.backend.musicApp.repository;

import com.backend.musicApp.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {

    boolean existsSongByFileNameEquals(String filename);

    boolean existsSongByTitleEquals(String filename);

}
