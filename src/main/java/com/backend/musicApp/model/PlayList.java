package com.backend.musicApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class PlayList {
    @Id
    private String id;
    private String ownerId;
    private String name;
    private String photoUrl;
    private List<String> songs;
    private int follows;
}
