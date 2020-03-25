package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.exception.ConflictException;
import com.app4.project.timelapseserver.model.request.ExecutionPatchRequest;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.service.SaveToVideoService;
import com.app4.project.timelapseserver.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);
  private final Executor executor = Executors.newSingleThreadExecutor();
  private final StorageService storageService;
  private final ExecutionRepository executionRepository;
  private final CameraState cameraState;
  private final SaveToVideoService saveToVideoService;
  private final int defaultFps;

  public ExecutionController(ExecutionRepository executionRepository, StorageService storageService,
                             CameraState cameraState, SaveToVideoService saveToVideoService,
                             @Value("${video.default.fps}") int defaultFps) {
    this.executionRepository = executionRepository;
    this.storageService = storageService;
    this.cameraState = cameraState;
    this.saveToVideoService = saveToVideoService;
    this.defaultFps = defaultFps;
  }

  @PostMapping
  public ResponseEntity addExecution(@RequestBody Execution execution) {
    if (executionRepository.count() >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Max number of executions reached");
    }
    if (executionRepository.getAll().stream().anyMatch(execution::overlaps)) {
      throw new ConflictException("Execution overlaps with another one");
    }
    validate(execution);
    executionRepository.add(execution);
    LOGGER.info("New execution was added: {}", execution);
    return ResponseEntity.ok(execution);
  }

  private void validate(Execution execution) {
    if (execution.getTitle() == null || execution.getTitle().isEmpty()) {
      throw new BadRequestException("The title must be set and not empty");
    }
    if (execution.getPeriod() <= 0) {
      throw new BadRequestException("The period must be greater than 0");
    }
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

  @PostMapping("/{id}/video/generate") // TODO add on swagger todo allow to have multiple videos for one execution????????
  public ResponseEntity startSavingToVideo(@PathVariable int id, @RequestParam Optional<Integer> fps,
                                           @RequestParam Optional<Long> fromTimestamp, @RequestParam Optional<Long> toTimestamp) {
    Execution execution = executionRepository.getById(id)
      .orElseThrow(() -> new BadRequestException("There isn't any execution with the specified id  get"));
    return ResponseEntity.ok(saveToVideoService.startVideoSaving(execution, fps.orElse(defaultFps),
      fromTimestamp.orElse(Long.MIN_VALUE), toTimestamp.orElse(Long.MAX_VALUE)));
  }

  @GetMapping("/{id}/video/savingState") // TODO add on swagger
  public ResponseEntity getExecutionSavingState(@PathVariable int id) {
    idCheck(id);
    return ResponseEntity.ok(saveToVideoService.getSavingProgress(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity removeExecution(@PathVariable int id) {
    idCheck(id);
    if (executionRepository.remove(id)) {
      executor.execute(() -> {
        storageService.deleteForExecution(id);
        LOGGER.info("Execution with id {} was removed", id);
      });
    }
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}") // TODO modify swagger
  public ResponseEntity updateExecution(@PathVariable int id, @RequestBody ExecutionPatchRequest patchRequest) {
    if (patchRequest.getPeriod() != null && patchRequest.getPeriod() <= 0) {
      throw new BadRequestException("The period must be greater than 0");
    }
    if (patchRequest.getTitle() != null && patchRequest.getTitle().isEmpty()) {
      throw new BadRequestException("The period must be greater than 0");
    }
    return ResponseEntity.ok(executionRepository.update(id, patchRequest));
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
