package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.GlobalState;
import com.app4.project.timelapseserver.exception.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

@RestController
@RequestMapping("/api")
public class APIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(APIController.class);

  private final Queue<Execution> executions;
  private final Queue<Command> commands;
  private CameraState state;

  public APIController(Queue<Execution> executions, Queue<Command> commands) {
    this.executions = executions;
    this.commands = commands;
    state = new CameraState();
  }

  @PutMapping("/executions/new")
  public ResponseEntity addExecution(Execution execution) {
    if (executions.offer(execution)) {
      throw new BadRequestException("Max number of executions reached");
    }
    LOGGER.info("New execution was added: {}", execution);
    return ResponseEntity.ok(execution);
  }

  @GetMapping("/executions/consume")
  public ResponseEntity consumeExecution() {
    if (executions.isEmpty()) {
      throw new BadRequestException("There isn't any execution to get");
    }
    Execution execution = executions.remove();
      LOGGER.info("Consumed execution with id execution {}", execution.getId());
    return ResponseEntity.ok(execution);
  }

  @PutMapping("/commands/new")
  public ResponseEntity addCommand(Command command) {
    if (commands.offer(command)) {
      throw new BadRequestException("Max number of commands reached");
    }
    LOGGER.info("New command was added: {}", command);
    return ResponseEntity.ok(command);
  }

  @GetMapping("/commands/consume")
  public ResponseEntity consumeCommand() {
    if (commands.isEmpty()) {
      throw new BadRequestException("There isn't any execution to get");
    }
    Command command = commands.remove();
    LOGGER.info("Consumed command {}", command);
    return ResponseEntity.ok(command);
  }

  @GetMapping("/executions/count")
  public ResponseEntity nbExecutions() {
    return ResponseEntity.ok().body(executions.size());
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
    GlobalState globalState = new GlobalState(state, this.executions.toArray(new Execution[0]),
        commands.toArray(new Command[0]));
    return ResponseEntity.ok(globalState);
  }

}
