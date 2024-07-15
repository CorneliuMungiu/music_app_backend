package com.backend.musicApp.controllers;

import com.backend.musicApp.dto.ImageDto;
import com.backend.musicApp.model.Image;
import com.backend.musicApp.repository.ImageRepository;
import com.backend.musicApp.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/images")
public class ImageController {

    private final StorageService storageService;
    private final ImageRepository imageRepository;

    @Autowired
    public ImageController(StorageService storageService, ImageRepository imageRepository) {
        this.storageService = storageService;
        this.imageRepository = imageRepository;
    }

    /**
     * Retrieves all images.
     *
     * @return A ResponseEntity containing a list of all images.
     */
    @GetMapping
    public ResponseEntity<List<Image>> getImages(){
        return ResponseEntity.ok(imageRepository.findAll());
    }

    /**
     * Retrieves an image by its ID.
     *
     * @param id The ID of the image to retrieve.
     * @return A ResponseEntity containing the image, or a not found status if the image does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImage(@PathVariable String id) {
        Optional<Image> image = imageRepository.findById(id);
        return image.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Uploads a new image.
     *
     * @param imageDto The ImageDto containing the image file and name.
     * @return A ResponseEntity containing the created image and HTTP status created.
     * @throws IOException If an error occurs during file upload.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@ModelAttribute ImageDto imageDto) throws IOException {
        Image image = new Image();
        image.setName(imageDto.getName());
        image.setUrl(storageService.uploadImage(imageDto.getFile()));
        Image insertedImage = imageRepository.insert(image);
        return new ResponseEntity<>(insertedImage, HttpStatus.CREATED);
    }

    /**
     * Deletes an image by its ID.
     *
     * @param id The ID of the image to delete.
     * @return A ResponseEntity indicating the deletion status.
     */
    @DeleteMapping("/id")
    public ResponseEntity<?> deleteImage(@RequestParam String id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            imageRepository.delete(image.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

