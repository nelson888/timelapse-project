package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.model.Command;
import com.app4.project.timelapseserver.model.Execution;
import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ApplicationConfiguration {

  @Value("${storage.root.path}")
  private String rootPath;

  public static final int MAX_EXECUTIONS = 10;
  private static final int MAX_COMMANDS = 10;

  @Bean
  public Queue<Execution> executionsQueue() {
    return new ArrayBlockingQueue<>(MAX_EXECUTIONS);
  }

  @Bean
  public Queue<Command> commandsQueue() {
    return new ArrayBlockingQueue<>(MAX_COMMANDS);
  }

  @Bean
  public Path rootPath() {
    return Paths.get(rootPath);
  }

  @Bean
  public Map<Integer, Path> fileMap() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  public UserRepository userRepository() {
    return new LocalUserRepository(Arrays.asList());
  }
}
