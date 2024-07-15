package com.backend.musicApp.controllers;

import com.backend.musicApp.model.GlobalPodcasts;
import com.backend.musicApp.repository.GlobalPodcastsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/globalPodcasts")
public class GlobalPodcastsController {
    private final GlobalPodcastsRepository globalPodcastsRepository;
    @Autowired
    public GlobalPodcastsController(GlobalPodcastsRepository globalPodcastsRepository) {
        this.globalPodcastsRepository = globalPodcastsRepository;
    }

    /**
     * Retrieves all global podcasts.
     *
     * @return A ResponseEntity containing a list of all global podcasts.
     */
    @GetMapping
    public ResponseEntity<List<GlobalPodcasts>> getAllGlobalPodcasts() {
        return ResponseEntity.ok(globalPodcastsRepository.findAll());
    }

    /**
     * Creates a new global podcast.
     *
     * @param globalPodcasts The GlobalPodcasts object to be created.
     * @return A ResponseEntity containing the created global podcast and HTTP status created.
     */
    @PostMapping
    public ResponseEntity<?> createGlobalPodcast(@ModelAttribute GlobalPodcasts globalPodcasts) {
        globalPodcastsRepository.save(globalPodcasts);
        return new ResponseEntity<>(globalPodcasts, HttpStatus.CREATED);
    }

}
