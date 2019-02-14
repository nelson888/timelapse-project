package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.User;
import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import com.app4.project.timelapseserver.service.FirebaseStorageService;
import com.app4.project.timelapseserver.service.StorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
public class ApplicationConfiguration {

  public static final int MAX_EXECUTIONS = 10;
  private static final int MAX_COMMANDS = 10;

  @Value("${firebase.database.url}")
  private String databaseUrl;
  @Value("${firebase.storage.bucket}")
  private String storageBucket;

  @Bean
  public BlockingQueue<Execution> executionsQueue() {
    return new PriorityBlockingQueue<>(MAX_EXECUTIONS);
  }

  @Bean
  public BlockingQueue<Command> commandsQueue() {
    return new ArrayBlockingQueue<>(MAX_COMMANDS);
  }

  @Bean
  public CameraState cameraState() {
    return new CameraState();
  }

  @Bean
  public StorageService storageService(Bucket bucket) {
    return new FirebaseStorageService(bucket);
  }

  @Bean
  public Map<Integer, Path> fileMap() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  public UserRepository userRepository() {
    return new LocalUserRepository(Arrays.asList(new User("android", "android"), new User("timelapse", "timelapse")));
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

  @Bean
  public Bucket bucket(StorageClient storageClient) {
    return storageClient.bucket(storageBucket);
  }


}
