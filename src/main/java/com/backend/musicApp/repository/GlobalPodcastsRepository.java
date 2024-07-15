package com.backend.musicApp.repository;

import com.backend.musicApp.model.GlobalPlayLists;
import com.backend.musicApp.model.GlobalPodcasts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GlobalPodcastsRepository extends MongoRepository<GlobalPodcasts, String> {

}
