package com.app4.project.timelapseserver.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Profile("mongodb")
@Configuration
public class MongoConfiguration {

  @Bean
  public MongoClientURI mongoUri(@Value("${mongo.uri}") String mongoUri) {
    return new MongoClientURI(mongoUri);
  }

  @Bean
  public MongoClient mongoClient(MongoClientURI uri) {
    return new MongoClient(uri);
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoClient mongoClient, @Value("${mongo.database}") String database) {
    return new MongoTemplate(mongoClient, database);
  }

}
