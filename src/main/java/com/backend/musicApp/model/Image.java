package com.backend.musicApp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
public class Image {
    @Id
    private String id;
    private String name;
    private String url;
}
