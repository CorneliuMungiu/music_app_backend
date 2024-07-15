package com.backend.musicApp.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageDto{
    private String name;
    private MultipartFile file;

    public ImageDto(String name, MultipartFile file) {
        this.name = name;
        this.file = file;
    }

}
