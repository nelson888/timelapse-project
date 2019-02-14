package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);
  private final BlockingQueue<Execution> executions;
  private final StorageService storageService;

  public ExecutionController(BlockingQueue<Execution> executions, StorageService storageService) {
    this.executions = executions;
    this.storageService = storageService;
  }

  @PostMapping("/")
  public ResponseEntity addExecution(@RequestBody Execution execution) {
    execution.setId(executions.size());
    if (!executions.offer(execution)) {
      throw new BadRequestException("Max number of executions reached");
    }
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
      LOGGER.info("Execution with id {} was removed", id);
      storageService.deleteForExecution(id);
      return ResponseEntity.ok(Boolean.TRUE);
    }
    return ResponseEntity.ok(Boolean.FALSE);
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
}
