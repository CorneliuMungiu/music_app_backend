package com.backend.musicApp.controllers;

import com.backend.musicApp.model.GlobalPlayLists;
import com.backend.musicApp.model.PlayList;
import com.backend.musicApp.repository.GlobalPlayListsRepository;
import com.backend.musicApp.repository.PlayListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/globalPlayLists")
public class GlobalPlayListsController {
    private final GlobalPlayListsRepository globalPlayListsRepository;
    private final PlayListRepository playListRepository;
    private final SongController songController;

    @Autowired
    public GlobalPlayListsController(GlobalPlayListsRepository globalPlayListsRepository, PlayListRepository playListRepository, SongController songController) {
        this.globalPlayListsRepository = globalPlayListsRepository;
        this.playListRepository = playListRepository;
        this.songController = songController;
    }

    /**
     * Retrieves all global playlists.
     *
     * @return A ResponseEntity containing a list of all global playlists.
     */
    @GetMapping
    public ResponseEntity<List<GlobalPlayLists>> getAllGlobalPlayLists() {
        return ResponseEntity.ok(globalPlayListsRepository.findAll());
    }

    /**
     * Adds a new global playlist.
     *
     * @param globalPlayLists The GlobalPlayLists object to be added.
     * @return A ResponseEntity containing the added global playlist and HTTP status created.
     */
    @PostMapping
    public ResponseEntity<?> addGlobalPlayList(@ModelAttribute GlobalPlayLists globalPlayLists) {
        globalPlayListsRepository.save(globalPlayLists);
        return new ResponseEntity<>(globalPlayLists, HttpStatus.CREATED);
    }

    /**
     * Updates the global playlist with the top 5 most followed playlists.
     *
     * @return A ResponseEntity indicating the update status.
     */
    @PutMapping("/updateMostFollowed")
    public ResponseEntity<?> updateMostFollowed() {
        List<PlayList> allPlayLists = playListRepository.findAll();

        List<PlayList> top5PlayLists = allPlayLists.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getFollows(), p1.getFollows()))
                .limit(5)
                .toList();

        List<String> top5PlayListIds = top5PlayLists.stream()
                .map(PlayList::getId)
                .toList();

        Optional<GlobalPlayLists> optionalGlobalPlayLists= globalPlayListsRepository.findById("666f2abdec9b8424da1d9dbe");
        if (optionalGlobalPlayLists.isPresent()) {
            GlobalPlayLists globalPlayLists = optionalGlobalPlayLists.get();
            globalPlayLists.setPlayLists(top5PlayListIds);
            globalPlayListsRepository.save(globalPlayLists);
        } else{
            ResponseEntity.ok("PlayList Not Found");
        }

        return ResponseEntity.ok("PlayList Updated");
    }

    /**
     * Updates the genre mixed playlist with songs of the same genre.
     *
     * @return A ResponseEntity indicating the update status.
     */
    @PutMapping("/updateGenreMixed")
    public ResponseEntity<?> updateGenreMixed() {
        Optional<GlobalPlayLists> optionalGlobalPlayLists = globalPlayListsRepository.findById("6671e1ca33ac1a23f8a8177c");
        if (optionalGlobalPlayLists.isPresent()) {
            GlobalPlayLists globalPlayLists = optionalGlobalPlayLists.get();
            List<String> playListIds = globalPlayLists.getPlayLists();
            for (String playListId : playListIds) {
                Optional<PlayList> playList = playListRepository.findById(playListId);

                if (playList.isPresent()) {
                    PlayList currentPlayList = playList.get();
                    currentPlayList.setSongs(songController.getGenreSongs(playList.get().getName()));
                    playListRepository.save(currentPlayList);
                }
            }
        }
        return ResponseEntity.ok("Updated Successfully");
    }
}
