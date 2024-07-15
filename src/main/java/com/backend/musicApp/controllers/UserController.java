package com.backend.musicApp.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.musicApp.model.PlayList;
import com.backend.musicApp.model.Song;
import com.backend.musicApp.model.User;
import com.backend.musicApp.repository.PlayListRepository;
import com.backend.musicApp.repository.SongRepository;
import com.backend.musicApp.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;
    private final PlayListRepository playListRepository;
    private final SongRepository songRepository;

    @Autowired
    public UserController(UserRepository userRepository, PlayListRepository playListRepository, SongRepository songRepository) {
        this.userRepository = userRepository;
        this.playListRepository = playListRepository;
        this.songRepository = songRepository;
    }

    /**
     * Retrieves all users.
     *
     * @return A response containing the list of all users.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Retrieves the current user based on the JWT token in the cookies.
     *
     * @param request The HTTP request containing the cookies.
     * @return A response containing the current user, or a not found status if the user does not exist.
     */
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("MusicAppAccessToken".equals(cookie.getName())) {
                    DecodedJWT decodedJWT = JWT.decode(cookie.getValue());
                    String email = decodedJWT.getClaim("email").asString();
                    return ResponseEntity.ok(userRepository.findByEmail(email));
                }
            }
        }
        return (ResponseEntity<User>) ResponseEntity.notFound();
    }

    /**
     * Retrieves the liked songs of a user.
     *
     * @param id The ID of the user.
     * @return A response containing the list of liked songs.
     */
    @GetMapping("/{id}/getLikedSongs")
    public ResponseEntity<List<Song>> getLikedSongs(@PathVariable String id) {
        List<Song> returnSongs = new ArrayList<>();
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User currentUser = user.get();
            List<String> songs = currentUser.getLikedSongs();
            for(String song : songs) {
                Optional<Song> songOptional = songRepository.findById(song);
                songOptional.ifPresent(returnSongs::add);
            }
        }
        return ResponseEntity.ok(returnSongs);
    }

    /**
     * Retrieves the playlists followed by a user.
     *
     * @param userId The ID of the user.
     * @return A response containing the list of followed playlists.
     */
    @GetMapping("/{userId}/getFollow")
    public ResponseEntity<List<String>> getFollowing(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User currentUser = user.get();
            List<String> playLists = currentUser.getPlayLists();
            return ResponseEntity.ok(playLists);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Adds a song to the liked songs of a user.
     *
     * @param userId The ID of the user.
     * @param songId The ID of the song to be added.
     * @return A response indicating the result of the operation.
     */
    @PostMapping("/{userId}/addToLiked")
    public ResponseEntity<?> addToLiked(@PathVariable String userId, @RequestParam String songId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.addToLikedSongs(songId);
            userRepository.save(user);

            return ResponseEntity.ok("Playlist added to user successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a song from the liked songs of a user.
     *
     * @param userId The ID of the user.
     * @param songId The ID of the song to be removed.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{userId}/deleteLiked")
    public ResponseEntity<?> deleteLiked(@PathVariable String userId, @RequestParam String songId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.removeFromLikedSongs(songId);
            userRepository.save(user);
            return ResponseEntity.ok("Playlist deleted from user successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Adds a playlist to a user's followed playlists.
     *
     * @param userId The ID of the user.
     * @param playlistId The ID of the playlist to be added.
     * @return A response indicating the result of the operation.
     */
    @PostMapping("/{userId}/addPlaylistToUser")
    public ResponseEntity<?> addPlaylistToUser(@PathVariable String userId,
                                               @RequestParam String playlistId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<PlayList> playlistOptional = playListRepository.findById(playlistId);

        if (userOptional.isPresent() && playlistOptional.isPresent()) {
            User user = userOptional.get();
            PlayList playlist = playlistOptional.get();
            playlist.setFollows(playlist.getFollows() + 1);
            playListRepository.save(playlist);

            user.getPlayLists().add(playlist.getId());
            userRepository.save(user);

            return ResponseEntity.ok("Playlist added to user successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a playlist from a user's followed playlists.
     *
     * @param userId The ID of the user.
     * @param playlistId The ID of the playlist to be removed.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{userId}/deletePlayListFromUser")
    public ResponseEntity<?> deletePlayListFromUser(@PathVariable String userId,
                                                    @RequestParam String playlistId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> playlists = user.getPlayLists();
            if(playlists.contains(playlistId)) {
                user.getPlayLists().remove(playlistId);
                Optional<PlayList> playlistOptional = playListRepository.findById(playlistId);
                if(playlistOptional.isPresent()) {
                    PlayList playlist = playlistOptional.get();
                    playlist.setFollows(playlist.getFollows() - 1);
                    playListRepository.save(playlist);
                }
            }
            userRepository.save(user);
            return ResponseEntity.ok("Playlist deleted from user successfully.");
        }
        return ResponseEntity.notFound().build();
    }

}
