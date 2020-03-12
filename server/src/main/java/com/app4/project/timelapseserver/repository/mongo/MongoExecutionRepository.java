package com.app4.project.timelapseserver.repository.mongo;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.model.request.ExecutionPatchRequest;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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

  @Override
  public Execution update(int id, ExecutionPatchRequest request) {
    Update update = new Update();
    if (request.getEndTime() != null) {
      update.set("endTime", request.getEndTime());
    }
    if (request.getPeriod() != null) {
      update.set("period", request.getPeriod());
    }
    if (request.getStartTime() != null) {
      update.set("startTime", request.getStartTime());
    }
    if (request.getTitle() != null) {
      update.set("title", request.getTitle());
    }
    return mongoTemplate.findAndModify(queryById(id), update, clazz);
  }
}
