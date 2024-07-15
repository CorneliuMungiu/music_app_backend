package com.backend.musicApp.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PlayListDto(String name, MultipartFile photo, List<String>songs) {
}
