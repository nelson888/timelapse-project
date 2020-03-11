package com.app4.project.timelapseserver.repository.mongo;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

public class MongoExecutionRepository extends MongoRepository<Execution> implements ExecutionRepository {

  private static final String COLLECTION_NAME = "Execution";

  public MongoExecutionRepository(MongoTemplate mongoTemplate) {
    super(mongoTemplate, Execution.class, COLLECTION_NAME);
  }

  @Override
  public Optional<Execution> getSoonest() {
    Query query = new Query();
    query.with(new Sort(Sort.Direction.ASC, "startTime"));
    return Optional.ofNullable(
      mongoTemplate.findOne(query, Execution.class, COLLECTION_NAME)
    );
  }
}
