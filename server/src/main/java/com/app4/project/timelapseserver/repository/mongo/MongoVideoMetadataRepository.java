package com.app4.project.timelapseserver.repository.mongo;

import com.app4.project.timelapse.model.VideoMetadata;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Component
public class MongoVideoMetadataRepository extends MongoRepository<VideoMetadata> implements VideoMetadataRepository {

  private static final String COLLECTION_NAME = "VideoMetadata";

  public MongoVideoMetadataRepository(MongoTemplate mongoTemplate) {
    super(mongoTemplate, VideoMetadata.class, COLLECTION_NAME);
  }

  @Override
  public Optional<VideoMetadata> getByVideoId(int videoId) {
    return findOne(Query.query(Criteria.where("videoId").is(videoId)));
  }

  @Override
  public List<VideoMetadata> getAllByExecutionId(int executionId) {
    return mongoTemplate.find(Query.query(Criteria.where("executionId").is(executionId)), clazz);
  }
}
