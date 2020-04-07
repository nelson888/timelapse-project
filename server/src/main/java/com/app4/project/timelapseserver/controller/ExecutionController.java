package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.TasksList;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.exception.ConflictException;
import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapse.model.request.ExecutionPatchRequest;
import com.app4.project.timelapseserver.repository.ExecutionRepository;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import com.app4.project.timelapseserver.service.SaveToVideoService;
import com.app4.project.timelapseserver.storage.StorageService;
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

import javax.annotation.PostConstruct;
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
  private final VideoMetadataRepository videoMetadataRepository;
  private final int defaultFps;
  private final boolean fillFakeData;

  public ExecutionController(ExecutionRepository executionRepository, StorageService storageService,
                             CameraState cameraState, SaveToVideoService saveToVideoService,
                             VideoMetadataRepository videoMetadataRepository,
                             @Value("${video.default.fps}") int defaultFps,
                             @Value("${executions.fill.data:false}") boolean fillFakeData) {
    this.executionRepository = executionRepository;
    this.storageService = storageService;
    this.cameraState = cameraState;
    this.saveToVideoService = saveToVideoService;
    this.videoMetadataRepository = videoMetadataRepository;
    this.defaultFps = defaultFps;
    this.fillFakeData = fillFakeData;
  }

  @PostMapping
  public ResponseEntity addExecution(@RequestBody Execution execution) {
    if (executionRepository.count() >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Max number of executions reached");
    }
    validate(execution);

    Optional<Execution> optOverlappedExecution = executionRepository.getAll().stream()
      .filter(execution::overlaps)
      .findFirst();
    if (optOverlappedExecution.isPresent()) {
      throw new ConflictException("Execution overlaps with execution with id " + optOverlappedExecution.get().getId());
    }
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
    return ResponseEntity.ok(executionRepository.getById(id)
      .orElseThrow(() -> new NotFoundException(String.format("Execution with id %d doesn't exists", id)))
    );
  }

  @PostMapping("/{id}/videos/generate")
  public ResponseEntity startSavingToVideo(@PathVariable int id, @RequestParam Optional<Integer> fps,
                                           @RequestParam Optional<Long> fromTimestamp, @RequestParam Optional<Long> toTimestamp) {
    Execution execution = executionRepository.getById(id)
      .orElseThrow(() -> new NotFoundException("Execution with id " + id + " was not found"));
    if (fps.isPresent() && fps.get() <= 0) {
      throw new BadRequestException("fps must be positive");
    }
    return ResponseEntity.ok(saveToVideoService.startVideoSaving(execution.getId(), fps.orElse(defaultFps),
      fromTimestamp.orElse(Long.MIN_VALUE), toTimestamp.orElse(Long.MAX_VALUE)));
  }

  @GetMapping("/{id}/videos")
  public ResponseEntity getAllByExecution(@PathVariable int id) {
    return ResponseEntity.ok(videoMetadataRepository.getAllByExecutionId(id));
  }

  @GetMapping("/{id}/videos/tasks")
  public ResponseEntity getAllTasks(@PathVariable int id) {
    if (executionRepository.getById(id).isEmpty()) {
      throw new NotFoundException("Execution with id " + id + " was not found");
    }
    return ResponseEntity.ok(new TasksList(saveToVideoService.getAllTasksForExecution(id)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity removeExecution(@PathVariable int id) {
    if (executionRepository.remove(id)) {
      executor.execute(() -> {
        storageService.deleteForExecution(id);
        LOGGER.info("Execution with id {} was removed", id);
      });
    }
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}")
  public ResponseEntity updateExecution(@PathVariable int id, @RequestBody ExecutionPatchRequest patchRequest) {
    Execution execution = executionRepository.getById(id).orElseThrow(() -> new NotFoundException("Execution with id " + id + " was not found"));

    Optional.ofNullable(patchRequest.getTitle()).ifPresent(execution::setTitle);
    Optional.ofNullable(patchRequest.getStartTime()).ifPresent(execution::setStartTime);
    Optional.ofNullable(patchRequest.getEndTime()).ifPresent(execution::setEndTime);
    Optional.ofNullable(patchRequest.getPeriod()).ifPresent(execution::setPeriod);
    validate(execution);
    Optional<Execution> optOverlappedExecution = executionRepository.getAll().stream()
      .filter(e -> e.getId() != id)
      .filter(execution::overlaps)
      .findFirst();
    if (optOverlappedExecution.isPresent()) {
      throw new ConflictException("Execution overlaps with execution with id " + optOverlappedExecution.get().getId());
    }
    executionRepository.update(id, patchRequest);
    return ResponseEntity.ok(executionRepository.getById(id));
  }

  @GetMapping("/count")
  public ResponseEntity nbExecutions() {
    return ResponseEntity.ok().body(executionRepository.count());
  }

  @GetMapping("/soonest")
  public ResponseEntity soonestExecution() {
    return ResponseEntity.ok(executionRepository.getSoonest()
      .orElseThrow(() -> new NotFoundException("There isn't any execution to get")));
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


  @PostConstruct
  public void fillWithFakeData() {
    if (!fillFakeData) {
      return;
    }
    executionRepository.removeAll();
    LOGGER.info("Filling Execution Repository with fake data");
    long now = System.currentTimeMillis();
    long day = 1000 * 60 * 60 * 24;
    String[] titles = new String[]{
      "Levee de la lune",
      "floraison tulipe",
      "couché de soleil",
      "test timelapse",
      "cours de projet"
    };

    for (int i = 0; i < titles.length; i++) {
      long startTime = now + (i + 1) * day;
      long endTime = startTime + day / 4;
      Execution execution = new Execution(titles[i], startTime, endTime, 5 + (long) (Math.random() * 10));
      executionRepository.add(execution);
    }
    LOGGER.info("Executions: {}", executionRepository.getAll());
  }
}
