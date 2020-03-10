package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping("/api")
public class StateController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StateController.class);

  //thread-safe queue
  private final BlockingQueue<Execution> executions;
  private final BlockingQueue<Command> commands;
  private volatile CameraState state;

  public StateController(BlockingQueue<Execution> executions, BlockingQueue<Command> commands, CameraState state) {
    this.executions = executions;
    this.commands = commands;
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
