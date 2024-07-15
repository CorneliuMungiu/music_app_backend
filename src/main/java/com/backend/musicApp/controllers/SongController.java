package com.backend.musicApp.controllers;

import com.backend.musicApp.dto.SongDto;
import com.backend.musicApp.model.Song;
import com.backend.musicApp.repository.SongRepository;
import com.backend.musicApp.services.StorageService;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/songs")
public class SongController {

    private final StorageService storageService;
    private final SongRepository songRepository;

    @Autowired
    public SongController(StorageService storageService, SongRepository songRepository) {
        this.storageService = storageService;
        this.songRepository = songRepository;
    }

    /**
     * Retrieves all songs.
     *
     * @return A response containing the list of all songs.
     */
    @GetMapping
    public ResponseEntity<List<Song>> getSongs(){
        return ResponseEntity.ok(songRepository.findAll());
    }

    /**
     * Retrieves songs by genre.
     *
     * @param genre The genre of songs to retrieve.
     * @return A list of song IDs that match the specified genre.
     */
    @GetMapping("/genre/{genre}")
    public List<String> getGenreSongs(@PathVariable String genre){
        List<Song> songs = songRepository.findAll();
        List<String> popSongs = new ArrayList<>();
        for (Song song : songs){
            if (song.getGenre().equals(genre)){
                popSongs.add(song.getId());
            }
        }
        return popSongs;
    }

    /**
     * Streams a song by its filename.
     *
     * @param filename The filename of the song to stream.
     * @param headers The HTTP headers of the request.
     * @return A response containing the streamed song resource.
     */
    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamSong(@PathVariable String filename, @RequestHeader HttpHeaders headers) {
        try {
            String cdnUrl = "https://musicapp.fra1.digitaloceanspaces.com" + filename;
            Resource resource = new UrlResource(cdnUrl);

            if (!resource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
            responseHeaders.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            List<HttpRange> httpRangeList = headers.getRange();
            if (!httpRangeList.isEmpty()) {
                HttpRange range = httpRangeList.getFirst();
                long fileLength = resource.contentLength();
                long start = range.getRangeStart(fileLength);
                long end = range.getRangeEnd(fileLength);
                responseHeaders.add("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(responseHeaders).body(resource);
            } else {
                return ResponseEntity.ok().headers(responseHeaders).body(resource);
            }
        } catch (MalformedURLException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a song by its ID.
     *
     * @param id The ID of the song to retrieve.
     * @return A response containing the song, or a not found status if the song does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Song> getSong(@PathVariable String id){
        Optional<Song> songOptional = songRepository.findById(id);
        if(songOptional.isPresent()){
            Song song = songOptional.get();
            return ResponseEntity.ok(song);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Formats the duration of a song.
     *
     * @param durationInSeconds The duration of the song in seconds.
     * @return The formatted duration string.
     */
    private String formatDuration(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Creates a new song.
     *
     * @param songDto The song data transfer object containing song details.
     * @return A response containing the created song, or an error message if the song already exists or there was an error.
     * @throws IOException If there is an error handling the song file.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createSong(@ModelAttribute SongDto songDto) throws IOException {
        Song song = new Song();
        song.setTitle(songDto.title());
        song.setArtist(songDto.artist());
        song.setFileName(songDto.fileName());
        song.setPhotoUrl(storageService.uploadImage(songDto.photo()));
        song.setGenre(songDto.genre());
        song.setPlayCount(0);

        if(songRepository.existsSongByFileNameEquals(songDto.file().getOriginalFilename()) || songRepository.existsSongByTitleEquals(song.getTitle())){
            return ResponseEntity.badRequest().body("taken");
        } else {
            File tempFile = File.createTempFile("temp", songDto.file().getOriginalFilename());
            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(songDto.file().getBytes());
            }
            try {
                Mp3File mp3file = new Mp3File(tempFile);
                long durationInSeconds = mp3file.getLengthInSeconds();
                String duration = formatDuration(durationInSeconds);
                song.setTime(duration);
            } catch (UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read song duration");
            }

            System.out.println("Uploading file");
            song.setSongUrl(storageService.uploadSong(songDto.file()));
            Song insertedSong = songRepository.insert(song);
            return new ResponseEntity<>(insertedSong, HttpStatus.CREATED);
        }
    }

    /**
     * Increments the play count of a song by its ID.
     *
     * @param id The ID of the song to increment the play count.
     * @return A response indicating the result of the operation.
     */
    @PutMapping("/{id}/incrementPlayCount")
    public ResponseEntity<?> incrementPlayCount(@PathVariable String id) {
        Optional<Song> optionalSong = songRepository.findById(id);
        if (optionalSong.isPresent()) {
            Song song = optionalSong.get();
            int currentPlayCount = song.getPlayCount();
            song.setPlayCount(currentPlayCount + 1);
            songRepository.save(song);
            return ResponseEntity.ok().body("Play count for song with ID " + id + " has been incremented");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates a song by its ID.
     *
     * @param id The ID of the song to update.
     * @param songData The new song data.
     * @return A response containing the updated song, or a not found status if the song does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable String id, @RequestBody Song songData){
        Optional<Song> songOptional = songRepository.findById(id);
        if (songOptional.isPresent()){
            Song song = songOptional.get();
            if (songData.getTitle() != null){
                song.setTitle(songData.getTitle());
            }
            if (songData.getArtist() != null){
                song.setArtist(songData.getArtist());
            }
            songRepository.save(song);
            return ResponseEntity.ok(song);
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}
