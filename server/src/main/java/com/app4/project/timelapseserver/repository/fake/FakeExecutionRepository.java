package com.app4.project.timelapseserver.repository.fake;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapse.model.request.ExecutionPatchRequest;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.util.IdPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.app4.project.timelapseserver.configuration.ApplicationConfiguration.MAX_EXECUTIONS;

@Profile("fake")
@Component
public class FakeExecutionRepository implements ExecutionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(FakeExecutionRepository.class);
  private final IdPool idPool = new IdPool(MAX_EXECUTIONS);
  private final BlockingQueue<Execution> executions = new PriorityBlockingQueue<>(MAX_EXECUTIONS);

  @Override
  public int count() {
    return executions.size();
  }

  @Override
  public Optional<Execution> getById(int id) {
    return executions
      .stream()
      .filter(e -> e.getId() == id)
      .findFirst();
  }

  @Override
  public List<Execution> getAll() {
    return new ArrayList<>(executions);
  }

  @Override
  public void add(Execution execution) {
    execution.setId(idPool.get());
    executions.add(execution);
  }

  @Override
  public boolean remove(int id) {
    if (executions.removeIf(e -> e.getId() == id)) {
      idPool.free(id);
      return true;
    }
    return false;
  }

  @Override
  public Optional<Execution> getSoonest() {
    return executions.isEmpty() ? Optional.empty() : Optional.of(executions.peek());
  }

  @Override
  public Execution update(int id, ExecutionPatchRequest request) {
    Execution ex = executions.stream().filter(e -> e.getId() == id).findFirst()
      .orElseThrow(() ->
        new NotFoundException(String.format("Execution with id %d doesn't exists", id)));
    updateExecution(ex, request);
    return ex;
  }

  void updateExecution(Execution ex, ExecutionPatchRequest request) {
    if (request.getEndTime() != null) {
      ex.setEndTime(request.getEndTime());
    }
    if (request.getPeriod() != null) {
      ex.setPeriod(request.getPeriod());
    }
    if (request.getStartTime() != null) {
      ex.setStartTime(request.getStartTime());
    }
    if (request.getTitle() != null) {
      ex.setTitle(request.getTitle());
    }
  }

  @Override
  public void removeAll() {
    executions.clear();
    for (int i = 0; i < MAX_EXECUTIONS; i++) {
      idPool.free(i);
    }
  }
}
