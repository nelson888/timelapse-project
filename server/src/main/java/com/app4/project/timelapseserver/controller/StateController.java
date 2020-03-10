package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StateController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StateController.class);

  private volatile CameraState state;

  public StateController(CameraState state) {
    this.state = state;
  }

  @GetMapping("/state")
  public ResponseEntity getState() {
    return ResponseEntity.ok(state);
  }

  @PutMapping("/state")
  public ResponseEntity updateState(@RequestBody CameraState state) {
    this.state = state;
    this.state.setLastHeartBeat(System.currentTimeMillis());
    LOGGER.info("Updated camera state: {}", state);
    return ResponseEntity.ok(state);
  }

}
