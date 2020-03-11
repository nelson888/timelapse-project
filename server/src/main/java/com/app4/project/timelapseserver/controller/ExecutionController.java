package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.exception.ConflictException;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);
  private final Executor executor = Executors.newSingleThreadExecutor();
  private final StorageService storageService;
  private final ExecutionRepository executionRepository;
  private final CameraState cameraState;

  public ExecutionController(ExecutionRepository executionRepository, StorageService storageService,
                             CameraState cameraState) {
    this.executionRepository = executionRepository;
    this.storageService = storageService;
    this.cameraState = cameraState;
  }

  @PostMapping
  public ResponseEntity addExecution(@RequestBody Execution execution) {
    if (executionRepository.count() >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Max number of executions reached");
    }
    if (executionRepository.getAll().stream().anyMatch(execution::overlaps)) {
      throw new ConflictException("Execution overlaps with another one");
    }
    executionRepository.add(execution);
    LOGGER.info("New execution was added: {}", execution);
    return ResponseEntity.ok(execution);
  }

  @GetMapping("/{id}")
  public ResponseEntity getExecution(@PathVariable int id) {
    idCheck(id);
    if (executionRepository.count() == 0) {
      throw new BadRequestException("There isn't any execution to get");
    }
    return ResponseEntity.ok(executionRepository.getById(id)
      .orElseThrow(() -> new BadRequestException("There isn't any execution with the specified id  get"))
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity removeExecution(@PathVariable int id) {
    idCheck(id);
    if (executionRepository.remove(id)) {
      executor.execute(() -> {
        storageService.deleteForExecution(id);
        LOGGER.info("Execution with id {} was removed", id);
      });
      return ResponseEntity.ok(Boolean.TRUE);
    } // TODO return not found no content
    return ResponseEntity.ok(Boolean.FALSE);
  }

  @PutMapping("/{id}")
  public ResponseEntity updateExecution(@PathVariable int id, @RequestBody Execution execution) {
    // TODO do it better
    if (!executionRepository.remove(id)) {
      throw new BadRequestException("Execution with id " + id + " doesn't exists");
    }
    execution.setId(id);
    executionRepository.add(execution);

    return ResponseEntity.ok(execution);
  }

  @GetMapping("/count")
  public ResponseEntity nbExecutions() {
    return ResponseEntity.ok().body(executionRepository.count());
  }

  @GetMapping("/soonest")
  public ResponseEntity soonestExecution() {
    return ResponseEntity.ok(executionRepository.getSoonest()
    .orElseThrow(() -> new BadRequestException("There isn't any execution to get")));
  }

  @GetMapping("/current")
  public ResponseEntity current() {
    for (Execution e : executionRepository.getAll()) {
      if (e.isRunning()) {
        cameraState.setCurrentExecution(e);
        return ResponseEntity.ok(e);
      }
    }
    return ResponseEntity.ok(null);
  }

  @GetMapping
  public ResponseEntity allExecutions() {
    return ResponseEntity.ok(executionRepository.getAll());
  }

  private void idCheck(int executionId) {
    if (executionId < 0 || executionId >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Execution with id " + executionId + " cannot exist");
    }
  }

}
