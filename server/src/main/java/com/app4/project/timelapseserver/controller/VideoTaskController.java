package com.app4.project.timelapseserver.controller;

// TODO add in swagger

import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapseserver.service.SaveToVideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video/tasks")
public class VideoTaskController {
// TODO add video data in storage controller?
  private final SaveToVideoService saveToVideoService;

  public VideoTaskController(SaveToVideoService saveToVideoService) {
    this.saveToVideoService = saveToVideoService;
  }

  @GetMapping("/{taskId}")
  public ResponseEntity getTaskProgress(@PathVariable int taskId) {
    return ResponseEntity.ok(saveToVideoService.getOptionalSavingProgress(taskId)
      .orElseThrow(() -> new NotFoundException("There is no task with id " + taskId)));
  }


  @GetMapping
  public ResponseEntity getAllTaskProgresses() {
    return ResponseEntity.ok(saveToVideoService.getAllTasks());
  }

}
