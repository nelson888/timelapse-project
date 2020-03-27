package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapseserver.service.SaveToVideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO renamed video task controller and use it only for taks
//  TODO add in swagger
// TODO add a cancel (delete) endpoint
@RestController
@RequestMapping("/api/videos/tasks")
public class VideoTaskController {


  private final SaveToVideoService saveToVideoService;

  public VideoTaskController(SaveToVideoService saveToVideoService) {
    this.saveToVideoService = saveToVideoService;
  }

  @GetMapping
  public ResponseEntity getAllTaskProgresses() {
    return ResponseEntity.ok(saveToVideoService.getAllTasks());
  }

  @GetMapping("/{taskId}")
  public ResponseEntity getTaskProgress(@PathVariable int taskId) {
    return ResponseEntity.ok(saveToVideoService.getOptionalSavingProgress(taskId)
      .orElseThrow(() -> new NotFoundException("There is no task with id " + taskId)));
  }

}
