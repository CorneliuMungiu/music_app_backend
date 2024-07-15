package com.backend.musicApp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Song {
    @Id
    private String id;

    private String fileName;
    private String title;
    private String artist;
    private String photoUrl;
    private String songUrl;
    private String time;
    private int playCount;
    private String genre;
}
