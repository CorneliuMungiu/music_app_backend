package com.backend.musicApp.dto;
import org.springframework.web.multipart.MultipartFile;

public record SongDto(String fileName,
                      String title,
                      String artist,
                      String genre,
                      MultipartFile file,
                      MultipartFile photo) {
}
