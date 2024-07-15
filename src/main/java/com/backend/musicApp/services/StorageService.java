package com.backend.musicApp.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageService {
    private final AmazonS3 space;

    public StorageService() {
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials("ACCESS_KEY", "SECRET_KEY")
        );
        space = AmazonS3ClientBuilder
                .standard()
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("fra1.digitaloceanspaces.com", "fra1")
                )
                .build();
    }

    /**
     * Retrieves the list of song file names from the "musicapp" bucket.
     *
     * @return List of song file names.
     */
    public List<String> getSongFileNames() {

        ListObjectsV2Result result = space.listObjectsV2("musicapp");
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        return objects.stream()
                .map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    /**
     * Uploads a song file to the "musicapp" bucket and returns its public URL.
     *
     * @param file The song file to upload.
     * @return The public URL of the uploaded song.
     * @throws IOException If an error occurs during file processing.
     */
    public String uploadSong(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        List<String> existingSongFileNames = getSongFileNames();
        if (existingSongFileNames.contains(fileName)) {
            throw new IllegalArgumentException("A song with the same name already exists.");
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        space.putObject(new PutObjectRequest("musicapp", file.getOriginalFilename(), file.getInputStream(), objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
        return space.getUrl("musicapp", file.getOriginalFilename()).toString();
    }

    /**
     * Uploads an image file to the "imagesmusicapp" bucket and returns its public URL.
     *
     * @param file The image file to upload.
     * @return The public URL of the uploaded image.
     * @throws IOException If an error occurs during file processing.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        String fileName = file.getOriginalFilename();
        space.putObject(new PutObjectRequest("imagesmusicapp", fileName, file.getInputStream(), objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
        return space.getUrl("imagesmusicapp", fileName).toString();
    }



}