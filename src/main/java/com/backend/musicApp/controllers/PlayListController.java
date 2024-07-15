package com.backend.musicApp.controllers;

import com.backend.musicApp.dto.PlayListDto;
import com.backend.musicApp.model.PlayList;
import com.backend.musicApp.model.Song;
import com.backend.musicApp.model.User;
import com.backend.musicApp.repository.PlayListRepository;
import com.backend.musicApp.repository.SongRepository;
import com.backend.musicApp.repository.UserRepository;
import com.backend.musicApp.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/playList")
public class PlayListController {
    private final PlayListRepository playListRepository;
    private final SongRepository songRepository;
    private final StorageService storageService;
    private final SongController songController;
    private final UserRepository userRepository;

    @Autowired
    public PlayListController(PlayListRepository playListRepository,
                              SongRepository songRepository,
                              StorageService storageService,
                              SongController songController,
                              UserRepository userRepository) {
        this.playListRepository = playListRepository;
        this.songRepository = songRepository;
        this.storageService = storageService;
        this.songController = songController;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all playlists.
     *
     * @return A list of all playlists.
     */
    @GetMapping
    public List<PlayList> getAllPlayLists() {
        return playListRepository.findAll();
    }

    /**
     * Retrieves a playlist by its ID.
     *
     * @param id The ID of the playlist.
     * @return The playlist if found, or an empty playlist if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayList> getPlayListById(@PathVariable String id) {
        Optional<PlayList> playListOptional = playListRepository.findById(id);
        return playListOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(new PlayList()));
    }

    /**
     * Retrieves the name of a playlist by its ID.
     *
     * @param id The ID of the playlist.
     * @return The name of the playlist if found.
     */
    @GetMapping("/{id}/getPlayListName")
    public ResponseEntity<String> getPlayListByName(@PathVariable String id) {
        Optional<PlayList> playListOptional = playListRepository.findById(id);
        if (playListOptional.isPresent()) {
            PlayList playList = playListOptional.get();
            return ResponseEntity.ok(playList.getName());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves the owner of a playlist by its ID.
     *
     * @param id The ID of the playlist.
     * @return The owner's ID if found.
     */
    @GetMapping("/{id}/getPlayListOwner")
    public ResponseEntity<String> getPlayListOwner(@PathVariable String id) {
        Optional<PlayList> playListOptional = playListRepository.findById(id);
        if (playListOptional.isPresent()) {
            PlayList playList = playListOptional.get();
            return ResponseEntity.ok(playList.getOwnerId());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves the total views of all songs in a playlist by its ID.
     *
     * @param id The ID of the playlist.
     * @return The total views count of all songs in the playlist.
     */
    @GetMapping("/{id}/getViews")
    public ResponseEntity<String> getPlayListViews(@PathVariable String id) {
        Optional<PlayList> playListOptional = playListRepository.findById(id);
        int counter = 0;
        if (playListOptional.isPresent()) {
            List<String> songIds = playListOptional.get().getSongs();
            for (String songId : songIds) {
                Optional<Song> songOptional = songRepository.findById(songId);
                if (songOptional.isPresent()) {
                    Song song = songOptional.get();
                    counter += song.getPlayCount();
                }
            }

        }
        return ResponseEntity.ok(Integer.toString(counter));
    }

    /**
     * Retrieves all songs from a playlist by its ID.
     *
     * @param playListId The ID of the playlist.
     * @return A list of songs in the playlist.
     */
    @GetMapping("/{playListId}/songs")
    public ResponseEntity<List<Song>> getSongsFromPlayList(@PathVariable String playListId) {
        Optional<PlayList> playListOptional = playListRepository.findById(playListId);
        List<Song> musicList = new ArrayList<>();

        if (playListOptional.isPresent()) {
            List<String> songIds = playListOptional.get().getSongs();

            for (String songId : songIds) {
                ResponseEntity<Song> response = songController.getSong(songId);
                if (response.getStatusCode() == HttpStatus.OK) {
                    musicList.add(response.getBody());
                }
            }
        }
        return ResponseEntity.ok(musicList);
    }

    /**
     * Retrieves the follower count of a playlist by its ID.
     *
     * @param playListId The ID of the playlist.
     * @return The number of followers if found, otherwise 0.
     */
    @GetMapping("/{playListId}/getFollowersCount")
    public ResponseEntity<Integer> getFollowersCount(@PathVariable String playListId) {
        Optional<PlayList> playListOptional = playListRepository.findById(playListId);
        if (playListOptional.isPresent()) {
            PlayList playList = playListOptional.get();
            return ResponseEntity.ok(playList.getFollows());
        }
        return ResponseEntity.ok(0);
    }

    /**
     * Checks if a user is following a playlist.
     *
     * @param playListId The ID of the playlist.
     * @param userId The ID of the user.
     * @return True if the user is following the playlist, otherwise false.
     */
    @GetMapping("/{playListId}/isFollowing")
    public ResponseEntity<Boolean> isFollowing(@PathVariable String playListId, @RequestParam String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> playLists = user.getPlayLists();
            if (playLists.contains(playListId)) {
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }

    /**
     * Creates a new playlist.
     *
     * @param playListDto The data transfer object containing playlist details.
     * @param userId The ID of the user creating the playlist.
     * @return The created playlist.
     * @throws IOException If an error occurs during file upload.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PlayList> createPlayList(@ModelAttribute PlayListDto playListDto,@RequestParam String userId) throws IOException {
        PlayList playList = new PlayList();
        playList.setOwnerId(userId);
        playList.setName(playListDto.name());
        playList.setFollows(0);
        playList.setSongs(playListDto.songs());
        playList.setPhotoUrl(storageService.uploadImage(playListDto.photo()));
        PlayList insertedPlayList = playListRepository.insert(playList);
        return new ResponseEntity<>(insertedPlayList, HttpStatus.CREATED);
    }

    /**
     * Creates a new empty playlist for a user.
     *
     * @param userId The ID of the user.
     * @param playList The playlist to create.
     * @return The created empty playlist.
     */
    @PostMapping("/{userId}/newEmptyPlayList")
    public ResponseEntity<PlayList> createEmptyPlayList(@PathVariable String userId, @RequestBody PlayList playList) {
        playList.setPhotoUrl("");
        playList.setFollows(0);
        playList.setOwnerId(userId);
        PlayList createdPlayList = playListRepository.save(playList);
        return new ResponseEntity<>(createdPlayList, HttpStatus.CREATED);
    }

    /**
     * Adds a song to a playlist.
     *
     * @param playListId The ID of the playlist.
     * @param songId The ID of the song to add.
     * @return A response indicating the result of the operation.
     */
    @PostMapping("/{playListId}/addSongToPlayList")
    public ResponseEntity<?> addSongToPlayList(@PathVariable String playListId,
                                               @RequestParam String songId) {
        Optional<PlayList> playListOptional = playListRepository.findById(playListId);
        Optional<Song> songOptional = songRepository.findById(songId);

        if (playListOptional.isPresent() && songOptional.isPresent()) {
            PlayList playList = playListOptional.get();
            Song song = songOptional.get();

            if(playList.getSongs().contains(song.getId())) {
                return ResponseEntity.ok("Already Added");
            }

            playList.getSongs().add(song.getId());
            if(playList.getPhotoUrl().isBlank()){
                playList.setPhotoUrl(song.getPhotoUrl());
            }
            playListRepository.save(playList);

            return ResponseEntity.ok("Song added to playlist successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a playlist if the requesting user is the owner of the playlist.
     *
     * @param playListId The ID of the playlist to delete.
     * @param userId The ID of the user requesting the deletion.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{playListId}/deletePlayList")
    public ResponseEntity<?> deletePlayList(@PathVariable String playListId, @RequestParam String userId) {
        Optional<PlayList> playListOptional = playListRepository.findById(playListId);
        if (playListOptional.isPresent()) {
            PlayList playList = playListOptional.get();
            if (playList.getOwnerId() == null)
                return ResponseEntity.notFound().build();
            if(playList.getOwnerId().equals(userId)) {
                playListRepository.delete(playList);
                return ResponseEntity.ok("PlayList deleted successfully.");
            }
        }
        return ResponseEntity.ok("Not your business");
    }

}
