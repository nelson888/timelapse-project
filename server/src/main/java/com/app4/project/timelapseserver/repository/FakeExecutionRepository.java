package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapse.model.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.app4.project.timelapseserver.configuration.ApplicationConfiguration.MAX_EXECUTIONS;

public class FakeExecutionRepository implements ExecutionRepository {

  private final IdPool idPool = new IdPool(MAX_EXECUTIONS);

  private static final Logger LOGGER = LoggerFactory.getLogger(FakeExecutionRepository.class);

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

  @PostConstruct //TODO TO REMOVE ONCE WE HAVE REAL DATA, ALSO REMOVE IMAGES IN RESSOURCES
  public void fillWithFakeData() {
    LOGGER.info("Filling the server with fake data");
    long now = System.currentTimeMillis();
    long day = 1000 * 60 * 60 * 24;
    String[] titles = new String[]{
      "Levee de la lune",
      "floraison tulipe",
      "couché de soleil"
    };

    for (int i = 0; i < titles.length; i++) {
      long startTime = now + (i + 1) * day;
      long endTime = startTime + day / 4;
      Execution execution = new Execution(titles[i], startTime, endTime, 5 + (long) (Math.random() * 10));
      execution.setId(idPool.get());
      executions.add(execution);
    }

    Execution e = new Execution("Now execution", now, now + day, 5 + (long) (Math.random() * 10));
    e.setId(idPool.get());
    executions.add(e);

    LOGGER.info("Executions: {}", executions);
  }


  private static class IdPool {

    private final boolean[] took;

    public IdPool(int max) {
      took = new boolean[max];
      Arrays.fill(took, false);
    }


    public int get() {
      int i = 0;
      while (took[i]) {
        i++;
        if (i >= took.length) {
          throw new RuntimeException("There isn't any free id");
        }
      }
      took[i] = true;
      return i;
    }

    public void free(int i) {
      if (i < 0 || i >= took.length) {
        return;
      }
      took[i] = false;
    }
  }

}