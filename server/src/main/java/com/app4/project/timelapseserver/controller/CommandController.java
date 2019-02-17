package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapseserver.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

  private final BlockingQueue<Command> commands;

  public CommandController(BlockingQueue<Command> commands) {
    this.commands = commands;
  }

  @PostMapping("/")
  public ResponseEntity addCommand(@RequestBody Command command) {
    if (!commands.offer(command)) {
      throw new BadRequestException("Max number of commands reached");
    }
    LOGGER.info("New command was added: {}", command);
    return ResponseEntity.ok(command);
  }

  @GetMapping("/consume")
  public ResponseEntity consumeCommand() {
    Command command = commands.isEmpty() ? null : commands.remove();
    if (command != null) {
      LOGGER.info("Consumed command {}", command);
    }
    return ResponseEntity.ok(command);
  }

}
