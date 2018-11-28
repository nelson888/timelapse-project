package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.Roles;
import com.app4.project.timelapse.model.User;
import com.app4.project.timelapseserver.model.UserDetailsServiceImpl;
import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  /**
  @Bean
  public UserRepository userRepository() {
    return new LocalUserRepository(Arrays.asList(new User("android", "android"), new User("timelapse", "timelapse")));
  }
**/

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  Set<User> users(PasswordEncoder passwordEncoder) {
    Set<User> set = new HashSet<>();
    set.add(new User("android", passwordEncoder.encode("android"), Roles.ANDROID));
    set.add(new User("raspberry", passwordEncoder.encode("raspberry"), Roles.TIMELAPSE));
    set.add(new User("test", passwordEncoder.encode("test"), Roles.ANDROID)); //TODO remove (just for testing)
    return Collections.unmodifiableSet(set);
  }

  @Bean
  UserDetailsService userDetailsService(Set<User> users) {
    return new UserDetailsServiceImpl(users);
  }

}
