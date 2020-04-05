package com.app4.project.timelapseserver.repository.mongo;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.model.request.ExecutionPatchRequest;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.util.IdPool;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.app4.project.timelapseserver.configuration.ApplicationConfiguration.MAX_EXECUTIONS;

@Profile("mongodb")
@Component
public class MongoExecutionRepository extends MongoRepository<Execution> implements ExecutionRepository {

  private static final String COLLECTION_NAME = "Execution";

  private final IdPool idPool = new IdPool(MAX_EXECUTIONS);

  public MongoExecutionRepository(MongoTemplate mongoTemplate) {
    super(mongoTemplate, Execution.class, COLLECTION_NAME);
    getAll().stream().forEach(e -> idPool.setTook(e.getId()));
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
  public void add(Execution object) {
    object.setId(idPool.get());
    super.add(object);
  }

  @Override
  public boolean remove(int id) {
    if (super.remove(id)) {
      idPool.free(id);
      return true;
    }
    return false;
  }

  @Override
  public Execution update(int id, ExecutionPatchRequest request) {
    Update update = new Update();
    Optional.ofNullable(request.getTitle()).ifPresent(title -> update.set("title", title));
    Optional.ofNullable(request.getStartTime()).ifPresent(startTime -> update.set("startTime", startTime));
    Optional.ofNullable(request.getEndTime()).ifPresent(endTime -> update.set("endTime", endTime));
    Optional.ofNullable(request.getPeriod()).ifPresent(period -> update.set("period", period));

    return mongoTemplate.findAndModify(queryById(id), update, clazz);
  }
}
