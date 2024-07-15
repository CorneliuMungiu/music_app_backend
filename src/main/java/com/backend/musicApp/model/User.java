package com.backend.musicApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private List<String> playLists;
    private List<String> likedSongs = new ArrayList<>();

    public void addToLikedSongs(String song) {
        likedSongs.add(song);
    }

    public void removeFromLikedSongs(String song) {
        likedSongs.remove(song);
    }
}
