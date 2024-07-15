package com.backend.musicApp;

import com.backend.musicApp.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MusicAppApplication {


	public static void main(String[] args) {
		ApplicationContext context = (ApplicationContext) SpringApplication.run(MusicAppApplication.class, args);
		StorageService storageService = context.getBean(StorageService.class);
		storageService.getSongFileNames();
	}

}
