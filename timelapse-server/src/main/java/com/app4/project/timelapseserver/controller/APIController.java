package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.GlobalState;
import com.app4.project.timelapseserver.exception.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

@RestController
@RequestMapping("/api")
public class APIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(APIController.class);

  //thread-safe queue
  private final Queue<Execution> executions;
  private final Queue<Command> commands;
  private volatile CameraState state;

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

  @GetMapping("/executions/get")
  public ResponseEntity consumeExecution() {
    if (executions.isEmpty()) {
      throw new BadRequestException("There isn't any execution to get");
    }
    Execution execution = executions.element();
    return ResponseEntity.ok(execution);
  }

  @DeleteMapping("/executions/remove/{id}")
  public ResponseEntity removeExecution(@PathVariable int id) {
    if (executions.removeIf(e -> e.getId() == id)) {
      LOGGER.info("Execution with id {} was removed", id);
      return ResponseEntity.ok(Boolean.TRUE);
    }
    return ResponseEntity.ok(Boolean.FALSE);
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