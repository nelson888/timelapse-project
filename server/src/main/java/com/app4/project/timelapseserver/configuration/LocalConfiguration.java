package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.service.storage.LocalStorageService;
import com.app4.project.timelapseserver.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("local")
@Configuration
public class LocalConfiguration {

  @Bean
  public StorageService storageService(Path tempDirRoot,
                                       @Value("${local.storage.root}") String storageRoot) {
    return new LocalStorageService(tempDirRoot, Paths.get(storageRoot));
  }

}
