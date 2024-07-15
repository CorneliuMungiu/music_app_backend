package com.backend.musicApp.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class MusicDto {
    private String id;
    private String title;
    private String artist;
    private boolean isFavorited;
    private String fileName;
    private String fileUrl;

    public MusicDto(String id, String title, String artist, String fileName, String fileUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
