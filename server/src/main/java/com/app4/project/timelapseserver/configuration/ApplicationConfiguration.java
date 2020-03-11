package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.google.common.jimfs.Jimfs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ApplicationConfiguration {

  public static final int MAX_EXECUTIONS = 10;
  public static final int MAX_COMMANDS = 10;

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
  public FileSystem inMemoryFileSystem() {
    return Jimfs.newFileSystem();
  }

}
