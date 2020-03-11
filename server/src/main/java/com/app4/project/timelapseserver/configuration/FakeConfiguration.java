package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.repository.FakeExecutionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("fake")
@Configuration
public class FakeConfiguration {

  @Bean
  public ExecutionRepository executionRepository() {
    return new FakeExecutionRepository();
  }

}
