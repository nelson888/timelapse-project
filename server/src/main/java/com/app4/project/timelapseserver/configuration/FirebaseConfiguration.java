package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.service.storage.FirebaseStorageService;
import com.app4.project.timelapseserver.service.storage.StorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile("firebase")
@Configuration
public class FirebaseConfiguration {

  @Value("${firebase.database.url}")
  private String databaseUrl;
  @Value("${firebase.storage.bucket}")
  private String storageBucket;

  @Bean
  public Bucket bucket(StorageClient storageClient) {
    return storageClient.bucket(storageBucket);
  }

  @Bean
  public StorageService storageService(Bucket bucket) {
    return new FirebaseStorageService(bucket);
  }

  @Bean
  public StorageClient storageClient() throws IOException {
    FirebaseOptions options = new FirebaseOptions.Builder()
      .setCredentials(GoogleCredentials.fromStream(ApplicationConfiguration.class.getResourceAsStream("/private/firebase-adminsdk.json")))
      .setDatabaseUrl(databaseUrl)
      .build();
    FirebaseApp.initializeApp(options);
    return StorageClient.getInstance();
  }

}
