package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.User;
import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
public class ApplicationConfiguration {

  @Value("${storage.root.path}")
  private String rootPath;

  public static final int MAX_EXECUTIONS = 10;
  private static final int MAX_COMMANDS = 10;

  @Bean
  public BlockingQueue<Execution> executionsQueue() {
    return new PriorityBlockingQueue<>(MAX_EXECUTIONS);
  }

  @Bean
  public BlockingQueue<Command> commandsQueue() {
    return new ArrayBlockingQueue<>(MAX_COMMANDS);
  }

  @Bean
  public Path rootPath() {
    return Paths.get(rootPath.replaceFirst("^~", System.getProperty("user.home")));
  }

  @Bean
  public Map<Integer, Path> fileMap() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  public UserRepository userRepository() {
    return new LocalUserRepository(Arrays.asList(new User("android", "android"), new User("timelapse", "timelapse")));
  }
}
