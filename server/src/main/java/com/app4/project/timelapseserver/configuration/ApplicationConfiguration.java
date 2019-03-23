package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import com.app4.project.timelapseserver.security.Role;
import com.app4.project.timelapseserver.security.UserDetailsImpl;
import com.app4.project.timelapseserver.service.FirebaseStorageService;
import com.app4.project.timelapseserver.service.StorageService;
import com.app4.project.timelapseserver.utils.IdPool;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
public class ApplicationConfiguration {

  public static final int MAX_EXECUTIONS = 10;
  public static final int MAX_COMMANDS = 10;

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
  public Map<Integer, Path> fileMap() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  public IdPool idPool() {
    return new IdPool(MAX_EXECUTIONS);
  }



}
