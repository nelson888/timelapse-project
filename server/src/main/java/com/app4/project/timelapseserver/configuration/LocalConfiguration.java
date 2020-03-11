package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.repository.FakeExecutionRepository;
import com.app4.project.timelapseserver.service.storage.LocalStorageService;
import com.app4.project.timelapseserver.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.FileSystem;
import java.nio.file.Paths;

@Profile("local")
@Configuration
public class LocalConfiguration {

  @Value("${local.storage.root}")
  private String storageRoot;

  @Bean
  public StorageService storageService(FileSystem fileSystem) {
    return new LocalStorageService(fileSystem, Paths.get(storageRoot));
  }

  //@Bean
  public ExecutionRepository executionRepository() {
    return new FakeExecutionRepository();
  }


}
