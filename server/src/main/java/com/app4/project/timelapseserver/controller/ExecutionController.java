package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.service.StorageService;
import com.app4.project.timelapseserver.utils.IdPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);
  private final BlockingQueue<Execution> executions;
  private final StorageService storageService;
  private final CameraState cameraState;
  private final IdPool idPool;

  public ExecutionController(BlockingQueue<Execution> executions, StorageService storageService,
                             CameraState cameraState, IdPool idPool) {
    this.executions = executions;
    this.storageService = storageService;
    this.cameraState = cameraState;
    this.idPool = idPool;
  }

  @PostMapping("/")
  public ResponseEntity addExecution(@RequestBody Execution execution) {
    if (executions.size() >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Max number of executions reached");
    }
    executions.offer(execution);
    execution.setId(idPool.get());
    LOGGER.info("New execution was added: {}", execution);
    return ResponseEntity.ok(execution);
  }

  @GetMapping("/{id}")
  public ResponseEntity getExecution(@PathVariable int id) {
    idCheck(id);
    if (executions.isEmpty()) {
      throw new BadRequestException("There isn't any execution to get");
    }
    return ResponseEntity.ok(executions
      .stream()
      .filter(e -> e.getId() == id)
      .findFirst()
      .orElseThrow(() -> new BadRequestException("There isn't any execution with the specified id  get"))
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity removeExecution(@PathVariable int id) {
    idCheck(id);
    if (executions.removeIf(e -> e.getId() == id)) {
      idPool.free(id);
      storageService.deleteForExecution(id);
      LOGGER.info("Execution with id {} was removed", id);
      return ResponseEntity.ok(Boolean.TRUE);
    }
    return ResponseEntity.ok(Boolean.FALSE);
  }

  @PutMapping("/{id}")
  public ResponseEntity updateExecution(@PathVariable int id, @RequestBody Execution execution) {
    if (!executions.removeIf(e -> e.getId() == id)) {
      throw new BadRequestException("Execution with id " + id + " doesn't exists");
    }
    execution.setId(id);
    executions.offer(execution);

    return ResponseEntity.ok(execution);
  }

  @GetMapping("/count")
  public ResponseEntity nbExecutions() {
    return ResponseEntity.ok().body(executions.size());
  }

  @GetMapping("/soonest")
  public ResponseEntity soonestExecution() {
    if (executions.isEmpty()) {
      throw new BadRequestException("There isn't any execution to get");
    }
    return ResponseEntity.ok(executions.peek());
  }

  @GetMapping("/current")
  public ResponseEntity current() {
    Execution execution;
    if (executions.isEmpty()) {
      execution = null;
    } else {
      execution = executions.peek();
    }
    cameraState.setCurrentExecution(execution);
    return ResponseEntity.ok(execution);
  }

  @GetMapping("/")
  public Execution[] allExecutions() {
    Execution[] executions = this.executions.toArray(new Execution[0]);
    Arrays.sort(executions); //sort in startTime order (the soon to far)
    return executions;
  }

  private void idCheck(int executionId) {
    if (executionId < 0 || executionId >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Execution with id " + executionId + " cannot exist");
    }
  }

  @PostConstruct //TODO TO REMOVE ONCE WE HAVE REAL DATA, ALSO REMOVE IMAGES IN RESSOURCES
  public void fillWithFakeData() {
    LOGGER.info("Filling the server with fake data");
    long now = System.currentTimeMillis();
    long day = 1000 * 60 * 60 * 24;
    String[] titles = new String[] {
      "Levee de la lune",
      "floraison tulipe",
      "couché de soleil"
    };

    for (int i = 0; i < titles.length; i++) {
      long startTime = now + (i + 1) * day;
      long endTime = startTime + day / 4;
      Execution execution = new Execution(titles[i], startTime, endTime, 5 +  (long) (Math.random() * 10));
      execution.setId(idPool.get());
      executions.add(execution);
    }

    Execution e = new Execution("Now execution", now, now + day, 5 +  (long) (Math.random() * 10));
    e.setId(idPool.get());
    executions.add(e);

    LOGGER.info("Executions: {}", executions);
  }
}
