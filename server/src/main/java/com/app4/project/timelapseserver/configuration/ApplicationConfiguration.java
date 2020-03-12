package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.SavingProgress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  public Path tempDirRoot(@Value("${temp.dir.root}") String tempDirRootPath) {
    return Paths.get(tempDirRootPath);
  }

  @Bean
  public ExecutorService executorService(@Value("${executor.threads}") int nbThreads) {
    return Executors.newFixedThreadPool(nbThreads);
  }

  @Bean
  public ConcurrentMap<Integer, SavingProgress> executionSavingStateMap() {
    return new ConcurrentHashMap<>();
  }

}
