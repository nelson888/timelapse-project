package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import com.app4.project.timelapseserver.service.SaveToVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO add in swagger
@RestController
@RequestMapping("/api/videos/")
public class VideoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(VideoController.class);

  private final VideoMetadataRepository videoRepository;
  private final SaveToVideoService saveToVideoService;

  public VideoController(VideoMetadataRepository videoRepository,
                         SaveToVideoService saveToVideoService) {
    this.videoRepository = videoRepository;
    this.saveToVideoService = saveToVideoService;
  }

  @GetMapping
  public ResponseEntity getAllVideos() {
    return ResponseEntity.ok(videoRepository.getAll());
  }

  @GetMapping("/{videoId}")
  public ResponseEntity getVideoById(@PathVariable int videoId) {
    return ResponseEntity.ok(videoRepository.getByVideoId(videoId)
      .orElseThrow(() -> new NotFoundException("There is no video with id " + videoId)));
  }

  @DeleteMapping("/{videoId}")
  public ResponseEntity deleteVideo(@PathVariable int videoId) {
    videoRepository.remove(videoId);
    // TODO remove on storage
    LOGGER.info("Deleted video {}", videoId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/execution/{executionId}")
  public ResponseEntity getAllByExecution(@PathVariable int executionId) {
    return ResponseEntity.ok(videoRepository.getAllByExecutionId(executionId));
  }

  @GetMapping("/tasks")
  public ResponseEntity getAllTaskProgresses() {
    return ResponseEntity.ok(saveToVideoService.getAllTasks());
  }

  @GetMapping("/tasks/{taskId}")
  public ResponseEntity getTaskProgress(@PathVariable int taskId) {
    return ResponseEntity.ok(saveToVideoService.getOptionalSavingProgress(taskId)
      .orElseThrow(() -> new NotFoundException("There is no task with id " + taskId)));
  }

}
