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

import javax.annotation.PostConstruct;
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

  public StateController(BlockingQueue<Execution> executions, BlockingQueue<Command> commands) {
    this.executions = executions;
    this.commands = commands;
    state = new CameraState();
  }

  @GetMapping("/state")
  public ResponseEntity getState() {
    return ResponseEntity.ok(state);
  }

  @PutMapping("/state")
  public ResponseEntity updateMapping(@RequestBody CameraState state) {
    this.state = state;
    LOGGER.info("Updated camera state: {}", state);
    return ResponseEntity.ok(state);
  }

  @GetMapping("/globalState")
  public ResponseEntity globalState() {
    Execution[] executions = this.executions.toArray(new Execution[0]);
    Arrays.sort(executions); //sort in startTime order (the soon to far)
    GlobalState globalState = new GlobalState(state, executions,
        commands.toArray(new Command[0]));
    return ResponseEntity.ok(globalState);
  }

  @PostConstruct //TODO TO REMOVE ONCE WE HAVE REAL DATA
  public void fillWithFakeData() {
    LOGGER.info("Filling the server with fake data");
    long now = System.currentTimeMillis();
    long day = 1000 * 60 * 60 * 24;
    String[] titles = new String[] {
      "Pop corn qui explose dans un micro-onde",
      "floraison tulipe",
      "couché de soleil"
    };

    for (int i = 0; i < titles.length; i++) {
      long startTime = now + (i + 1) * day;
      long endTime = startTime + day / 4;
      executions.add(new Execution(titles[i], startTime, endTime, (long) (Math.random() * 100)));
    }

    LOGGER.info("Executions: {}", executions);
  }
}
