package com.app4.project.timelapseserver.repository.mongo;

import com.app4.project.timelapse.model.VideoMetadata;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

public class MongoVideoMetadataRepository extends MongoRepository<VideoMetadata> implements VideoMetadataRepository {

  private static final String COLLECTION_NAME = "VideoMetadata";

  public MongoVideoMetadataRepository(MongoTemplate mongoTemplate) {
    super(mongoTemplate, VideoMetadata.class, COLLECTION_NAME);
  }

  @Override
  public Optional<VideoMetadata> getByVideoId(int videoId) {
    return findOne(Query.query(Criteria.where("videoId").is(videoId)));
  }

}
